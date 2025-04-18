package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingNsCompleter;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.graph.MappingsGraph;
import net.ornithemc.keratin.api.task.mapping.graph.Validators;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.mappingutils.io.Format;

public abstract class BuildProcessedMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		getProject().getLogger().lifecycle(":building processed mappings for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();
		BuildFiles buildFiles = mappings.getBuildFiles();

		File graphDir = mappings.getMappingsDirectory();
		File output = buildFiles.getProcessedMappingsFile(minecraftVersion);

		workQueue.submit(BuildProcessedMappings.class, parameters -> {
			parameters.getMinecraftVersion().set(minecraftVersion.id());
			parameters.getGraphDirectory().set(graphDir);
			parameters.getOutput().set(output);
		});
	}

	public interface BuildParameters extends WorkParameters {

		Property<String> getMinecraftVersion();

		Property<File> getGraphDirectory();

		Property<File> getOutput();

	}

	public static abstract class BuildProcessedMappings implements WorkAction<BuildParameters>, MappingsGraph {

		@Override
		public void execute() {
			String minecraftVersion = getParameters().getMinecraftVersion().get();

			File graphDir = getParameters().getGraphDirectory().get();
			File output = getParameters().getOutput().get();

			try {
				loadMappings(minecraftVersion, graphDir, output, Format.TINY_V2, Validators.removeDummyMappings(true));

				MemoryMappingTree mappings = new MemoryMappingTree();
				MappingReader.read(output.toPath(), mappings);

				try (MappingWriter writer = MappingWriter.create(output.toPath(), MappingFormat.TINY_2_FILE)) {
					mappings.accept(new MappingNsCompleter(writer, Map.of("named", "intermediary")));
				}
			} catch (IOException e) {
				throw new UncheckedIOException("error while building processed mappings", e);
			}
		}
	}
}
