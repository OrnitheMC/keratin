package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.maven.SingleBuildMavenArtifacts;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadIntermediaryTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "mappings/mappings.tiny";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		SingleBuildMavenArtifacts intermediary = keratin.getIntermediaryArtifacts();

		boolean intermediaryChanged = false;

		if (minecraftVersion.hasSharedVersioning()) {
			intermediaryChanged |= downloadAndExtract(
				intermediary.get(minecraftVersion.id()),
				PATH_IN_JAR,
				files.getMergedIntermediaryMappingsJar(minecraftVersion),
				files.getMergedIntermediaryMappings(minecraftVersion),
				keratin.isCacheInvalid()
			);
		} else {
			if (minecraftVersion.hasClient()) {
				intermediaryChanged |= downloadAndExtract(
					intermediary.get(minecraftVersion.client().id()),
					PATH_IN_JAR,
					files.getClientIntermediaryMappingsJar(minecraftVersion),
					files.getClientIntermediaryMappings(minecraftVersion),
					keratin.isCacheInvalid()
				);
			}
			if (minecraftVersion.hasServer()) {
				intermediaryChanged |= downloadAndExtract(
					intermediary.get(minecraftVersion.server().id()),
					PATH_IN_JAR,
					files.getServerIntermediaryMappingsJar(minecraftVersion),
					files.getServerIntermediaryMappings(minecraftVersion),
					keratin.isCacheInvalid()
				);
			}
		}

		if (intermediaryChanged) {
			keratin.invalidateCache();
		}
	}
}
