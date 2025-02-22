package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class MapNestsTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.hasSharedObfuscation())) {
			if (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getMergedNests(minecraftVersion) : files.getIntermediaryMergedNests(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedNests(minecraftVersion) : files.getNamedNests(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getClientNests(minecraftVersion) : files.getIntermediaryClientNests(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientNests(minecraftVersion) : files.getNamedNests(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getServerNests(minecraftVersion) : files.getIntermediaryServerNests(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerNests(minecraftVersion) : files.getNamedNests(minecraftVersion));
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
			throw new IllegalStateException("cannot map Nests from " + srcNs + " to " + dstNs);
		}
	}
}
