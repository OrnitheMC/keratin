package net.ornithemc.keratin.api.task.javadoc;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;

public abstract class MapMinecraftForJavadocTask extends MinecraftTask implements Mapper {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedVersioning() && minecraftVersion.canBeMerged()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getNamedJar(minecraftVersion.id()));
				parameters.getMappings().set(files.getNamedMappings(minecraftVersion));
				parameters.getLibraries().addAll(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(INTERMEDIARY);
				parameters.getTargetNamespace().set(NAMED);
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getIntermediaryClientJar(minecraftVersion));
					parameters.getOutput().set(files.getNamedJar(minecraftVersion.client().id()));
					parameters.getMappings().set(files.getNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion.client().id()));
					parameters.getSourceNamespace().set(INTERMEDIARY);
					parameters.getTargetNamespace().set(NAMED);
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getIntermediaryServerJar(minecraftVersion));
					parameters.getOutput().set(files.getNamedJar(minecraftVersion.server().id()));
					parameters.getMappings().set(files.getNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion.server().id()));
					parameters.getSourceNamespace().set(INTERMEDIARY);
					parameters.getTargetNamespace().set(NAMED);
				});
			}
		}
	}
}
