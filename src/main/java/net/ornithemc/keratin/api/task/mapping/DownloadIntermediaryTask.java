package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class DownloadIntermediaryTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "mappings/mappings.tiny";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();
		SingleBuildMavenArtifacts intermediary = keratin.getIntermediaryArtifacts();

		MappingsCache mappings = files.getGlobalCache().getMappingsCache();

		boolean intermediaryChanged = false;

		if (minecraftVersion.hasSharedVersioning()) {
			intermediaryChanged |= downloadAndExtract(
				intermediary.get(minecraftVersion.id()),
				PATH_IN_JAR,
				mappings.getMergedIntermediaryMappingsJar(minecraftVersion),
				mappings.getMergedIntermediaryMappingsFile(minecraftVersion),
				keratin.isCacheInvalid()
			);
		} else {
			if (minecraftVersion.hasClient()) {
				intermediaryChanged |= downloadAndExtract(
					intermediary.get(minecraftVersion.client().id()),
					PATH_IN_JAR,
					mappings.getClientIntermediaryMappingsJar(minecraftVersion),
					mappings.getClientIntermediaryMappingsFile(minecraftVersion),
					keratin.isCacheInvalid()
				);
			}
			if (minecraftVersion.hasServer()) {
				intermediaryChanged |= downloadAndExtract(
					intermediary.get(minecraftVersion.server().id()),
					PATH_IN_JAR,
					mappings.getServerIntermediaryMappingsJar(minecraftVersion),
					mappings.getServerIntermediaryMappingsFile(minecraftVersion),
					keratin.isCacheInvalid()
				);
			}
		}

		if (intermediaryChanged) {
			keratin.invalidateCache();
		}
	}
}
