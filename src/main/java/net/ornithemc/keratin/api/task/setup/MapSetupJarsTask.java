package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MapSetupJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(files.getSetupMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getNamedSetupMergedJar(minecraftVersion));
				parameters.getMappings().set(files.getCombinedSetupMergedMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set("official");
				parameters.getTargetNamespace().set("named");
			});
		} else {
			if (details.client()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getSetupClientJar(minecraftVersion));
					parameters.getOutput().set(files.getNamedSetupClientJar(minecraftVersion));
					parameters.getMappings().set(files.getCombinedSetupClientMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("named");
				});
			}
			if (details.server()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getSetupServerJar(minecraftVersion));
					parameters.getOutput().set(files.getNamedSetupServerJar(minecraftVersion));
					parameters.getMappings().set(files.getCombinedSetupServerMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("named");
				});
			}
		}
	}
}
