package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Exceptor;

public abstract class MakeBaseExceptionsTask extends MinecraftTask implements Exceptor {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			extractExceptions(
				files.getMergedJar(minecraftVersion),
				files.getBaseMergedExceptions(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				extractExceptions(
					files.getClientJar(minecraftVersion),
					files.getBaseClientExceptions(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				extractExceptions(
					files.getServerJar(minecraftVersion),
					files.getBaseServerExceptions(minecraftVersion)
				);
			}
		}
	}
}
