package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class DownloadNestsTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "nests/mappings.nest";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();
		MultipleBuildsMavenArtifacts nestsArtifacts = keratin.getNestsArtifacts();

		NestsCache nests = files.getGlobalCache().getNestsCache();

		BuildNumbers builds = keratin.getNestsBuilds(minecraftVersion);

		boolean nestsChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			if (builds.merged() > 0) {
				nestsChanged |= downloadAndExtract(
					nestsArtifacts.get(minecraftVersion.id(), builds.merged()),
					PATH_IN_JAR,
					nests.getMergedNestsJar(minecraftVersion, builds),
					nests.getMergedNestsFile(minecraftVersion, builds),
					keratin.isCacheInvalid()
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				if (builds.client() > 0) {
					String versionKey = minecraftVersion.hasSharedVersioning()
						? minecraftVersion.clientKey()
						: minecraftVersion.client().id();

					nestsChanged |= downloadAndExtract(
						nestsArtifacts.get(versionKey, builds.client()),
						PATH_IN_JAR,
						nests.getClientNestsJar(minecraftVersion, builds),
						nests.getClientNestsFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				if (builds.server() > 0) {
					String versionKey = minecraftVersion.hasSharedVersioning()
						? minecraftVersion.serverKey()
						: minecraftVersion.server().id();

					nestsChanged |= downloadAndExtract(
						nestsArtifacts.get(versionKey, builds.server()),
						PATH_IN_JAR,
						nests.getServerNestsJar(minecraftVersion, builds),
						nests.getServerNestsFile(minecraftVersion, builds),
						keratin.isCacheInvalid()
					);
				}
			}
		}

		if (nestsChanged) {
			keratin.invalidateCache();
		}
	}
}
