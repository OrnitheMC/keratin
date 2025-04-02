package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class MapMinecraftTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.canBeMergedAsObfuscated())) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getInput().set(fromOfficial ? files.getMergedJar(minecraftVersion) : files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedJar(minecraftVersion) : files.getNamedJar(minecraftVersion.id()));
				parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(srcNs);
				parameters.getTargetNamespace().set(dstNs);
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInput().set(fromOfficial ? files.getClientJar(minecraftVersion) : files.getIntermediaryClientJar(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientJar(minecraftVersion) : files.getNamedJar(minecraftVersion.client().id()));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion.client().id()));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInput().set(fromOfficial ? files.getServerJar(minecraftVersion) : files.getIntermediaryServerJar(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerJar(minecraftVersion) : files.getNamedJar(minecraftVersion.server().id()));
					parameters.getMappings().set(fromOfficial ? files.getServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion.server().id()));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
		}
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case INTERMEDIARY -> OFFICIAL.equals(srcNs);
			case NAMED -> INTERMEDIARY.equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map Minecraft from " + srcNs + " to " + dstNs);
		}
	}
}
