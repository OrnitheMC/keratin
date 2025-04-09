package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.unpick.UnpickDefinitions;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class BuildUnpickDefinitionsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.hasSharedVersioning()) {
			workQueue.submit(BuildUnpickDefinitions.class, parameters -> {
				parameters.getProcessedUnpickDefinitions().set(buildFiles.getProcessedUnpickDefinitionsFile(minecraftVersion));
				parameters.getNestsFile().set(buildFiles.getNamedNestsFile(minecraftVersion));
				parameters.getUnpickDefinitions().set(buildFiles.getUnpickDefinitionsFile(minecraftVersion.id()));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(BuildUnpickDefinitions.class, parameters -> {
					parameters.getProcessedUnpickDefinitions().set(buildFiles.getProcessedUnpickDefinitionsFile(minecraftVersion));
					parameters.getNestsFile().set(buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getUnpickDefinitions().set(buildFiles.getUnpickDefinitionsFile(minecraftVersion.client().id()));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(BuildUnpickDefinitions.class, parameters -> {
					parameters.getProcessedUnpickDefinitions().set(buildFiles.getProcessedUnpickDefinitionsFile(minecraftVersion));
					parameters.getNestsFile().set(buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getUnpickDefinitions().set(buildFiles.getUnpickDefinitionsFile(minecraftVersion.server().id()));
				});
			}
		}
	}

	public interface BuildParameters extends WorkParameters {

		Property<File> getProcessedUnpickDefinitions();

		Property<File> getNestsFile();

		Property<File> getUnpickDefinitions();

	}

	public static abstract class BuildUnpickDefinitions implements WorkAction<BuildParameters>, UnpickDefinitions {

		@Override
		public void execute() {
			File input = getParameters().getProcessedUnpickDefinitions().get();
			File nests = getParameters().getNestsFile().get();
			File output = getParameters().getUnpickDefinitions().get();

			try {
				if (output.exists()) {
					output.delete();
				}

				Files.copy(input, output);

				if (nests.exists()) {
					unnestUnpickDefinitions(output, output, nests);
				}
			} catch (IOException e) {
				throw new RuntimeException("error while building unpick definitions");
			}
		}
	}
}
