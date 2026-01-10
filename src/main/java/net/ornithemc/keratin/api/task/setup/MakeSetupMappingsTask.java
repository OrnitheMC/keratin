package net.ornithemc.keratin.api.task.setup;

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
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.api.task.processing.Nester;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupFiles;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MakeSetupMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		GameJarsCache gameJars = globalCache.getGameJarsCache();
		MappedJarsCache mappedJars = globalCache.getMappedJarsCache();
		MappingsCache mappings = globalCache.getMappingsCache();
		NestsCache nests = globalCache.getNestsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();
		SetupFiles setupFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getSetupFiles();

		BuildNumbers nestsBuilds = keratin.getNestsBuilds(minecraftVersion);

		if (minecraftVersion.canBeMergedAsObfuscated()) {
			int mappingsBuild = keratin.getNamedMappingsBuild(minecraftVersion.id());

			workQueue.submit(MakeSetupMappings.class, parameters -> {
				parameters.getInputMappings().set(mappings.getMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getOutputMappings().set(setupFiles.getMergedIntermediaryMappingsFile(minecraftVersion));
				parameters.getJar().set(gameJars.getMergedJar(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
				parameters.getNestsFile().set(nests.getMergedNestsFile(minecraftVersion, nestsBuilds));
			});
			workQueue.submit(MakeSetupMappings.class, parameters -> {
				parameters.getInputMappings().set(mappings.getNamedMappingsFile(minecraftVersion.id(), mappingsBuild));
				parameters.getOutputMappings().set(setupFiles.getMergedNamedMappingsFile(minecraftVersion));
				parameters.getJar().set(mappedJars.getIntermediaryMergedJar(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getTargetNamespace().set(Mapper.NAMED);
				parameters.getNestsFile().set(nests.getIntermediaryMergedNestsFile(minecraftVersion, nestsBuilds));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				int mappingsBuild = keratin.getNamedMappingsBuild(minecraftVersion.client().id());

				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(mappings.getClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getOutputMappings().set(setupFiles.getClientIntermediaryMappingsFile(minecraftVersion));
					parameters.getJar().set(gameJars.getClientJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
					parameters.getNestsFile().set(nests.getClientNestsFile(minecraftVersion, nestsBuilds));
				});
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(mappings.getNamedMappingsFile(minecraftVersion.client().id(), mappingsBuild));
					parameters.getOutputMappings().set(setupFiles.getClientNamedMappingsFile(minecraftVersion));
					parameters.getJar().set(mappedJars.getIntermediaryClientJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.NAMED);
					parameters.getNestsFile().set(nests.getIntermediaryClientNestsFile(minecraftVersion, nestsBuilds));
				});
			}
			if (minecraftVersion.hasServer()) {
				int mappingsBuild = keratin.getNamedMappingsBuild(minecraftVersion.server().id());

				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(mappings.getServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getOutputMappings().set(setupFiles.getServerIntermediaryMappingsFile(minecraftVersion));
					parameters.getJar().set(gameJars.getServerJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
					parameters.getNestsFile().set(nests.getServerNestsFile(minecraftVersion, nestsBuilds));
				});
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(mappings.getNamedMappingsFile(minecraftVersion.server().id(), mappingsBuild));
					parameters.getOutputMappings().set(setupFiles.getServerNamedMappingsFile(minecraftVersion));
					parameters.getJar().set(mappedJars.getIntermediaryServerJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.NAMED);
					parameters.getNestsFile().set(nests.getIntermediaryServerNestsFile(minecraftVersion, nestsBuilds));
				});
			}
		}
	}

	public interface SetupMappingsParameters extends WorkParameters {

		Property<File> getInputMappings();

		Property<File> getOutputMappings();

		Property<File> getJar();

		ListProperty<File> getLibraries();

		Property<String> getTargetNamespace();

		Property<File> getNestsFile();

	}

	public static abstract class MakeSetupMappings implements WorkAction<SetupMappingsParameters>, MappingsFiller, Nester {

		@Override
		public void execute() {
			File jar = getParameters().getJar().get();
			Collection<File> libraries = getParameters().getLibraries().get();
			File input = getParameters().getInputMappings().get();
			File output = getParameters().getOutputMappings().get();
			String targetNs = getParameters().getTargetNamespace().get();
			File nests = getParameters().getNestsFile().getOrNull();

			try {
				fillMethodMappings(
					input,
					output,
					jar,
					libraries,
					targetNs
				);
			} catch (IOException e) {
				throw new RuntimeException("error while filling in " + targetNs + " mappings", e);
			}

			if (nests != null) {
				try {
					nestMappings(output, output, nests);
				} catch (IOException e) {
					throw new RuntimeException("error while running nester", e);
				}
			}
		}
	}
}
