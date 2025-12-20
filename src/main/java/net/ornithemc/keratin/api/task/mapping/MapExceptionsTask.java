package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.files.GlobalCache.ExceptionsCache;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MapExceptionsTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();
		ExceptionsCache exceptions = files.getGlobalCache().getExceptionsCache();

		BuildNumbers builds = keratin.getExceptionsBuilds(minecraftVersion);

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		if (fromOfficial ? minecraftVersion.canBeMergedAsObfuscated() : minecraftVersion.canBeMerged()) {
			if (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.client() > 0 || builds.server() > 0)) {
				workQueue.submit(MapExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(exceptions.getMergedExceptionsFile(minecraftVersion, builds));
					parameters.getOutput().set(exceptions.getIntermediaryMergedExceptionsFile(minecraftVersion, builds));
					parameters.getMappings().set(mappings.getFilledMergedIntermediaryMappingsFile(minecraftVersion));
				});
			}
		} else {
			if (minecraftVersion.hasClient() && (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.client() > 0))) {
				workQueue.submit(MapExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(exceptions.getClientExceptionsFile(minecraftVersion, builds));
					parameters.getOutput().set(exceptions.getIntermediaryClientExceptionsFile(minecraftVersion, builds));
					parameters.getMappings().set(mappings.getFilledClientIntermediaryMappingsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer() && (minecraftVersion.hasSharedObfuscation() ? (builds.merged() > 0) : (builds.server() > 0))) {
				workQueue.submit(MapExceptions.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getBrokenInnerClasses().set(fromOfficial && minecraftVersion.hasBrokenInnerClasses());
					parameters.getInput().set(exceptions.getServerExceptionsFile(minecraftVersion, builds));
					parameters.getOutput().set(exceptions.getIntermediaryServerExceptionsFile(minecraftVersion, builds));
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
			throw new IllegalStateException("cannot map exceptions from " + srcNs + " to " + dstNs);
		}
	}
}
