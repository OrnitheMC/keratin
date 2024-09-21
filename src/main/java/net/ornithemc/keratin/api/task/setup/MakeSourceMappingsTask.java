package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class MakeSourceMappingsTask extends MinecraftTask implements MappingsPatcher {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(PatchMappings.class, parameters -> {
				parameters.getIntermediaryMappings().set(files.getSetupMergedIntermediaryMappings(minecraftVersion));
				parameters.getNamedMappings().set(files.getSetupMergedNamedMappings(minecraftVersion));
				parameters.getMappings().set(files.getSourceMergedMappings(minecraftVersion));
				parameters.getJar().set(files.getSetupMergedJar(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getIntermediaryMappings().set(files.getSetupClientIntermediaryMappings(minecraftVersion));
					parameters.getNamedMappings().set(files.getSetupClientNamedMappings(minecraftVersion));
					parameters.getMappings().set(files.getSourceClientMappings(minecraftVersion));
					parameters.getJar().set(files.getSetupClientJar(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(PatchMappings.class, parameters -> {
					parameters.getIntermediaryMappings().set(files.getSetupServerIntermediaryMappings(minecraftVersion));
					parameters.getNamedMappings().set(files.getSetupServerNamedMappings(minecraftVersion));
					parameters.getMappings().set(files.getSourceServerMappings(minecraftVersion));
					parameters.getJar().set(files.getSetupServerJar(minecraftVersion));
				});
			}
		}
	}
}
