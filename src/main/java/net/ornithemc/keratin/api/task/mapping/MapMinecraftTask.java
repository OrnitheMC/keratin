package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MapMinecraftTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		if (fromOfficial ? details.sharedMappings() : (details.client() && details.server())) {
			workQueue.submit(MapJar.class, parameters -> {
				parameters.getInput().set(fromOfficial ? files.getMergedJar(minecraftVersion) : files.getIntermediaryMergedJar(minecraftVersion));
				parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedJar(minecraftVersion) : files.getNamedJar(minecraftVersion));
				parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				parameters.getLibraries().set(files.getLibraries(minecraftVersion));
				parameters.getSourceNamespace().set(srcNs);
				parameters.getTargetNamespace().set(dstNs);
			});
		} else {
			if (details.client()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(fromOfficial ? files.getClientJar(minecraftVersion) : files.getIntermediaryClientJar(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientJar(minecraftVersion) : files.getNamedJar(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
			if (details.server()) {
				workQueue.submit(MapJar.class, parameters -> {
					parameters.getInput().set(fromOfficial ? files.getServerJar(minecraftVersion) : files.getIntermediaryServerJar(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerJar(minecraftVersion) : files.getNamedJar(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
					parameters.getLibraries().set(files.getLibraries(minecraftVersion));
					parameters.getSourceNamespace().set(srcNs);
					parameters.getTargetNamespace().set(dstNs);
				});
			}
		}
	}

	private static void validateNamespaces(String srcNs, String dstNs) {
		boolean valid = switch (dstNs) {
			case "intermediary" -> "official".equals(srcNs);
			case "named" -> "intermediary".equals(srcNs);
			default -> false;
		};
		if (!valid) {
			throw new IllegalStateException("cannot map Minecraft from " + srcNs + " to " + dstNs);
		}
	}
}
