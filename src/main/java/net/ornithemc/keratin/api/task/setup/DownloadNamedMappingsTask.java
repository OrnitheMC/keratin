package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadNamedMappingsTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "mappings/mappings.tiny";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		boolean namedMappingsChanged = false;

		if (minecraftVersion.hasSharedVersioning()) {
			namedMappingsChanged |= downloadNamedMappings(minecraftVersion.id());
		} else {
			if (minecraftVersion.hasClient()) {
				namedMappingsChanged |= downloadNamedMappings(minecraftVersion.client().id());
			}
			if (minecraftVersion.hasServer()) {
				namedMappingsChanged |= downloadNamedMappings(minecraftVersion.server().id());
			}
		}

		if (namedMappingsChanged) {
			getExtension().invalidateCache();
		}
	}

	private boolean downloadNamedMappings(String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		MultipleBuildsMavenArtifacts namedMappings = keratin.getNamedMappingsArtifacts();

		int build = keratin.getNamedMappingsBuild(minecraftVersion);

		if (build > 0) {
			return downloadAndExtract(
				namedMappings.get(minecraftVersion, build),
				PATH_IN_JAR,
				files.getNamedMappingsJar(minecraftVersion),
				files.getNamedMappingsFile(minecraftVersion),
				keratin.isCacheInvalid()
			);
		}

		return false;
	}
}
