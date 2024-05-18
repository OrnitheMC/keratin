package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.graph.MappingsGraph;
import net.ornithemc.keratin.api.task.mapping.graph.Validators;
import net.ornithemc.mappingutils.io.Format;

public abstract class BuildProcessedMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		getProject().getLogger().lifecycle(":building processed mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		File output = files.getProcessedNamedMappings(minecraftVersion);

		workQueue.submit(BuildProcessedMappings.class, parameters -> {
			parameters.getMinecraftVersion().set(minecraftVersion);
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
				loadMappings(minecraftVersion, graphDir, output, Format.TINY_V2, Validators.REMOVE_DUMMY_MAPPINGS);
			} catch (IOException e) {
				throw new UncheckedIOException("error while building processed mappings", e);
			}
		}
	}
}
