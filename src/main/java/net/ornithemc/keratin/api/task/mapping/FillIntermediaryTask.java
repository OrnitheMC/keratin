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
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.setup.MappingsFiller;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class FillIntermediaryTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		GameJarsCache gameJars = globalCache.getGameJarsCache();
		MappingsCache mappings = globalCache.getMappingsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(FillIntermediary.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getInputMappings().set(mappings.getMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getOutputMappings().set(mappings.getFilledMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getJar().set(gameJars.getMergedJar(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(FillIntermediary.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInputMappings().set(mappings.getClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getOutputMappings().set(mappings.getFilledClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getJar().set(gameJars.getClientJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(FillIntermediary.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInputMappings().set(mappings.getServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getOutputMappings().set(mappings.getFilledServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getJar().set(gameJars.getServerJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
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

				fillMethodMappings(
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
