package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.gradle.api.Action;

import net.fabricmc.tinyremapper.OutputConsumerPath;
import net.fabricmc.tinyremapper.TinyRemapper;
import net.fabricmc.tinyremapper.TinyUtils;

import net.ornithemc.keratin.api.task.TaskAware;
import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;

public interface Mapper extends TaskAware {

	default void mapJar(File input, File output, File mappings, Collection<File> libraries, String srcNs, String dstNs) {
		mapJar(input, output, mappings, libraries, srcNs, dstNs, builder -> { });
	}

	default void mapJar(File input, File output, File mappings, Collection<File> libraries, String srcNs, String dstNs, Action<TinyRemapper.Builder> action) {
		if (output.exists()) {
			output.delete();
		}

		TinyRemapper.Builder remapperBuilder = TinyRemapper.newRemapper()
			.withMappings(TinyUtils.createTinyMappingProvider(mappings.toPath(), srcNs, dstNs))
			.renameInvalidLocals(true)
			.rebuildSourceFilenames(true);
		action.execute(remapperBuilder);
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

	default void mapNests(File input, File output, File mappings) throws IOException {
		MappingUtils.mapNests(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
	}

	default void mapSparrow(File input, File output, File mappings) throws IOException {
		MappingUtils.mapSignatures(input.toPath(), output.toPath(), Format.TINY_V2, mappings.toPath());
	}
}
