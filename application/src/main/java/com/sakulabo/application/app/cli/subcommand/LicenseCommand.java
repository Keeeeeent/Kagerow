package com.sakulabo.application.app.cli.subcommand;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import com.sakulabo.core.Kagerow.Utilities.KagerowLogger;
import com.sakulabo.core.Kagerow.Utilities.KagerowUtilities;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

/**
 * ライセンス管理実装クラスです
 *
 * @author keeeeeent
 */
@Command(name = "license", mixinStandardHelpOptions = true, subcommands = {
})
public class LicenseCommand implements Callable<Integer>, FileVisitor<Path> {

	/** 名前空間 */
	@Parameters(index = "0", arity = "0..1")
	private String namespace;

	/** {@inheritDoc} */
	@Override
	public Integer call() throws Exception {
		try {
			Path licenseDir = KagerowUtilities.createAppDirPath().resolve("license", "licenses");
			Files.walkFileTree(licenseDir, this);
			System.out.println();
		} catch (Exception e) {
			KagerowLogger.newAppLogger().err(e);
			return Integer.valueOf(1);
		}
		return Integer.valueOf(0);
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
		String name = Objects.toString(dir.getName(dir.getNameCount() - 1));
		if (Objects.nonNull(namespace)) {
			if (name.equals(namespace)) {
				return FileVisitResult.SKIP_SUBTREE;
			}
		}
		if (!name.equals("licenses")) {
			System.out.println();
			System.out.println(String.format("License Information : %s", name));
			System.out.println("────────────────────────────────");
			System.out.println();
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
		try (Stream<String> line = Files.lines(file)) {
			line.forEach(System.out::println);
		}
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

	/** {@inheritDoc} */
	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
		return FileVisitResult.CONTINUE;
	}

}
