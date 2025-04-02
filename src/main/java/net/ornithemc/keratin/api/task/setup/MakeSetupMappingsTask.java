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
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.api.task.processing.Nester;

public abstract class MakeSetupMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MakeSetupMappings.class, parameters -> {
				parameters.getInputMappings().set(files.getMergedIntermediaryMappings(minecraftVersion));
				parameters.getOutputMappings().set(files.getSetupMergedIntermediaryMappings(minecraftVersion));
				parameters.getJar().set(files.getMergedJar(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
				parameters.getNestsFile().set(files.getMergedNestsFile(minecraftVersion));
			});
			workQueue.submit(MakeSetupMappings.class, parameters -> {
				parameters.getInputMappings().set(files.getNamedMappingsFile(minecraftVersion.id()));
				parameters.getOutputMappings().set(files.getSetupMergedNamedMappings(minecraftVersion));
				parameters.getJar().set(files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getTargetNamespace().set(Mapper.NAMED);
				parameters.getNestsFile().set(files.getIntermediaryMergedNestsFile(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getClientIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupClientIntermediaryMappings(minecraftVersion));
					parameters.getJar().set(files.getClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
					parameters.getNestsFile().set(files.getClientNestsFile(minecraftVersion));
				});
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getNamedMappingsFile(minecraftVersion.client().id()));
					parameters.getOutputMappings().set(files.getSetupClientNamedMappings(minecraftVersion));
					parameters.getJar().set(files.getIntermediaryClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.NAMED);
					parameters.getNestsFile().set(files.getIntermediaryClientNestsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getServerIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupServerIntermediaryMappings(minecraftVersion));
					parameters.getJar().set(files.getServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.INTERMEDIARY);
					parameters.getNestsFile().set(files.getServerNestsFile(minecraftVersion));
				});
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getNamedMappingsFile(minecraftVersion.server().id()));
					parameters.getOutputMappings().set(files.getSetupServerNamedMappings(minecraftVersion));
					parameters.getJar().set(files.getIntermediaryServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set(Mapper.NAMED);
					parameters.getNestsFile().set(files.getIntermediaryServerNestsFile(minecraftVersion));
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
				fillMappings(
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
