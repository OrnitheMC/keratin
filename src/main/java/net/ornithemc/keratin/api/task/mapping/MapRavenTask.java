package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class MapRavenTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		int clientBuild = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getRavenBuild(minecraftVersion, GameSide.MERGED);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.hasSharedObfuscation())) {
			if (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapRaven.class, parameters -> {
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getMergedRavenFile(minecraftVersion) : files.getIntermediaryMergedRavenFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedRavenFile(minecraftVersion) : files.getNamedRavenFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapRaven.class, parameters -> {
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getClientRavenFile(minecraftVersion) : files.getIntermediaryClientRavenFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientRavenFile(minecraftVersion) : files.getNamedRavenFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapRaven.class, parameters -> {
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getServerRavenFile(minecraftVersion) : files.getIntermediaryServerRavenFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerRavenFile(minecraftVersion) : files.getNamedRavenFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
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
			throw new IllegalStateException("cannot map Raven from " + srcNs + " to " + dstNs);
		}
	}
}
