package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MapNestsTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		int clientBuild = keratin.getNestsBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getNestsBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getNestsBuild(minecraftVersion, GameSide.MERGED);

		if ((details.client() && details.server()) && (!fromOfficial || details.sharedMappings())) {
			if (details.sharedMappings() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getInput().set(fromOfficial ? files.getMergedNests(minecraftVersion) : files.getIntermediaryMergedNests(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedNests(minecraftVersion) : files.getNamedNests(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (details.client() && (details.sharedMappings() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getInput().set(fromOfficial ? files.getClientNests(minecraftVersion) : files.getIntermediaryClientNests(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientNests(minecraftVersion) : files.getNamedNests(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (details.server() && (details.sharedMappings() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapNests.class, parameters -> {
					parameters.getInput().set(fromOfficial ? files.getServerNests(minecraftVersion) : files.getIntermediaryServerNests(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerNests(minecraftVersion) : files.getNamedNests(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
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
			throw new IllegalStateException("cannot map Nests from " + srcNs + " to " + dstNs);
		}
	}
}
