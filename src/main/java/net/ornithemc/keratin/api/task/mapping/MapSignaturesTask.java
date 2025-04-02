package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class MapSignaturesTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		int clientBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.hasSharedObfuscation())) {
			if (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getMergedSignaturesFile(minecraftVersion) : files.getIntermediaryMergedSignaturesFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedSignaturesFile(minecraftVersion) : files.getNamedSignaturesFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getFilledMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getClientSignaturesFile(minecraftVersion) : files.getIntermediaryClientSignaturesFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientSignaturesFile(minecraftVersion) : files.getNamedSignaturesFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getFilledClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getServerSignaturesFile(minecraftVersion) : files.getIntermediaryServerSignaturesFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerSignaturesFile(minecraftVersion) : files.getNamedSignaturesFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getFilledServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
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
			throw new IllegalStateException("cannot map signatures from " + srcNs + " to " + dstNs);
		}
	}
}
