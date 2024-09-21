package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;
import net.ornithemc.mappingutils.MappingUtils;

public abstract class MakeGeneratedSignaturesTask extends MinecraftTask implements SignaturePatcher {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			File jar = files.getGeneratedMergedJar(minecraftVersion);
			File sigs = files.getGeneratedMergedSignatures(minecraftVersion);
			File nests = files.getMergedNests(minecraftVersion);

			extractSignatures(
				jar,
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
			if (minecraftVersion.hasClient()) {
				File jar = files.getGeneratedClientJar(minecraftVersion);
				File sigs = files.getGeneratedClientSignatures(minecraftVersion);
				File nests = files.getClientNests(minecraftVersion);

				extractSignatures(
					jar,
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
			if (minecraftVersion.hasServer()) {
				File jar = files.getGeneratedServerJar(minecraftVersion);
				File sigs = files.getGeneratedServerSignatures(minecraftVersion);
				File nests = files.getServerNests(minecraftVersion);

				extractSignatures(
					jar,
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
