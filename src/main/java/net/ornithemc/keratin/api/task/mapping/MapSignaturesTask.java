package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.GlobalCache.SignaturesCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MapSignaturesTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();
		SignaturesCache signatures = files.getGlobalCache().getSignaturesCache();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		BuildNumbers builds = keratin.getSignaturesBuilds(minecraftVersion);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.hasSharedObfuscation())) {
			if (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.client() > 0 || builds.server() > 0)) {
				workQueue.submit(MapSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(signatures.getMergedSignaturesFile(minecraftVersion, builds));
					parameters.getOutput().set(signatures.getIntermediaryMergedSignaturesFile(minecraftVersion, builds));
					parameters.getMappings().set(mappings.getFilledMergedIntermediaryMappingsFile(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.client() > 0))) {
				workQueue.submit(MapSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(signatures.getClientSignaturesFile(minecraftVersion, builds));
					parameters.getOutput().set(signatures.getIntermediaryClientSignaturesFile(minecraftVersion, builds));
					parameters.getMappings().set(mappings.getFilledClientIntermediaryMappingsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.server() > 0))) {
				workQueue.submit(MapSignatures.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(signatures.getServerSignaturesFile(minecraftVersion, builds));
					parameters.getOutput().set(signatures.getIntermediaryServerSignaturesFile(minecraftVersion, builds));
					parameters.getMappings().set(mappings.getFilledServerIntermediaryMappingsFile(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case INTERMEDIARY -> OFFICIAL.equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map signatures from " + srcNs + " to " + dstNs);
		}
	}
}
