package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MapProcessedMinecraftTask extends MappingTask implements Merger {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.client() && details.server()) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(files.getProcessedIntermediaryMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getProcessedNamedJar(minecraftVersion));
				parameters.getMappings().set(files.getProcessedNamedMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(srcNs);
				parameters.getTargetNamespace().set(dstNs);
			});
		} else {
			if (details.client()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getProcessedIntermediaryClientJar(minecraftVersion));
					parameters.getOutput().set(files.getProcessedNamedJar(minecraftVersion));
					parameters.getMappings().set(files.getProcessedNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
			if (details.server()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(files.getProcessedIntermediaryServerJar(minecraftVersion));
					parameters.getOutput().set(files.getProcessedNamedJar(minecraftVersion));
					parameters.getMappings().set(files.getProcessedNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
		}
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case "named" -> "intermediary".equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map processed Minecraft from " + srcNs + " to " + dstNs);
		}
	}
}
