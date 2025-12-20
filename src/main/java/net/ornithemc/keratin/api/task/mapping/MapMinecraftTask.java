package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class MapMinecraftTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		GameJarsCache gameJars = globalCache.getGameJarsCache();
		MappedJarsCache mappedJars = globalCache.getMappedJarsCache();
		MappingsCache mappings = globalCache.getMappingsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		boolean fromOfficial = OFFICIAL.equals(srcNs);

		if (fromOfficial ? minecraftVersion.canBeMergedAsObfuscated() : minecraftVersion.canBeMerged()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getInput().set(fromOfficial ? gameJars.getMergedJar(minecraftVersion) : mappedJars.getIntermediaryMergedJar(minecraftVersion));
				parameters.getOutput().set(fromOfficial ? mappedJars.getIntermediaryMergedJar(minecraftVersion) : buildFiles.getNamedJar(minecraftVersion.id()));
				parameters.getMappings().set(fromOfficial ? mappings.getMergedIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(srcNs);
				parameters.getTargetNamespace().set(dstNs);
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInput().set(fromOfficial ? gameJars.getClientJar(minecraftVersion) : mappedJars.getIntermediaryClientJar(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? mappedJars.getIntermediaryClientJar(minecraftVersion) : buildFiles.getNamedJar(minecraftVersion.client().id()));
					parameters.getMappings().set(fromOfficial ? mappings.getClientIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.client().id()));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getInput().set(fromOfficial ? gameJars.getServerJar(minecraftVersion) : mappedJars.getIntermediaryServerJar(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? mappedJars.getIntermediaryServerJar(minecraftVersion) : buildFiles.getNamedJar(minecraftVersion.server().id()));
					parameters.getMappings().set(fromOfficial ? mappings.getServerIntermediaryMappingsFile(minecraftVersion) : buildFiles.getMappingsFile(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.server().id()));
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
