package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MapGeneratedJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(files.getNamedGeneratedMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getGeneratedMergedJar(minecraftVersion));
				parameters.getMappings().set(files.getCombinedSetupMergedMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set("named");
				parameters.getTargetNamespace().set("official");
			});
		} else {
			if (details.client()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getNamedGeneratedClientJar(minecraftVersion));
					parameters.getOutput().set(files.getGeneratedClientJar(minecraftVersion));
					parameters.getMappings().set(files.getCombinedSetupClientMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("named");
					parameters.getTargetNamespace().set("official");
				});
			}
			if (details.server()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getNamedGeneratedServerJar(minecraftVersion));
					parameters.getOutput().set(files.getGeneratedServerJar(minecraftVersion));
					parameters.getMappings().set(files.getCombinedSetupServerMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("named");
					parameters.getTargetNamespace().set("official");
				});
			}
		}
	}
}
