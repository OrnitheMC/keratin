package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public interface Exceptor {

	default void exceptionsPatchJar(File input, File output, File exceptions) throws IOException {
		_exceptionsPatchJar(input, output, exceptions);
	}

	static void _exceptionsPatchJar(File input, File output, File exceptions) throws IOException {
		if (!input.equals(output)) {
			Files.copy(input, output);
		}
		net.ornithemc.exceptor.Exceptor.apply(output.toPath(), exceptions.toPath());
	}

	default void extractExceptions(File jar, File exceptions) throws IOException {
		_extractExceptions(jar, exceptions);
	}

	static void _extractExceptions(File jar, File exceptions) throws IOException {
		net.ornithemc.exceptor.Exceptor.extract(jar.toPath(), exceptions.toPath());
	}
}
