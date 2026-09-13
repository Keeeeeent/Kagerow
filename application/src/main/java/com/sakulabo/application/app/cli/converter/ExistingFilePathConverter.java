package com.sakulabo.application.app.cli.converter;

import java.nio.file.Files;
import java.nio.file.Path;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * ファイルの存在確認を行うコンバーターです
 *
 * @author keeeeeent
 */
public class ExistingFilePathConverter implements ITypeConverter<Path> {

	/** {@inheritDoc} */
	@Override
	public Path convert(String value) {
		Path path = Path.of(value);
		if (!Files.isRegularFile(path)) {
			throw new TypeConversionException("File does not exist: " + value);
		}
		return path.toAbsolutePath().normalize();
	}

}