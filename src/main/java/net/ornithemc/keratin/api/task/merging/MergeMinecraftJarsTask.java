package net.ornithemc.keratin.api.task.merging;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class MergeMinecraftJarsTask extends MergeTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String namespace = getNamespace().get();

		validateNamespace(namespace);

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GameJarsCache gameJars = files.getGlobalCache().getGameJarsCache();
		MappedJarsCache mappedJars = files.getGlobalCache().getMappedJarsCache();

		if (minecraftVersion.canBeMerged()) {
			boolean official = Mapper.OFFICIAL.equals(namespace);

			if (official == minecraftVersion.hasSharedObfuscation()) {
				workQueue.submit(MergeJars.class, parameters -> {
					parameters.getOverwrite().set(keratin.isCacheInvalid());
					parameters.getClient().set(official ? gameJars.getClientJar(minecraftVersion) : mappedJars.getIntermediaryClientJar(minecraftVersion));
					parameters.getServer().set(official ? gameJars.getServerJar(minecraftVersion) : mappedJars.getIntermediaryServerJar(minecraftVersion));
					parameters.getMerged().set(official ? gameJars.getMergedJar(minecraftVersion) : mappedJars.getIntermediaryMergedJar(minecraftVersion));
				});
			}
		}
	}

	private static void validateNamespace(String namespace) {
		if (!Mapper.OFFICIAL.equals(namespace) && !Mapper.INTERMEDIARY.equals(namespace)) {
			throw new IllegalStateException("cannot merge Minecraft jars in the " + namespace + " namespace");
		}
	}
}
