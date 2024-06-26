package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.io.Files;

import io.github.gaming32.signaturechanger.cli.ApplyAction;

public interface SignaturePatcher {

	default void signaturePatchJar(File jarIn, File jarOut, File sigs) throws IOException {
		Files.copy(jarIn, jarOut);
		ApplyAction.run(sigs.toPath(), List.of(jarOut.toPath()));
	}
}
