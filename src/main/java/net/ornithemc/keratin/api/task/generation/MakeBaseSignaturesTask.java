package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MakeBaseSignaturesTask extends MinecraftTask implements SignaturePatcher {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		GameJarsCache gameJars = files.getGlobalCache().getGameJarsCache();
		BuildFiles buildFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			extractSignatures(
				gameJars.getMergedJar(minecraftVersion),
				buildFiles.getBaseMergedSignaturesFile(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				extractSignatures(
					gameJars.getClientJar(minecraftVersion),
					buildFiles.getBaseClientSignaturesFile(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				extractSignatures(
					gameJars.getServerJar(minecraftVersion),
					buildFiles.getBaseServerSignaturesFile(minecraftVersion)
				);
			}
		}
	}
}
