package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.DownloaderAndExtractor;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.ExceptionsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class DownloadExceptionsTask extends MinecraftTask implements DownloaderAndExtractor {

	private static final String PATH_IN_JAR = "exceptions/mappings.excs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();
		MultipleBuildsMavenArtifacts exceptionsArtifacts = keratin.getExceptionsArtifacts();

		ExceptionsCache exceptions = files.getGlobalCache().getExceptionsCache();

		BuildNumbers builds = keratin.getExceptionsBuilds(minecraftVersion);

		boolean exceptionsChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			if (builds.merged() > 0) {
				exceptionsChanged |= downloadAndExtract(
					exceptionsArtifacts.get(minecraftVersion.id(), builds.merged()),
					PATH_IN_JAR,
					exceptions.getMergedExceptionsJar(minecraftVersion, builds),
					exceptions.getMergedExceptionsFile(minecraftVersion, builds),
					false
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				if (builds.client() > 0) {
					String versionKey = minecraftVersion.hasSharedVersioning()
						? minecraftVersion.clientKey()
						: minecraftVersion.client().id();

					exceptionsChanged |= downloadAndExtract(
						exceptionsArtifacts.get(versionKey, builds.client()),
						PATH_IN_JAR,
						exceptions.getClientExceptionsJar(minecraftVersion, builds),
						exceptions.getClientExceptionsFile(minecraftVersion, builds),
						false
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				if (builds.server() > 0) {
					String versionKey = minecraftVersion.hasSharedVersioning()
						? minecraftVersion.serverKey()
						: minecraftVersion.server().id();

					exceptionsChanged |= downloadAndExtract(
						exceptionsArtifacts.get(versionKey, builds.server()),
						PATH_IN_JAR,
						exceptions.getServerExceptionsJar(minecraftVersion, builds),
						exceptions.getServerExceptionsFile(minecraftVersion, builds),
						false
					);
				}
			}
		}

		if (exceptionsChanged) {
			keratin.invalidateCache();
		}
	}
}
