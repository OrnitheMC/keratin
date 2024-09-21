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
				parameters.getTargetNamespace().set("intermediary");
				parameters.getNestsFile().set(files.getMergedNests(minecraftVersion));
			});
			workQueue.submit(MakeSetupMappings.class, parameters -> {
				parameters.getInputMappings().set(files.getFeatherMappings(minecraftVersion.id()));
				parameters.getOutputMappings().set(files.getSetupMergedNamedMappings(minecraftVersion));
				parameters.getJar().set(files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getTargetNamespace().set("named");
				parameters.getNestsFile().set(files.getIntermediaryMergedNests(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getClientIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupClientIntermediaryMappings(minecraftVersion));
					parameters.getJar().set(files.getClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set("intermediary");
					parameters.getNestsFile().set(files.getClientNests(minecraftVersion));
				});
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getFeatherMappings(minecraftVersion.client().id()));
					parameters.getOutputMappings().set(files.getSetupClientNamedMappings(minecraftVersion));
					parameters.getJar().set(files.getIntermediaryClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set("named");
					parameters.getNestsFile().set(files.getIntermediaryClientNests(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getServerIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupServerIntermediaryMappings(minecraftVersion));
					parameters.getJar().set(files.getServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set("intermediary");
					parameters.getNestsFile().set(files.getServerNests(minecraftVersion));
				});
				workQueue.submit(MakeSetupMappings.class, parameters -> {
					parameters.getInputMappings().set(files.getFeatherMappings(minecraftVersion.server().id()));
					parameters.getOutputMappings().set(files.getSetupServerNamedMappings(minecraftVersion));
					parameters.getJar().set(files.getIntermediaryServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getTargetNamespace().set("named");
					parameters.getNestsFile().set(files.getIntermediaryServerNests(minecraftVersion));
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
