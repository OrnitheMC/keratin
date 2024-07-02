package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

import io.github.gaming32.signaturechanger.cli.ApplyAction;

public interface SignaturePatcher {

	default void signaturePatchJar(File input, File output, File sigs) throws IOException {
		_signaturePatchJar(input, output, sigs);
	}

	static void _signaturePatchJar(File input, File output, File sigs) throws IOException {
		if (!input.equals(output)) {
			Files.copy(input, output);
		}
		ApplyAction.run(sigs.toPath(), List.of(output.toPath()));
	}
}
