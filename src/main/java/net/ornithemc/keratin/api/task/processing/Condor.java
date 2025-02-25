package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

public interface Condor {

	default void lvtPatchJar(File input, File output, List<File> libraries) throws IOException {
		_lvtPatchJar(input, output, libraries);
	}

	static void _lvtPatchJar(File input, File output, List<File> libraries) throws IOException {
		if (!input.equals(output)) {
			Files.copy(input, output);
		}
		net.ornithemc.condor.Condor.run(output.toPath(), libraries.stream().map(File::toPath).toList());
	}
}
