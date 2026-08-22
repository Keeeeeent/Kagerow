package com.sakulabo.application.app.cli.converter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

/**
 * 親ファイルパスの存在確認を行うコンバーターです
 *
 * @author keeeeeent
 */
public class ExistingParentDirConverter implements ITypeConverter<Path> {

	/** {@inheritDoc} */
	@Override
	public Path convert(String value) {
		Path path = Path.of(value);
		Path target = path.getParent();
		if (Objects.isNull(target) || Files.notExists(target)) {
			throw new TypeConversionException("Dir does not exist: " + value);
		}
		return path;
	}

}
