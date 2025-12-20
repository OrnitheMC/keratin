package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class MapNestsTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();
		NestsCache nests = files.getGlobalCache().getNestsCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		BuildNumbers builds = keratin.getNestsBuilds(minecraftVersion);

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		if (fromOfficial ? minecraftVersion.canBeMergedAsObfuscated() : minecraftVersion.canBeMerged()) {
			if (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.client() > 0 || builds.server() > 0)) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? nests.getMergedNestsFile(minecraftVersion, builds) : nests.getIntermediaryMergedNestsFile(minecraftVersion, builds));
					parameters.getOutput().set(fromOfficial ? nests.getIntermediaryMergedNestsFile(minecraftVersion, builds) : buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? mappings.getFilledMergedIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.client() > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? nests.getClientNestsFile(minecraftVersion, builds) : nests.getIntermediaryClientNestsFile(minecraftVersion, builds));
					parameters.getOutput().set(fromOfficial ? nests.getIntermediaryClientNestsFile(minecraftVersion, builds) : buildFiles.getNamedNestsFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? mappings.getFilledClientIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.server() > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? nests.getServerNestsFile(minecraftVersion, builds) : nests.getIntermediaryServerNestsFile(minecraftVersion, builds));
					parameters.getOutput().set(fromOfficial ? nests.getIntermediaryServerNestsFile(minecraftVersion, builds) : buildFiles.getNamedNestsFile(minecraftVersion));
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
