package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MapProcessedMinecraftTask extends MappingTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(MapJar.class, parameters -> {
			parameters.getOverwrite().set(keratin.isCacheInvalid());
			parameters.getInput().set(files.getMainProcessedIntermediaryJar(minecraftVersion));
			parameters.getOutput().set(files.getProcessedNamedJar(minecraftVersion.id()));
			parameters.getMappings().set(files.getProcessedNamedMappings(minecraftVersion));
			parameters.getLibraries().set(files.getLibraries(minecraftVersion));
			parameters.getSourceNamespace().set(srcNs);
			parameters.getTargetNamespace().set(dstNs);
		});
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case NAMED -> INTERMEDIARY.equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map processed Minecraft from " + srcNs + " to " + dstNs);
		}
	}
}
