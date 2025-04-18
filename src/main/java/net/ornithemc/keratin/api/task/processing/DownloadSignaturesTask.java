package net.ornithemc.keratin.api.task.processing;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.DownloaderAndExtractor;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.SignaturesCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class DownloadSignaturesTask extends MinecraftTask implements DownloaderAndExtractor {

	private static final String PATH_IN_JAR = "signatures/mappings.sigs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();
		MultipleBuildsMavenArtifacts signaturesArtifacts = keratin.getSignaturesArtifacts();

		SignaturesCache signatures = files.getGlobalCache().getSignaturesCache();

		BuildNumbers builds = keratin.getSignaturesBuilds(minecraftVersion);

		boolean signaturesChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			if (builds.merged() > 0) {
				signaturesChanged |= downloadAndExtract(
					signaturesArtifacts.get(minecraftVersion.id(), builds.merged()),
					PATH_IN_JAR,
					signatures.getMergedSignaturesJar(minecraftVersion, builds),
					signatures.getMergedSignaturesFile(minecraftVersion, builds),
					false
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				if (builds.client() > 0) {
					String versionKey = minecraftVersion.hasSharedVersioning()
						? minecraftVersion.clientKey()
						: minecraftVersion.client().id();

					signaturesChanged |= downloadAndExtract(
						signaturesArtifacts.get(versionKey, builds.client()),
						PATH_IN_JAR,
						signatures.getClientSignaturesJar(minecraftVersion, builds),
						signatures.getClientSignaturesFile(minecraftVersion, builds),
						false
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				if (builds.server() > 0) {
					String versionKey = minecraftVersion.hasSharedVersioning()
						? minecraftVersion.serverKey()
						: minecraftVersion.server().id();

					signaturesChanged |= downloadAndExtract(
						signaturesArtifacts.get(versionKey, builds.server()),
						PATH_IN_JAR,
						signatures.getServerSignaturesJar(minecraftVersion, builds),
						signatures.getServerSignaturesFile(minecraftVersion, builds),
						false
					);
				}
			}
		}

		if (signaturesChanged) {
			keratin.invalidateCache();
		}
	}
}
