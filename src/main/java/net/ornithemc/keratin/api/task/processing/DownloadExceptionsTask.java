package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.ExceptionsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class DownloadExceptionsTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "exceptions/mappings.excs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();
		MultipleBuildsMavenArtifacts exceptionsArtifacts = keratin.getExceptionsArtifacts();

		ExceptionsCache exceptions = files.getGlobalCache().getExceptionsCache();

		boolean exceptionsChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File outputJar = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? exceptions.getMergedExceptionsJar(minecraftVersion)
					: minecraftVersion.hasClient()
						? exceptions.getClientExceptionsJar(minecraftVersion)
						: exceptions.getServerExceptionsJar(minecraftVersion);
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? exceptions.getMergedExceptionsFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? exceptions.getClientExceptionsFile(minecraftVersion)
						: exceptions.getServerExceptionsFile(minecraftVersion);

				exceptionsChanged |= downloadAndExtract(
					exceptionsArtifacts.get(minecraftVersion.key(GameSide.MERGED), build),
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
						exceptionsArtifacts.get(minecraftVersion.key(GameSide.CLIENT), build),
						PATH_IN_JAR,
						exceptions.getClientExceptionsJar(minecraftVersion),
						exceptions.getClientExceptionsFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getExceptionsBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					exceptionsChanged |= downloadAndExtract(
						exceptionsArtifacts.get(minecraftVersion.key(GameSide.SERVER), build),
						PATH_IN_JAR,
						exceptions.getServerExceptionsJar(minecraftVersion),
						exceptions.getServerExceptionsFile(minecraftVersion),
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
