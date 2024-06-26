package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import net.ornithemc.keratin.api.task.TaskAware;
import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;

public interface Mapper extends TaskAware {

	interface MapperParameters extends WorkParameters {

		Property<File> getInput();

		Property<File> getOutput();

		Property<File> getMappings();

		ListProperty<File> getLibraries();

		Property<String> getSourceNamespace();

		Property<String> getTargetNamespace();

	}

	abstract class MapperAction implements WorkAction<MapperParameters> {

		@Override
		public void execute() {
			try {
				map(
					getParameters().getInput().get(),
					getParameters().getOutput().get(),
					getParameters().getMappings().get()
				);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running mapper", e);
			}
		}

		abstract void map(File input, File output, File mappings) throws IOException;

	}

	abstract class MapJar extends MapperAction {

		@Override
		void map(File input, File output, File mappings) throws IOException {
			List<File> libraries = getParameters().getLibraries().get();
			String srcNs = getParameters().getSourceNamespace().get();
			String dstNs = getParameters().getTargetNamespace().get();

			if (output.exists()) {
				output.delete();
			}

			TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
				.withMappings(TinyUtils.createTinyMappingProvider(mappings.toPath(), srcNs, dstNs))
				.renameInvalidLocals(true)
				.rebuildSourceFilenames(true);
			TinyRemapper remapper = remapperBuilder.build();

			try {
				OutputConsumerPath.Builder outputConsumerBuilder = new OutputConsumerPath.Builder(output.toPath());
				OutputConsumerPath outputConsumer = outputConsumerBuilder.build();
				outputConsumer.addNonClassFiles(input.toPath());
				remapper.readInputs(input.toPath());
				for (File library : libraries) {
					remapper.readClassPath(library.toPath());
				}
				remapper.apply(outputConsumer);
				outputConsumer.close();
				remapper.finish();
			} catch (Exception e) {
				remapper.finish();
				throw new RuntimeException("Failed to remap jar", e);
			}
		}
	}

	abstract class MapNests extends MapperAction {

		@Override
		void map(File input, File output, File mappings) throws IOException {
			MappingUtils.mapNests(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
		}
	}

	abstract class MapSparrow extends MapperAction {

		@Override
		void map(File input, File output, File mappings) throws IOException {
			MappingUtils.mapSignatures(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
		}
	}
}
