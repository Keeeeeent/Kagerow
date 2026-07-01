package com.sakulabo.regulation.annotation.processor;

import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import com.sakulabo.regulation.annotation.AnnotationProcessor;
import com.sakulabo.regulation.annotation.utils.ChainProcessor;

import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.source.tree.ClassTree;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.api.JavacTrees;

/**
 * AppLoggerが付与されたクラスにロガーフィールドを追加するチェーンプロセッサーです<br/>
 * このクラスはIDEやMavenなどのビルド対象から外されたクラスです。<br/>
 * Kagerow専用コンパイラにて専用にビルドし提供されます。
 * 
 * @author keeeeeent
 */
@AnnotationProcessor
public class AppLoggerChainProcessor implements ChainProcessor {

	/** {@inheritDoc} */
	@Override
	@SuppressWarnings("unchecked")
	public boolean process(Set<? extends TypeElement> annotations,
			RoundEnvironment roundEnv,
			ProcessingEnvironment processingEnv,
			Set<?> options) {

		try {

			// ASTを取得するためのユーティリティクラスインスタンスを取得
			JavacTrees trees = JavacTrees.instance(processingEnv);
			// コンパイラ本体を取得
			Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
			// ソースコードを生成・変形するためのインスタンス
			TreeMaker treeMaker = TreeMaker.instance(context);
			// Javac 内部 AST を生成・操作するインスタンスを生成
			Names names = Names.instance(context);

			// ロガーの追加
			for (Element clazz : (Set<Element>) options) {

				// 対象クラスの JCClassDecl を取得（JavacTrees 経由）
				TreePath path = trees.getPath(clazz);
				ClassTree classTree = (ClassTree) path.getLeaf();
				JCClassDecl classDecl = (JCClassDecl) classTree;

				// フィールドのシンボルや型を生成 TODO IDEで検知されないフィールドのため、プラグインを今後開発する必要がある
				JCModifiers modifiers = treeMaker.Modifiers(Flags.PRIVATE);
				JCExpression fieldType = makeQualifiedName(treeMaker, names, "java.lang.String");
				// フィールド名
				Name fieldName = names.fromString("log");
				// フィールド定義ノード作成（初期値なし）
				JCVariableDecl field = treeMaker.VarDef(modifiers, fieldName, fieldType, null);
				// フィールドをクラス定義に追加（先頭に追加）
				classDecl.defs = classDecl.defs.prepend(field);

				// ログ出力
				logMsg("Add Logger Field to ", Objects.toString(clazz));

			}

		} catch (Throwable e) {
			processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
		}

		return true;
	}

	/**
	 * 文字列をASTに変換します 
	 * @param maker AST木構造生成インスタンス
	 * @param names AST
	 * @param fqcn 文字列
	 * @return AST
	 */
	JCExpression makeQualifiedName(TreeMaker maker, Names names, String fqcn) {
		if (Objects.isNull(fqcn)) {
			throw new IllegalArgumentException("FQCN must not be null");
		}
		String[] parts = fqcn.split("\\.", 0);
		JCExpression expr = maker.Ident(names.fromString(parts[0]));
		for (int i = 1; i < parts.length; i++) {
			expr = maker.Select(expr, names.fromString(parts[i]));
		}
		return expr;
	}

}
