package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class MakeSetupMappingsTask extends MinecraftTask implements MappingsFiller {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			workQueue.submit(FillMappings.class, parameters -> {
				parameters.getJar().set(files.getMergedJar(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getInputMappings().set(files.getMergedIntermediaryMappings(minecraftVersion));
				parameters.getOutputMappings().set(files.getSetupMergedIntermediaryMappings(minecraftVersion));
				parameters.getTargetNamespace().set("intermediary");
			});
			workQueue.submit(FillMappings.class, parameters -> {
				parameters.getJar().set(files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getInputMappings().set(files.getFeatherMappings(minecraftVersion));
				parameters.getOutputMappings().set(files.getSetupMergedNamedMappings(minecraftVersion));
				parameters.getTargetNamespace().set("named");
			});
		} else {
			if (details.client()) {
				workQueue.submit(FillMappings.class, parameters -> {
					parameters.getJar().set(files.getClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getInputMappings().set(files.getClientIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupClientIntermediaryMappings(minecraftVersion));
					parameters.getTargetNamespace().set("intermediary");
				});
				workQueue.submit(FillMappings.class, parameters -> {
					parameters.getJar().set(files.getIntermediaryClientJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getInputMappings().set(files.getFeatherMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupClientNamedMappings(minecraftVersion));
					parameters.getTargetNamespace().set("named");
				});
			}
			if (details.server()) {
				workQueue.submit(FillMappings.class, parameters -> {
					parameters.getJar().set(files.getServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getInputMappings().set(files.getServerIntermediaryMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupServerIntermediaryMappings(minecraftVersion));
					parameters.getTargetNamespace().set("intermediary");
				});
				workQueue.submit(FillMappings.class, parameters -> {
					parameters.getJar().set(files.getIntermediaryServerJar(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getInputMappings().set(files.getFeatherMappings(minecraftVersion));
					parameters.getOutputMappings().set(files.getSetupServerNamedMappings(minecraftVersion));
					parameters.getTargetNamespace().set("named");
				});
			}
		}
	}
}
