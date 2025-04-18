package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.DownloaderAndExtractor;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class DownloadNestsTask extends MinecraftTask implements DownloaderAndExtractor {

	private static final String PATH_IN_JAR = "nests/mappings.nest";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();
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
					false
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
						false
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
						false
					);
				}
			}
		}

		if (nestsChanged) {
			keratin.invalidateCache();
		}
	}
}
