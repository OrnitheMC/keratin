package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
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

		boolean nestsChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File outputJar = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? nests.getMergedNestsJar(minecraftVersion)
					: minecraftVersion.hasClient()
						? nests.getClientNestsJar(minecraftVersion)
						: nests.getServerNestsJar(minecraftVersion);
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? nests.getMergedNestsFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? nests.getClientNestsFile(minecraftVersion)
						: nests.getServerNestsFile(minecraftVersion);

				nestsChanged |= downloadAndExtract(
					nestsArtifacts.get(minecraftVersion.key(GameSide.MERGED), build),
					PATH_IN_JAR,
					outputJar,
					output,
					keratin.isCacheInvalid()
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);

				if (build > 0) {
					nestsChanged |= downloadAndExtract(
						nestsArtifacts.get(minecraftVersion.key(GameSide.CLIENT), build),
						PATH_IN_JAR,
						nests.getClientNestsJar(minecraftVersion),
						nests.getClientNestsFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					nestsChanged |= downloadAndExtract(
						nestsArtifacts.get(minecraftVersion.key(GameSide.SERVER), build),
						PATH_IN_JAR,
						nests.getServerNestsJar(minecraftVersion),
						nests.getServerNestsFile(minecraftVersion),
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
