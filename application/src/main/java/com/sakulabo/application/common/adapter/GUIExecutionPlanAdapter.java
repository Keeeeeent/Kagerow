package com.sakulabo.application.common.adapter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import com.sakulabo.application.common.code.GUIExecutionPlanAdapterText;
import com.sakulabo.core.Kagerow.Adapter.KagerowExecutionPlanAdapter;

/**
 * 実行計画のライフサイクルフックのGUI向け実装を提供するクラスです
 *
 * @author keeeeeent
 */
public class GUIExecutionPlanAdapter implements KagerowExecutionPlanAdapter {

	/** 開始時刻 */
	private Instant startTime;
	/** 終了時刻 */
	private Instant endTime;

	/** {@inheritDoc} */
	@Override
	public void start() {
		this.startTime = Instant.now();
	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		this.endTime = Instant.now();
	}

	/**
	 * 実行時間を計算します
	 * @return 実行時間計算結果の文字列表現
	 */
	public String calcTime() {
		// 測定に失敗した場合
		if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
			return GUIExecutionPlanAdapterText.PROP_001.toString();
		}
		// 実行時間の総量取得
		Duration time = Duration.between(startTime, endTime);
		// 秒以下の時間を算出
		double rowSec = time.toSecondsPart() + (double) time.toMillisPart() / 1000;
		// 秒数を表示する場合、無限少数は4桁で切り上げる
		// 末尾の0は削除し、文字列に変換する
		String sec = BigDecimal.valueOf(rowSec)
				.setScale(4, RoundingMode.UP)
				.stripTrailingZeros()
				.toPlainString();
		String format;
		// フォーマット
		if (0 < time.toDays()) {
			format = String.format(
					GUIExecutionPlanAdapterText.PROP_002.toString(),
					time.toDays(),
					time.toHoursPart(),
					time.toMinutesPart(), sec);
		} else if (0 < time.toHours()) {
			format = String.format(
					GUIExecutionPlanAdapterText.PROP_003.toString(),
					time.toHours(),
					time.toMinutesPart(), sec);
		} else {
			format = String.format(GUIExecutionPlanAdapterText.PROP_004.toString(), sec);
		}
		return format;
	}

	/** {@inheritDoc} */
	@Override
	public void startValidation() {
	}

	/** {@inheritDoc} */
	@Override
	public void endValidation() {
	}

	/** {@inheritDoc} */
	@Override
	public void startCreateKDB() {
	}

	/** {@inheritDoc} */
	@Override
	public void endCreateKDB() {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoInputPlugin() {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoInputPlugin() {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoInputPluginIndividual() {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoInputPluginIndividual(ExecutPluginInfo info) {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoKsql() {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoKsql() {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoKsqlIndividual(String id) {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoKsqlIndividual(ExecutKsqlInfo info) {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoOutputPlugin() {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoOutputPlugin() {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoOutputPluginIndividual() {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoOutputPluginIndividual(ExecutPluginInfo info) {
	}

	/** {@inheritDoc} */
	@Override
	public void startDoCmd() {
	}

	/** {@inheritDoc} */
	@Override
	public void endDoCmd() {
	}

}
