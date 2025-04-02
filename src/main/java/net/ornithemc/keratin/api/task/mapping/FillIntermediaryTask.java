package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.setup.MappingsFiller;

public abstract class FillIntermediaryTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(FillIntermediary.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getInputMappings().set(files.getMergedIntermediaryMappings(minecraftVersion));
				parameters.getOutputMappings().set(files.getFilledMergedIntermediaryMappings(minecraftVersion));
				parameters.getJar().set(files.getMergedJar(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(FillIntermediary.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInputMappings().set(files.getClientIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getFilledClientIntermediaryMappings(minecraftVersion));
					parameters.getJar().set(files.getClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(FillIntermediary.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInputMappings().set(files.getServerIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getFilledServerIntermediaryMappings(minecraftVersion));
					parameters.getJar().set(files.getServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				});
			}
		}
	}

	public interface FillIntermediaryParameters extends WorkParameters {

		Property<Boolean> getOverwrite();

		Property<File> getInputMappings();

		Property<File> getOutputMappings();

		Property<File> getJar();

		ListProperty<File> getLibraries();

	}

	public static abstract class FillIntermediary implements WorkAction<FillIntermediaryParameters>, MappingsFiller {

		@Override
		public void execute() {
			boolean overwrite = getParameters().getOverwrite().get();
			File jar = getParameters().getJar().get();
			Collection<File> libraries = getParameters().getLibraries().get();
			File input = getParameters().getInputMappings().get();
			File output = getParameters().getOutputMappings().get();

			try {
				if (KeratinGradleExtension.validateOutput(output, overwrite)) {
					return;
				}

				fillMappings(
					input,
					output,
					jar,
					libraries,
					Mapper.INTERMEDIARY
				);
			} catch (IOException e) {
				throw new RuntimeException("error while filling in intermediary mappings", e);
			}
		}
	}
}
