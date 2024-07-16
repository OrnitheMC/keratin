package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;

public abstract class MakeBaseExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings()) {
			extractExceptions(
				files.getMergedJar(minecraftVersion),
				files.getBaseMergedExceptions(minecraftVersion)
			);
		} else {
			if (details.client()) {
				extractExceptions(
					files.getClientJar(minecraftVersion),
					files.getBaseClientExceptions(minecraftVersion)
				);
			}
			if (details.server()) {
				extractExceptions(
					files.getServerJar(minecraftVersion),
					files.getBaseServerExceptions(minecraftVersion)
				);
			}
		}
	}
}
