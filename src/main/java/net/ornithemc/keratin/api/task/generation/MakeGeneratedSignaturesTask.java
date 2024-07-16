package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;
import net.ornithemc.mappingutils.MappingUtils;

public abstract class MakeGeneratedSignaturesTask extends MinecraftTask implements SignaturePatcher {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			File sigs = files.getGeneratedMergedSignatures(minecraftVersion);
			File nests = files.getMergedNests(minecraftVersion);

			extractSignatures(
				files.getMergedJar(minecraftVersion),
				sigs
			);
			if (nests != null) {
				MappingUtils.undoNestsToSignatures(
					sigs.toPath(),
					sigs.toPath(),
					nests.toPath()
				);
			}
		} else {
			if (details.client()) {
				File sigs = files.getGeneratedClientSignatures(minecraftVersion);
				File nests = files.getClientNests(minecraftVersion);

				extractSignatures(
					files.getClientJar(minecraftVersion),
					sigs
				);
				if (nests != null) {
					MappingUtils.undoNestsToSignatures(
						sigs.toPath(),
						sigs.toPath(),
						nests.toPath()
					);
				}
			}
			if (details.server()) {
				File sigs = files.getGeneratedServerSignatures(minecraftVersion);
				File nests = files.getServerNests(minecraftVersion);

				extractSignatures(
					files.getServerJar(minecraftVersion),
					sigs
				);
				if (nests != null) {
					MappingUtils.undoNestsToSignatures(
						sigs.toPath(),
						sigs.toPath(),
						nests.toPath()
					);
				}
			}
		}
	}
}
