package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class PatchSetupMappingsTask extends MinecraftTask implements MappingsPatcher {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			workQueue.submit(PatchMappings.class, parameters -> {
				parameters.getJar().set(files.getMergedJar(minecraftVersion));
				parameters.getMappings().set(files.getSetupMergedIntermediaryMappings(minecraftVersion));
				parameters.getTargetNamespace().set("intermediary");
				parameters.getNests().set(files.getMergedNests(minecraftVersion));
			});
			workQueue.submit(PatchMappings.class, parameters -> {
				parameters.getJar().set(files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getMappings().set(files.getSetupMergedNamedMappings(minecraftVersion));
				parameters.getTargetNamespace().set("named");
				parameters.getNests().set(files.getIntermediaryMergedNests(minecraftVersion));
			});
		} else {
			if (details.client()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getJar().set(files.getClientJar(minecraftVersion));
					parameters.getMappings().set(files.getSetupClientIntermediaryMappings(minecraftVersion));
					parameters.getTargetNamespace().set("intermediary");
					parameters.getNests().set(files.getClientNests(minecraftVersion));
				});
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getJar().set(files.getIntermediaryClientJar(minecraftVersion));
					parameters.getMappings().set(files.getSetupClientNamedMappings(minecraftVersion));
					parameters.getTargetNamespace().set("named");
					parameters.getNests().set(files.getIntermediaryClientNests(minecraftVersion));
				});
			}
			if (details.server()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getJar().set(files.getServerJar(minecraftVersion));
					parameters.getMappings().set(files.getSetupServerIntermediaryMappings(minecraftVersion));
					parameters.getTargetNamespace().set("intermediary");
					parameters.getNests().set(files.getServerNests(minecraftVersion));
				});
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getJar().set(files.getIntermediaryServerJar(minecraftVersion));
					parameters.getMappings().set(files.getSetupServerNamedMappings(minecraftVersion));
					parameters.getTargetNamespace().set("named");
					parameters.getNests().set(files.getIntermediaryServerNests(minecraftVersion));
				});
			}
		}
	}
}
