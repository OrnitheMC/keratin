package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MapSetupJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(files.getSetupMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getIntermediarySetupMergedJar(minecraftVersion));
				parameters.getMappings().set(files.getSetupMergedIntermediaryMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set("official");
				parameters.getTargetNamespace().set("intermediary");
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getSetupClientJar(minecraftVersion));
					parameters.getOutput().set(files.getIntermediarySetupClientJar(minecraftVersion));
					parameters.getMappings().set(files.getSetupClientIntermediaryMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("intermediary");
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getSetupServerJar(minecraftVersion));
					parameters.getOutput().set(files.getIntermediarySetupServerJar(minecraftVersion));
					parameters.getMappings().set(files.getSetupServerIntermediaryMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("intermediary");
				});
			}
		}
	}
}
