package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.graph.MappingsGraph;
import net.ornithemc.keratin.api.task.mapping.graph.Validators;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;

public abstract class PrepareBuildTask extends MinecraftTask implements MappingsGraph {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		NestsCache nests = files.getGlobalCache().getNestsCache();
		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();
		BuildFiles buildFiles = mappings.getBuildFiles();

		BuildNumbers nestsBuilds = keratin.getNestsBuilds(minecraftVersion);

		workQueue.submit(PrepareBuild.class, parameters -> {
			parameters.getMinecraftVersion().set(minecraftVersion.id());
			parameters.getGraphDirectory().set(mappings.getMappingsDirectory());
			parameters.getNests().set(nests.getMainIntermediaryNestsFile(minecraftVersion, nestsBuilds));
			parameters.getProcessedOutput().set(buildFiles.getProcessedMappingsFile(minecraftVersion));
			parameters.getOutput().set(buildFiles.getMappingsFile(minecraftVersion));
		});
	}

	public interface BuildParameters extends WorkParameters {

		Property<String> getMinecraftVersion();

		Property<File> getGraphDirectory();

		Property<File> getNests();

		Property<File> getProcessedOutput();

		Property<File> getOutput();

	}

	public static abstract class PrepareBuild implements WorkAction<BuildParameters>, MappingsGraph {

		@Override
		public void execute() {
			String minecraftVersion = getParameters().getMinecraftVersion().get();

			File graphDir = getParameters().getGraphDirectory().get();
			File nests = getParameters().getNests().getOrNull();
			File processedOutput = getParameters().getProcessedOutput().get();
			File output = getParameters().getOutput().get();

			try {
				loadMappings(minecraftVersion, graphDir, processedOutput, Format.TINY_V2, Validators.removeDummyMappings(true));

				if (nests == null) {
					Files.copy(processedOutput, output);
				} else {
					MappingUtils.undoNests(Format.TINY_V2, processedOutput.toPath(), output.toPath(), nests.toPath());
				}
			} catch (IOException e) {
				throw new UncheckedIOException("error while preparing mappings build", e);
			}
		}
	}
}
