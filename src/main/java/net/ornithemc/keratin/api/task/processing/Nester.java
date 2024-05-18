package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;
import net.ornithemc.nester.NesterException;

public interface Nester {

	default void nestJar(File input, File output, File nests) throws IOException {
		try {
			net.ornithemc.nester.Nester.nestJar(
				new net.ornithemc.nester.Nester.Options().silent(true),
				input.toPath(),
				output.toPath(),
				nests.toPath()
			);
		} catch (NesterException e) {
			throw new IOException("failed to apply nests to jar", e);
		}
	}

	default void nestMappings(File input, File output, File nests) throws IOException {
		MappingUtils.applyNests(Format.TINY_V2, input.toPath(), output.toPath(), nests.toPath());
	}
}
