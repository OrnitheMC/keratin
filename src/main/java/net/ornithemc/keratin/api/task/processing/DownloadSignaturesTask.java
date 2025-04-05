package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.maven.MultipleBuildsMavenArtifacts;
import net.ornithemc.keratin.api.task.DownloaderAndExtracter;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.SignaturesCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class DownloadSignaturesTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "signatures/mappings.sigs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();
		MultipleBuildsMavenArtifacts signaturesArtifacts = keratin.getSignaturesArtifacts();

		SignaturesCache signatures = files.getGlobalCache().getSignaturesCache();

		boolean signaturesChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File outputJar = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? signatures.getMergedSignaturesJar(minecraftVersion)
					: minecraftVersion.hasClient()
						? signatures.getClientSignaturesJar(minecraftVersion)
						: signatures.getServerSignaturesJar(minecraftVersion);
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? signatures.getMergedSignaturesFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? signatures.getClientSignaturesFile(minecraftVersion)
						: signatures.getServerSignaturesFile(minecraftVersion);

				signaturesChanged |= downloadAndExtract(
					signaturesArtifacts.get(minecraftVersion.key(GameSide.MERGED), build),
					PATH_IN_JAR,
					outputJar,
					output,
					keratin.isCacheInvalid()
				);
			}
		} else {
			if (minecraftVersion.hasClient()) {
				int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.CLIENT);

				if (build > 0) {
					signaturesChanged |= downloadAndExtract(
						signaturesArtifacts.get(minecraftVersion.key(GameSide.CLIENT), build),
						PATH_IN_JAR,
						signatures.getClientSignaturesJar(minecraftVersion),
						signatures.getClientSignaturesFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					signaturesChanged |= downloadAndExtract(
						signaturesArtifacts.get(minecraftVersion.key(GameSide.SERVER), build),
						PATH_IN_JAR,
						signatures.getServerSignaturesJar(minecraftVersion),
						signatures.getServerSignaturesFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
		}

		if (signaturesChanged) {
			keratin.invalidateCache();
		}
	}
}
