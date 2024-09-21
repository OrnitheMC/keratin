package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MapSourceJarsTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(files.getSourceMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getNamedSourceMergedJar(minecraftVersion));
				parameters.getMappings().set(files.getSourceMergedMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set("official");
				parameters.getTargetNamespace().set("named");
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getSourceClientJar(minecraftVersion));
					parameters.getOutput().set(files.getNamedSourceClientJar(minecraftVersion));
					parameters.getMappings().set(files.getSourceClientMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("named");
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getSourceServerJar(minecraftVersion));
					parameters.getOutput().set(files.getNamedSourceServerJar(minecraftVersion));
					parameters.getMappings().set(files.getSourceServerMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set("official");
					parameters.getTargetNamespace().set("named");
				});
			}
		}
	}
}
