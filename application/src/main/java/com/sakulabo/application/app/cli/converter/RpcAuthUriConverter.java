package com.sakulabo.application.app.cli.converter;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * 指定された文字列をRPC-URIを遵守したURI形式に変換するコンバーターです
 *
 * @author keeeeeent
 */
public class RpcAuthUriConverter implements ITypeConverter<URI> {

	/** URI形式検証フォーマット */
	private static final Pattern pattern = Pattern.compile("rpc://\\S+\\?token=\\S+");

	/** {@inheritDoc} */
	@Override
	public URI convert(String value) throws Exception {
		Matcher matcher = pattern.matcher(value);
		if (matcher.matches()) {
			return URI.create(value);
		}
		throw new TypeConversionException("The specified URI format is invalid: " + value);
	}

}
