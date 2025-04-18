package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.merging.Merger;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceJars;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MergeSourceJarsTask extends MinecraftTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		SourceJars sourceJars = files.getExceptionsAndSignaturesDevelopmentFiles().getSourceJars();

		if (!minecraftVersion.hasSharedObfuscation() && minecraftVersion.canBeMerged()) {
			workQueue.submit(MergeJars.class, parameters -> {
				parameters.getClient().set(sourceJars.getNamedClientJar(minecraftVersion));
				parameters.getServer().set(sourceJars.getNamedServerJar(minecraftVersion));
				parameters.getMerged().set(sourceJars.getNamedMergedJar(minecraftVersion));
			});
		}
	}
}
