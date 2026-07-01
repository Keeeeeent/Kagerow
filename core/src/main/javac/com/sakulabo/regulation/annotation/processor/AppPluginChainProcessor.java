package com.sakulabo.regulation.annotation.processor;

import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sakulabo.regulation.annotation.AnnotationProcessor;
import com.sakulabo.regulation.annotation.utils.ChainProcessor;

import com.sun.source.tree.ModuleTree;
import com.sun.source.util.TreePath;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCProvides;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.api.JavacTrees;

/**
 * AppPluginの実装プロバイダーの宣言を追加するチェーンプロセッサーです。<br/>
 * このクラスはIDEやMavenなどのビルド対象から外されたクラスです。<br/>
 * Kagerow専用コンパイラにて専用にビルドし提供されます。
 * 
 * @author keeeeeent
 */
@AnnotationProcessor
public class AppPluginChainProcessor implements ChainProcessor {

	/** AST抽象木構造取得ユーティリティクラスインスタンス */
	private JavacTrees trees;
	/** AST抽象木構造取得 */
	private TreeMaker treeMaker;
	/** Javac内部AST */
	private Names names;
	/** SPIサービスクラス完全名称 */
	private String service = "com.sakulabo.regulation.spi.PluginAdapter";
	/** モジュールエレメント */
	private static Element module;

	/**
	 * デフォルトのコンストラクタ
	 * 
	 * @param module モジュールASTインスタンス
	 */
	public AppPluginChainProcessor(Element module) {
		this.module = module;
	}

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv,
			ProcessingEnvironment processingEnv,
			Set<?> options) {

		try {

			// ASTを取得するためのユーティリティクラスインスタンスを取得
			trees = JavacTrees.instance(processingEnv);
			// コンパイラ本体を取得
			Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
			// ソースコードを生成・変形するためのインスタンス
			treeMaker = TreeMaker.instance(context);
			// Javac 内部 AST を生成・操作するインスタンスを生成
			names = Names.instance(context);

			// モジュールの取得
			if (Objects.nonNull(this.module)) {
				if (this.module instanceof ModuleElement module) {
					// モジュールインスタンス取得
					TreePath path = trees.getPath(module);
					ModuleTree moduleTree = (ModuleTree) path.getLeaf();
					JCModuleDecl moduleDecl = (JCModuleDecl) moduleTree;
					// プロバイダーの追加
					for (String clazz : (Set<String>) options) {
						addProvides(moduleDecl, names, treeMaker, service, clazz);
						logMsg("Add module-info.java ", clazz);
					}
				}
			}

		} catch (Throwable e) {
			e.printStackTrace();
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}

		return true;

	}

	/**
	 * module-info.javaの指定のSPIプロバイダーのディレクティブを追加します
	 * @param moduleDecl モジュールJCTRee
	 * @param names AST
	 * @param maker AST木構造生成インスタンス
	 * @param service SPI
	 * @param impl プロバイダー名称（withに追加される）
	 */
	void addProvides(JCModuleDecl moduleDecl, Names names, TreeMaker maker, String service, String impl) {

		// 型名をExpressionに変換
		JCExpression serviceExpr = makeQualifiedName(maker, names, service);
		JCExpression implExpr = makeQualifiedName(maker, names, impl);

		// すでに提供してるprovidesがあるか探す
		JCProvides targetProvides = null;
		for (JCTree directive : moduleDecl.directives) {
			if (directive instanceof JCProvides provides) {
				if (provides.serviceName.toString().equals(service)) {
					targetProvides = provides;
					break;
				}
			}
		}

		if (targetProvides != null) {
			// すでにある → implが登録されてなければ追加
			boolean alreadyExists = targetProvides.implNames
					.stream()
					.anyMatch(e -> e.toString().equals(impl));

			if (!alreadyExists) {
				targetProvides.implNames = targetProvides.implNames.append(implExpr);
			}
		} else {
			// なければ新しくprovidesを作成して追加
			List<JCExpression> impls = List.of(implExpr);
			JCProvides newProvides = maker.Provides(serviceExpr, impls);
			moduleDecl.directives = moduleDecl.directives.append(newProvides);
		}

	}

	/**
	 * 文字列をASTに変換します 
	 * @param maker AST木構造生成インスタンス
	 * @param names AST
	 * @param fqcn 文字列
	 * @return AST
	 */
	JCExpression makeQualifiedName(TreeMaker maker, Names names, String fqcn) {
		if (fqcn == null) {
			throw new IllegalArgumentException("FQCN must not be null");
		}
		String[] parts = fqcn.split("\\.", 0);
		JCExpression expr = maker.Ident(names.fromString(parts[0]));
		for (int i = 1; i < parts.length; i++) {
			expr = maker.Select(expr, names.fromString(parts[i]));
		}
		return expr;
	}

	/** {@inheritDoc} */
	@Override
	public void logMsg(String prefix, String logMessage) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;34mINFO[m] ");
		buffer.append(prefix);
		buffer.append("[0;33m");
		buffer.append(logMessage);
		buffer.append("[m [1m");
		System.out.println(new String(buffer));
	}

}
