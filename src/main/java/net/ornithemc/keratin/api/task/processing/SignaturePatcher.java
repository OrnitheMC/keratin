package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

import io.github.gaming32.signaturechanger.cli.ApplyAction;
import io.github.gaming32.signaturechanger.cli.GenerateAction;
import io.github.gaming32.signaturechanger.generator.SigsClassGenerator.EmptySignatureMode;

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

	default void extractSignatures(File jar, File sigs) throws IOException {
		_extractSignatures(jar, sigs);
	}

	static void _extractSignatures(File jar, File sigs) throws IOException {
		GenerateAction.run(sigs.toPath(), List.of(jar.toPath()), EmptySignatureMode.IGNORE);
	}
}
