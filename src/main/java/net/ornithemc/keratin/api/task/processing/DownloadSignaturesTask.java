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

public abstract class DownloadSignaturesTask extends MinecraftTask implements DownloaderAndExtracter {

	private static final String PATH_IN_JAR = "signatures/mappings.sigs";

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		MultipleBuildsMavenArtifacts signatures = keratin.getSignaturesArtifacts();

		boolean signaturesChanged = false;

		if (minecraftVersion.hasSharedObfuscation()) {
			int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.MERGED);

			if (build > 0) {
				File outputJar = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedSignaturesJar(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientSignaturesJar(minecraftVersion)
						: files.getServerSignaturesJar(minecraftVersion);
				File output = minecraftVersion.hasClient() && minecraftVersion.hasServer()
					? files.getMergedSignaturesFile(minecraftVersion)
					: minecraftVersion.hasClient()
						? files.getClientSignaturesFile(minecraftVersion)
						: files.getServerSignaturesFile(minecraftVersion);

				signaturesChanged |= downloadAndExtract(
					signatures.get(minecraftVersion.key(GameSide.MERGED), build),
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
						signatures.get(minecraftVersion.key(GameSide.CLIENT), build),
						PATH_IN_JAR,
						files.getClientSignaturesJar(minecraftVersion),
						files.getClientSignaturesFile(minecraftVersion),
						keratin.isCacheInvalid()
					);
				}
			}
			if (minecraftVersion.hasServer()) {
				int build = keratin.getSignaturesBuild(minecraftVersion, GameSide.SERVER);

				if (build > 0) {
					signaturesChanged |= downloadAndExtract(
						signatures.get(minecraftVersion.key(GameSide.SERVER), build),
						PATH_IN_JAR,
						files.getServerSignaturesJar(minecraftVersion),
						files.getServerSignaturesFile(minecraftVersion),
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
