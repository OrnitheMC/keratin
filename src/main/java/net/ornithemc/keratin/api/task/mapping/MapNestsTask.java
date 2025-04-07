package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MapNestsTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();
		NestsCache nests = files.getGlobalCache().getNestsCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

		if (minecraftVersion.canBeMerged() && (!fromOfficial || minecraftVersion.hasSharedObfuscation())) {
			if (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? nests.getMergedNestsFile(minecraftVersion) : nests.getIntermediaryMergedNestsFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? nests.getIntermediaryMergedNestsFile(minecraftVersion) : buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? mappings.getFilledMergedIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? nests.getClientNestsFile(minecraftVersion) : nests.getIntermediaryClientNestsFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? nests.getIntermediaryClientNestsFile(minecraftVersion) : buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? mappings.getFilledClientIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? nests.getServerNestsFile(minecraftVersion) : nests.getIntermediaryServerNestsFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? nests.getIntermediaryServerNestsFile(minecraftVersion) : buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? mappings.getFilledServerIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
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
			throw new IllegalStateException("cannot map nests from " + srcNs + " to " + dstNs);
		}
	}
}
