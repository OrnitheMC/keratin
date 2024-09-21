package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.mappingutils.MappingUtils;

public abstract class MakeGeneratedExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			File jar = files.getGeneratedMergedJar(minecraftVersion);
			File excs = files.getGeneratedMergedExceptions(minecraftVersion);
			File nests = files.getMergedNests(minecraftVersion);

			extractExceptions(
				jar,
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
			if (minecraftVersion.hasClient()) {
				File jar = files.getGeneratedClientJar(minecraftVersion);
				File excs = files.getGeneratedClientExceptions(minecraftVersion);
				File nests = files.getClientNests(minecraftVersion);

				extractExceptions(
					jar,
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
			if (minecraftVersion.hasServer()) {
				File jar = files.getGeneratedServerJar(minecraftVersion);
				File excs = files.getGeneratedServerExceptions(minecraftVersion);
				File nests = files.getServerNests(minecraftVersion);

				extractExceptions(
					jar,
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
