package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;

public abstract class MakeBaseSignaturesTask extends MinecraftTask implements SignaturePatcher {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			extractSignatures(
				files.getMergedJar(minecraftVersion),
				files.getBaseMergedSignatures(minecraftVersion)
			);
		} else {
			if (details.client()) {
				extractSignatures(
					files.getClientJar(minecraftVersion),
					files.getBaseClientSignatures(minecraftVersion)
				);
			}
			if (details.server()) {
				extractSignatures(
					files.getServerJar(minecraftVersion),
					files.getBaseServerSignatures(minecraftVersion)
				);
			}
		}
	}
}
