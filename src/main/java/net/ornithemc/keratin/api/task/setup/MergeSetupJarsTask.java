package net.ornithemc.keratin.api.task.setup;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MergeSetupJarsTask extends MinecraftTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.sharedMappings() && details.client() && details.server()) {
			workQueue.submit(MergeJars.class, parameters -> {
				parameters.getClient().set(files.getNamedSetupClientJar(minecraftVersion));
				parameters.getServer().set(files.getNamedSetupServerJar(minecraftVersion));
				parameters.getMerged().set(files.getNamedSetupMergedJar(minecraftVersion));
			});
		}
	}
}
