package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;

public abstract class MakeBaseSignaturesTask extends MinecraftTask implements SignaturePatcher {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			extractSignatures(
				files.getMergedJar(minecraftVersion),
				files.getBaseMergedSignatures(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				extractSignatures(
					files.getClientJar(minecraftVersion),
					files.getBaseClientSignatures(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				extractSignatures(
					files.getServerJar(minecraftVersion),
					files.getBaseServerSignatures(minecraftVersion)
				);
			}
		}
	}
}
