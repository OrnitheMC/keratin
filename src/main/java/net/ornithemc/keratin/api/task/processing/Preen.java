package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

public interface Preen {

	default void splitMergedBridgeMethods(File input, File output) throws IOException {
		_splitMergedBridgeMethods(input, output);
	}

	static void _splitMergedBridgeMethods(File input, File output) throws IOException {
		if (!input.equals(output)) {
			Files.copy(input, output);
		}
		net.ornithemc.preen.Preen.splitMergedBridgeMethods(output.toPath());
	}

	default void modifyMergedBridgeMethodsAccess(File input, File output) throws IOException {
		_modifyMergedBridgeMethodsAccess(input, output);
	}

	static void _modifyMergedBridgeMethodsAccess(File input, File output) throws IOException {
		if (!input.equals(output)) {
			Files.copy(input, output);
		}
		net.ornithemc.preen.Preen.modifyMergedBridgeMethodsAccess(output.toPath());
	}
}
