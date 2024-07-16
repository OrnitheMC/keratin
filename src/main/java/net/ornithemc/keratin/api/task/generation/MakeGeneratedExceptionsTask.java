package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.mappingutils.MappingUtils;

public abstract class MakeGeneratedExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			File excs = files.getGeneratedMergedExceptions(minecraftVersion);
			File nests = files.getMergedNests(minecraftVersion);

			extractExceptions(
				files.getMergedJar(minecraftVersion),
				excs
			);
			if (nests != null) {
				MappingUtils.undoNestsToExceptions(
					excs.toPath(),
					excs.toPath(),
					nests.toPath()
				);
			}
		} else {
			if (details.client()) {
				File excs = files.getGeneratedClientExceptions(minecraftVersion);
				File nests = files.getClientNests(minecraftVersion);

				extractExceptions(
					files.getClientJar(minecraftVersion),
					excs
				);
				if (nests != null) {
					MappingUtils.undoNestsToExceptions(
						excs.toPath(),
						excs.toPath(),
						nests.toPath()
					);
				}
			}
			if (details.server()) {
				File excs = files.getGeneratedServerExceptions(minecraftVersion);
				File nests = files.getServerNests(minecraftVersion);

				extractExceptions(
					files.getServerJar(minecraftVersion),
					excs
				);
				if (nests != null) {
					MappingUtils.undoNestsToExceptions(
						excs.toPath(),
						excs.toPath(),
						nests.toPath()
					);
				}
			}
		}
	}
}
