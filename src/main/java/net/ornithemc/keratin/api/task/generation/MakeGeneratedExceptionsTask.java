package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

import net.ornithemc.mappingutils.MappingUtils;

public abstract class MakeGeneratedExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		NestsCache nestsCache = files.getGlobalCache().getNestsCache();
		BuildFiles buildFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getBuildFiles();

		BuildNumbers nestsBuilds = keratin.getNestsBuilds(minecraftVersion);

		if (minecraftVersion.hasSharedObfuscation()) {
			File jar = buildFiles.getGeneratedMergedJar(minecraftVersion);
			File excs = buildFiles.getGeneratedMergedExceptionsFile(minecraftVersion);
			File nests = nestsCache.getMergedNestsFile(minecraftVersion, nestsBuilds);

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
				File jar = buildFiles.getGeneratedClientJar(minecraftVersion);
				File excs = buildFiles.getGeneratedClientExceptionsFile(minecraftVersion);
				File nests = nestsCache.getClientNestsFile(minecraftVersion, nestsBuilds);

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
				File jar = buildFiles.getGeneratedServerJar(minecraftVersion);
				File excs = buildFiles.getGeneratedServerExceptionsFile(minecraftVersion);
				File nests = nestsCache.getServerNestsFile(minecraftVersion, nestsBuilds);

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
