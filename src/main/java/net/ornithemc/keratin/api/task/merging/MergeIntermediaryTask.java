package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class MergeIntermediaryTask extends MinecraftTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.canBeMergedLikeAlpha()) {
			workQueue.submit(MergeIntermediary.class, parameters -> {
				parameters.getClient().set(files.getClientIntermediaryMappings(minecraftVersion));
				parameters.getServer().set(files.getServerIntermediaryMappings(minecraftVersion));
				parameters.getMerged().set(files.getMergedIntermediaryMappings(minecraftVersion));
			});
		}
	}
}
