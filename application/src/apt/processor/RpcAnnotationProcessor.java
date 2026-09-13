package processor;

import java.io.IOException;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic.Kind;
import javax.tools.FileObject;

/**
 * RPCエンドポイント向けXSDを生成するアノテーションプロセッサーです
 *
 * @author keeeeeent
 */
@SupportedAnnotationTypes("com.sakulabo.application.app.rpc.RpcSetting")
public class RpcAnnotationProcessor extends AbstractProcessor {

	/** XSD規定フォーマット */
	private final static MessageFormat XML_FORMAT = new MessageFormat(
			"""
					<?xml version="1.0" encoding="UTF-8"?>
					<xs:schema xmlns:xs="http://www.w3.org/2001/XMLSchema"
						elementFormDefault="qualified">

					    <!-- 構造体定義 -->
					    <xs:complexType name="StructType">
					    	<xs:sequence>
						        <xs:element name="struct">
						            <xs:complexType>
						            	<xs:sequence>
								            <xs:element name="member" minOccurs="0" maxOccurs="unbounded">
								                {0}
							                </xs:element>
										</xs:sequence>
						            </xs:complexType>
						        </xs:element>
					        </xs:sequence>
					    </xs:complexType>

					    <xs:complexType name="RpcParamType">
						    <xs:sequence>
						        <xs:element name="param">
						            <xs:complexType>
						                <xs:sequence>
						                    <xs:element name="value" type="StructType" minOccurs="1" maxOccurs="1"/>
						                </xs:sequence>
						            </xs:complexType>
						        </xs:element>
					        </xs:sequence>
					    </xs:complexType>

					    <xs:complexType name="RpcArrayType">
						    <xs:sequence>
								<xs:element name="data" minOccurs="1" maxOccurs="unbounded">
									<xs:complexType>
										<xs:sequence>
											<xs:element name="value">
												<xs:complexType>
													<xs:sequence>
														<xs:element name="string" type="xs:string" minOccurs="1" maxOccurs="1"/>
													</xs:sequence>
												</xs:complexType>
											</xs:element>
										</xs:sequence>
									</xs:complexType>
								</xs:element>
						    </xs:sequence>
						</xs:complexType>

						<!-- ルート要素 -->
						<xs:element name="methodCall">
							<xs:complexType>
								<xs:sequence>
					                <xs:element name="methodName" minOccurs="1" maxOccurs="1">
									    <xs:simpleType>
									        <xs:restriction base="xs:string">
									            <xs:enumeration value="{1}"/>
									        </xs:restriction>
									    </xs:simpleType>
									</xs:element>
					                <xs:element name="params" type="RpcParamType" minOccurs="1" maxOccurs="1"/>
					            </xs:sequence>
					        </xs:complexType>
					    </xs:element>

					</xs:schema>""");

	/** XSDパラメータフォーマット */
	private final static MessageFormat STRUCT_XML_FORMAT = new MessageFormat("""
			<xs:element name="name">
												    <xs:simpleType>
												        <xs:restriction base="xs:string">
												            <xs:enumeration value="{0}"/>
												        </xs:restriction>
												    </xs:simpleType>
												</xs:element>
												<xs:element name="value">
										          <xs:complexType>
										              <xs:sequence>
										                  <xs:element name="{1}" type="{2}"/>
										              </xs:sequence>
										          </xs:complexType>
												</xs:element>""");

