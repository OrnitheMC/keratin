package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class DownloadNestsTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "nests/mappings.nest";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		MultipleBuildsMavenArtifacts nests = keratin.getNestsArtifacts();

		boolean nestsChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File outputJar = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedNestsJar(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientNestsJar(minecraftVersion)
						: files.getServerNestsJar(minecraftVersion);
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedNestsFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientNestsFile(minecraftVersion)
						: files.getServerNestsFile(minecraftVersion);

				nestsChanged |= downloadAndExtract(
					nests.get(minecraftVersion.key(GameSide.MERGED), build),
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
						nests.get(minecraftVersion.key(GameSide.CLIENT), build),
						PATH_IN_JAR,
						files.getClientNestsJar(minecraftVersion),
						files.getClientNestsFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					nestsChanged |= downloadAndExtract(
						nests.get(minecraftVersion.key(GameSide.SERVER), build),
						PATH_IN_JAR,
						files.getServerNestsJar(minecraftVersion),
						files.getServerNestsFile(minecraftVersion),
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
