package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

import net.ornithemc.condor.Options;

public interface Condor {

	default void lvtPatchJar(File input, File output, List<File> libraries, boolean obfuscateNames) throws IOException {
		_lvtPatchJar(input, output, libraries, obfuscateNames);
	}

	static void _lvtPatchJar(File input, File output, List<File> libraries, boolean obfuscateNames) throws IOException {
		if (!input.equals(output)) {
			Files.copy(input, output);
		}

		Options.Builder options = Options.builder().removeInvalidEntries();
		if (obfuscateNames) {
			options.obfuscateNames();
		} else {
			options.keepParameterNames();
		}

		net.ornithemc.condor.Condor.run(output.toPath(), libraries.stream().map(File::toPath).toList(), options.build());
	}
}