	/** {@inheritDoc} */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement ano : annotations) {
			for (Element elem : roundEnv.getElementsAnnotatedWith(ano)) {
				// クラスのメソッドを全て走査
				for (Element code : elem.getEnclosedElements()) {
					if (code instanceof ExecutableElement method) {
						if (method.getKind() == ElementKind.METHOD) {

							// RPCエンドポイントのの場合、追加処理を実行
							// 基本的にこのタイミングで生成されるリストは要素が1つになる想定
							// 将来の拡張性も担保するためリストで持つ
							List<AnnotationMirror> methodSettiing = isTarget(method,
									"com.sakulabo.application.app.rpc.RpcMethod");

							for (AnnotationMirror mirror : methodSettiing) {

								// RPCメソッド情報を取得
								Map<? extends ExecutableElement, ? extends AnnotationValue> data = mirror
										.getElementValues();

								// RpcMethodアノテーションの要素解析を実施
								for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> dat : data
										.entrySet()) {

									// 要素を取得
									String name = dat.getKey().getSimpleName().toString();
									Object value = dat.getValue().getValue();

									// valueの場合、バインドされるRPCメソッド名が取得できる
									if ("value".equals(name)) {

										// 論理データ作成
										List<SearchParam> params = createParam(method);
										XmlRpcStruct struct = new XmlRpcStruct((String) value, params);
										String xsd = createXSD(struct);

										// ファイル書き出し
										try {
											Filer filer = processingEnv.getFiler();
											FileObject file = filer.createResource(
													StandardLocation.CLASS_OUTPUT,
													"",
													createXSDFilePath(method));
											// ログ書き出し
											resourceLogMsg(Objects.toString(file.toUri()));

											// XSD出力
											try (Writer writer = file.openWriter()) {
												writer.write(xsd);
											}

										} catch (IOException e) {
											processingEnv.getMessager().printMessage(
													Kind.ERROR,
													e.getMessage());
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	/** {@inheritDoc} */
	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}

	/**
	 * XSDファイルパスを生成します
	 * @param method メソッド（ソースコード）
	 * @return XSDファイルパス
	 * @throws IOException クラス定義不正
	 */
	private String createXSDFilePath(ExecutableElement method) throws IOException {

		// RPC設定情報を検索
		List<AnnotationMirror> methodSettiing = isTarget(method.getEnclosingElement(),
				"com.sakulabo.application.app.rpc.RpcSetting");
		// RPC設定情報を取得
		Map<? extends ExecutableElement, ? extends AnnotationValue> data = methodSettiing
				.getFirst()
				.getElementValues();

		// RpcMethodアノテーションの要素解析を実施
		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> dat : data.entrySet()) {

			// 要素を取得
			String name = dat.getKey().getSimpleName().toString();
			Object value = dat.getValue().getValue();

			if ("value".equals(name)) {
				// RPCメソッド設定情報を検索
				Map<? extends ExecutableElement, ? extends AnnotationValue> methodInfo = isTarget(method,
						"com.sakulabo.application.app.rpc.RpcMethod")
								.getFirst()
								.getElementValues();
				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> minfo : methodInfo.entrySet()) {
					// 要素を取得
					String mname = minfo.getKey().getSimpleName().toString();
					Object mvalue = minfo.getValue().getValue();
					if ("value".equals(mname)) {
						return "rpc-xsd/request-%s-%s.xsd".formatted(value, mvalue);
					}
				}
			}

		}

		// 対象が存在しない場合はコンパイルエラー
		throw new IOException("Invalid setting");

	}

	/**
	 * リソースにプロバイダーを追加した時のメッセージを出力します
	 * @param filePath 出力先
	 */
	private void resourceLogMsg(String filePath) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[[1;34mINFO[m] ");
		buffer.append("Generate XSD-FILE " + filePath);
		System.out.println(new String(buffer));
	}

	/**
	 * XSD文字列を生成します
	 * @param struct XSD論理データ構造
	 * @return XSD文字列
	 */
	private String createXSD(XmlRpcStruct struct) {
		StringBuilder builder = new StringBuilder();
		String member = """
				<xs:complexType>
								                <xs:sequence>
								                    %s
								                </xs:sequence>
								            </xs:complexType>""";
		for (SearchParam param : struct.params()) {
			String formatedParam = STRUCT_XML_FORMAT.format(new Object[] {
					param.value, param.getTagName(), param.getTagType()
			});
			builder.append(formatedParam);
		}
		if (0 < builder.length()) {
			member = member.formatted(builder.toString());
		} else {
			member = "";
		}
		return XML_FORMAT.format(new Object[] { member, struct.methodName });
	}

	/**
	 * XSD論理データ構造
	 * @param methodName RPCメソッド名
	 * @param params PRCメソッドパラメータ
	 */
	private static record XmlRpcStruct(
			String methodName,
			List<SearchParam> params) {
	};

	/**
	 * RPCメソッドパラメータ論理データ構造
	 * @param value パラメータ名称
	 * @param required 必須フラグ
	 * @param className クラス情報
	 */
	private static record SearchParam(
			String value,
			boolean required,
			String className) {

		/**
		 * XSDエンドポイント受け取りタグ種別を生成します
		 * @return タグ種別
		 */
		String getTagName() {
			return switch (className) {
			case "com.sakulabo.application.app.rpc.datatype.receive.Base64ReceiveDataType" -> {
				yield "base64";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType" -> {
				yield "boolean";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.DateTimeReceiveDataType" -> {
				yield "dateTime.iso8601";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.DoubleReceiveDataType" -> {
				yield "double";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.IntegerReceiveDataType" -> {
				yield "int";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.ArrayReceiveDataType" -> {
				yield "array";
			}
			case null -> "nil";
			default -> "string";
			};
		}

		/**
		 * XSDエンドポイント受け取りデータタイプを取得します
		 * @return データタイプ
		 */
		String getTagType() {
			return switch (className) {
			case "com.sakulabo.application.app.rpc.datatype.receive.Base64ReceiveDataType" -> {
				yield "xs:base64Binary";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.BooleanReceiveDataType" -> {
				yield "xs:boolean";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.DateTimeReceiveDataType" -> {
				yield "xs:dateTime";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.DoubleReceiveDataType" -> {
				yield "xs:double";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.IntegerReceiveDataType" -> {
				yield "xs:int";
			}
			case "com.sakulabo.application.app.rpc.datatype.receive.ArrayReceiveDataType" -> {
				yield "RpcArrayType";
			}
			case null -> "xs:string";
			default -> "xs:string";
			};
		}
	}

	/**
	 * PRCメソッドパラメータの論理データ構造を生成します
	 * @param method メソッド（ソースコード）
	 * @return PRCメソッドパラメータの論理データ構造
	 */
	private List<SearchParam> createParam(ExecutableElement method) {

		List<SearchParam> result = new ArrayList<>();

		for (VariableElement param : method.getParameters()) {

			List<AnnotationMirror> paramSettiing = isTarget(param,
					"com.sakulabo.application.app.rpc.RpcMethodParam");

			for (AnnotationMirror methodParam : paramSettiing) {

				Map<? extends ExecutableElement, ? extends AnnotationValue> data = methodParam
						.getElementValues();

				String value = null;
				boolean required = false;

				for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> dat : data
						.entrySet()) {
					String key = dat.getKey().getSimpleName().toString();
					Object val = dat.getValue().getValue();
					if ("value".equals(key)) {
						value = (String) val;
					}
					if ("required".equals(key)) {
						required = (boolean) val;
					}
				}

				String className = param.asType().toString();

				result.add(new SearchParam(value, required, className));

			}
		}
		return result;
	}

	/**
	 * 文字列から指定された要素が処理対象であるか判定します
	 * @param target 対象要素
	 * @param name 検索条件
	 * @return 検索結果
	 */
	private List<AnnotationMirror> isTarget(Element target, String name) {
		List<AnnotationMirror> resultList = new ArrayList<>();
		for (AnnotationMirror mirror : target.getAnnotationMirrors()) {
			String annotationName = ((TypeElement) mirror.getAnnotationType().asElement())
					.getQualifiedName()
					.toString();
			if (annotationName.equals(name)) {
				resultList.add(mirror);
			}
		}
		return resultList;
	}

}
