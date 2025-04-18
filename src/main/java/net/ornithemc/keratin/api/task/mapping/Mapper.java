package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.task.TaskAware;
import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;

public interface Mapper extends TaskAware {

	static String OFFICIAL = "official";
	static String CLIENT_OFFICIAL = "clientOfficial";
	static String SERVER_OFFICIAL = "serverOfficial";
	static String INTERMEDIARY = "intermediary";
	static String NAMED = "named";

	interface MapperParameters extends WorkParameters {

		Property<Boolean> getOverwrite();

		Property<Boolean> getBrokenInnerClasses();

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
			boolean overwrite = getParameters().getOverwrite().get();
			File input = getParameters().getInput().get();
			File output = getParameters().getOutput().get();
			File mappings = getParameters().getMappings().get();

			try {
				if (KeratinGradleExtension.validateOutput(output, overwrite)) {
					return;
				}

				if (getParameters().getBrokenInnerClasses().isPresent()) {
					MappingUtils.parseInnerClasses = !getParameters().getBrokenInnerClasses().get();
				}

				run(input, output, mappings);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running mapper", e);
			} finally {
				MappingUtils.parseInnerClasses = true;
			}
		}

		abstract void run(File input, File output, File mappings) throws IOException;

	}

	abstract class MapJar extends MapperAction {

		@Override
		void run(File input, File output, File mappings) throws IOException {
			List<File> libraries = getParameters().getLibraries().get();
			String srcNs = getParameters().getSourceNamespace().get();
			String dstNs = getParameters().getTargetNamespace().get();

			Mapper._mapJar(input, output, mappings, libraries, srcNs, dstNs);
		}
	}

	abstract class MapExceptions extends MapperAction {

		@Override
		void run(File input, File output, File mappings) throws IOException {
			Mapper._mapExceptions(input, output, mappings);
		}
	}

	abstract class MapSignatures extends MapperAction {

		@Override
		void run(File input, File output, File mappings) throws IOException {
			Mapper._mapSignatures(input, output, mappings);
		}
	}

	abstract class MapNests extends MapperAction {

		@Override
		void run(File input, File output, File mappings) throws IOException {
			Mapper._mapNests(input, output, mappings);
		}
	}

	default void mapJar(File input, File output, File mappings, Collection<File> libraries, String srcNs, String dstNs) throws IOException {
		Mapper._mapJar(input, output, mappings, libraries, srcNs, dstNs);
	}

	static void _mapJar(File input, File output, File mappings, Collection<File> libraries, String srcNs, String dstNs) throws IOException {
		if (output.exists()) {
			output.delete();
		}

		TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
			.withMappings(TinyUtils.createTinyMappingProvider(mappings.toPath(), srcNs, dstNs))
			.renameInvalidLocals(true)
			.rebuildSourceFilenames(true)
			.fixPackageAccess(NAMED.equals(dstNs));
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
			throw new IOException("Failed to remap jar", e);
		}
	}

	default void mapExceptions(File input, File output, File mappings) throws IOException {
		Mapper._mapExceptions(input, output, mappings);
	}

	static void _mapExceptions(File input, File output, File mappings) throws IOException {
		MappingUtils.mapExceptions(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
	}

	default void mapSignatures(File input, File output, File mappings) throws IOException {
		Mapper._mapSignatures(input, output, mappings);
	}

	static void _mapSignatures(File input, File output, File mappings) throws IOException {
		MappingUtils.mapSignatures(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
	}

	default void mapNests(File input, File output, File mappings) throws IOException {
		Mapper._mapNests(input, output, mappings);
	}

	static void _mapNests(File input, File output, File mappings) throws IOException {
		MappingUtils.mapNests(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
	}
}
