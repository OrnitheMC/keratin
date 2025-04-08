package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import com.google.common.io.Files;

import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;
import net.ornithemc.nester.NesterException;

public interface Nester {

	interface MinecraftProcessorParameters extends WorkParameters {

		Property<File> getInputJar();

		Property<File> getOutputJar();

		Property<File> getNestsFile();

	}

	abstract class NestJar implements WorkAction<MinecraftProcessorParameters>, Nester {

		@Override
		public void execute() {
			try {
				File jarIn = getParameters().getInputJar().get();
				File jarOut = getParameters().getOutputJar().get();
				File nests = getParameters().getNestsFile().getOrNull();

				if (nests != null) {
					nestJar(jarIn, jarOut, nests);
				} else {
					Files.copy(jarIn, jarOut);
				}
			} catch (Exception e) {
				throw new RuntimeException("error while running nester", e);
			}
		}
	}

	default void nestJar(File input, File output, File nests) throws IOException {
		_nestJar(input, output, nests);
	}

	static void _nestJar(File input, File output, File nests) throws IOException {
		try {
			if (input.equals(output)) {
				File tmp = output;

				while (tmp.exists()) {
					tmp = new File(tmp.getParentFile(), tmp.getName() + ".tmp");
				}

				Files.copy(input, tmp);
				_nestJar(tmp, output, nests);

				tmp.delete();
			} else {
				net.ornithemc.nester.Nester.nestJar(
					input.toPath(),
					output.toPath(),
					nests.toPath()
				);
			}
		} catch (NesterException e) {
			throw new IOException("failed to apply nests to jar", e);
		}
	}

	default void nestMappings(File input, File output, File nests) throws IOException {
		_nestMappings(input, output, nests);
	}

	static void _nestMappings(File input, File output, File nests) throws IOException {
		MappingUtils.applyNests(Format.TINY_V2, input.toPath(), output.toPath(), nests.toPath());
	}
}
