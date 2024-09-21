package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class SplitGeneratedJarTask extends MinecraftTask implements JarSplitter {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.hasSharedObfuscation() && minecraftVersion.canBeMerged()) {
			workQueue.submit(SplitJar.class, parameters -> {
				parameters.getClient().set(files.getNamedGeneratedClientJar(minecraftVersion));
				parameters.getServer().set(files.getNamedGeneratedServerJar(minecraftVersion));
				parameters.getMerged().set(files.getNamedGeneratedMergedJar(minecraftVersion));
			});
		}
	}
}
