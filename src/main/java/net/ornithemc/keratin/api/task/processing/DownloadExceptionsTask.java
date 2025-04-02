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

public abstract class DownloadExceptionsTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "exceptions/mappings.excs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		MultipleBuildsMavenArtifacts exceptions = keratin.getExceptionsArtifacts();

		boolean exceptionsChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File outputJar = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedExceptionsJar(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientExceptionsJar(minecraftVersion)
						: files.getServerExceptionsJar(minecraftVersion);
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedExceptionsFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientExceptionsFile(minecraftVersion)
						: files.getServerExceptionsFile(minecraftVersion);

				exceptionsChanged |= downloadAndExtract(
					exceptions.get(minecraftVersion.key(GameSide.MERGED), build),
					PATH_IN_JAR,
					outputJar,
					output,
					keratin.isCacheInvalid()
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.CLIENT);

				if (build > 0) {
					exceptionsChanged |= downloadAndExtract(
						exceptions.get(minecraftVersion.key(GameSide.CLIENT), build),
						PATH_IN_JAR,
						files.getClientExceptionsJar(minecraftVersion),
						files.getClientExceptionsFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					exceptionsChanged |= downloadAndExtract(
						exceptions.get(minecraftVersion.key(GameSide.SERVER), build),
						PATH_IN_JAR,
						files.getServerExceptionsJar(minecraftVersion),
						files.getServerExceptionsFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
		}

		if (exceptionsChanged) {
			keratin.invalidateCache();
		}
	}
}
