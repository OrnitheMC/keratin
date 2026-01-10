package net.ornithemc.keratin.api.task.generation;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class SplitGeneratedJarTask extends MinecraftTask implements JarSplitter {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		BuildFiles buildFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.canBeMergedAsMapped()) {
			workQueue.submit(SplitJar.class, parameters -> {
				parameters.getClient().set(buildFiles.getNamedGeneratedClientJar(minecraftVersion));
				parameters.getServer().set(buildFiles.getNamedGeneratedServerJar(minecraftVersion));
				parameters.getMerged().set(buildFiles.getNamedGeneratedMergedJar(minecraftVersion));
			});
		}
	}
}
