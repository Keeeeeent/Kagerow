package com.sakulabo.application.app.rpc.executor.Impl;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.UnaryOperator;

import com.sakulabo.application.app.rpc.RpcFilter;
import com.sakulabo.application.app.rpc.RpcMethod;
import com.sakulabo.application.app.rpc.RpcMethodParam;
import com.sakulabo.application.app.rpc.RpcSetting;
import com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.PathReceiveDataType;
import com.sakulabo.application.app.rpc.datatype.receive.StringReceiveDataType;
import com.sakulabo.application.app.rpc.exception.RpcRuntimeException;
import com.sakulabo.application.app.rpc.executor.ScriptExecutor;
import com.sakulabo.application.app.rpc.filters.CertificationFilter;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanBaseAdapter;
import com.sakulabo.core.Kagerow.Utilities.KagerowExecutionPlanAccessor;
import com.sakulabo.core.Kagerow.Utilities.KagerowScriptAccessor;

/**
 * スクリプト実行機能のRPCコントローラー実装クラス
 *
 * @author keeeeeent
 */
@RpcSetting("script")
@RpcFilter(filter = CertificationFilter.class, required = true)
public class ScriptExecutorImpl implements ScriptExecutor {

	/** セッション格納メモリ */
	private static final Map<UUID, KagerowExecutionPlanAdapterImpl> sessions = new ConcurrentHashMap<>();

	/**
	 * セッション管理クラス
	 */
	private class KagerowExecutionPlanAdapterImpl extends KagerowExecutionPlanBaseAdapter
			implements UnaryOperator<KagerowScriptAccessor> {

		/** 有効期間 */
		private static final Duration MAX_LIMIT = Duration.ofMinutes(30);
		/** 有効期限 */
		private volatile Instant limit = Instant.now().plus(MAX_LIMIT);
		/** セッション */
		private KagerowExecutionPlanAccessor plan;
		/** スクリプト環境変数 */
		private List<String> env;

		/**
		 * コンストラクタ
		 * 
		 * @param env スクリプト環境変数
		 */
		private KagerowExecutionPlanAdapterImpl(List<String> env) {
			this.env = env;
		}

		/** {@inheritDoc} */
		@Override
		public void end() {
			super.end();
			// 有効期限を延長
			synchronized (this) {
				limit = Instant.now().plus(MAX_LIMIT);
			}
		}

		/**
		 * 有効期限内か判定します
		 * 
		 * @return 判定結果
		 */
		public boolean isValid() {
			return Instant.now().isAfter(limit);
		}

		/** {@inheritDoc} */
		@Override
		public KagerowScriptAccessor apply(KagerowScriptAccessor scriptAccessor) {
			Map<String, String> env = scriptAccessor.getEnv();
			for (String value : this.env) {
				String[] pair = value.split("=", 2);
				String key = pair[0];
				String val = pair.length > 1 ? pair[1] : "";
				env.put(key, val);
			}
			KagerowScriptAccessor newScriptAccessor = scriptAccessor.setEnv(env);
			return newScriptAccessor;
		}

	}

	/** {@inheritDoc} */
	@Override
	@RpcMethod("execute")
	public void executeScript(
			@RpcMethodParam("sessionid") StringReceiveDataType sessionId,
			@RpcMethodParam("secure") BooleanReceiveDataType isSecure,
			@RpcMethodParam("path") PathReceiveDataType path) {

		try {

			// 引数取得
			boolean isSecureFlag = isSecure.getRawType().orElse(false);
			String uuid = sessionId.getRawType().orElse(null);
			KagerowExecutionPlanAdapterImpl adapter = null;

			// 初回実行か判定
			if (Objects.isNull(uuid)) {
				// パスを取得
				Optional<Path> filePath = path.getRawType();
				if (filePath.isEmpty()) {
					throw new RpcRuntimeException("Please specify the path to the script file");
				}
				// アダプター生成
				adapter = new KagerowExecutionPlanAdapterImpl(new ArrayList<>());
				// スクリプト実行
				KagerowExecutionPlanAccessor plan = KagerowExecutionPlanAccessor.execute(filePath.get(), adapter,
						isSecureFlag);
				// セッション保存
				adapter.plan = plan;
			} else {
				// アダプター取得
				UUID key = UUID.fromString(uuid);
				adapter = sessions.get(key);
				// 有効期限の確認
				if (!adapter.isValid()) {
					// 有効期限切れの場合、セッションをクローズ
					sessions.remove(key);
					adapter.plan.close();
					// 例外スロー
					throw new RpcRuntimeException("This session has already ended");
				}
				// 有効期限内の場合スクリプト実行
				adapter.plan.execute();
			}

		} catch (Exception e) {
			throw new RpcRuntimeException("Script Execution Failed", e);
		}
	}
}
