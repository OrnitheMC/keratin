package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MapRavenTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		int clientBuild = keratin.getRavenBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getRavenBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getRavenBuild(minecraftVersion, GameSide.MERGED);

		if ((details.client() && details.server()) && (!fromOfficial || details.sharedMappings())) {
			if (details.sharedMappings() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapRaven.class, parameters -> {
					parameters.getBrokenInnerClasses().set(details.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getMergedRavenFile(minecraftVersion) : files.getIntermediaryMergedRavenFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedRavenFile(minecraftVersion) : files.getNamedRavenFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (details.client() && (details.sharedMappings() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapRaven.class, parameters -> {
					parameters.getBrokenInnerClasses().set(details.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getClientRavenFile(minecraftVersion) : files.getIntermediaryClientRavenFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientRavenFile(minecraftVersion) : files.getNamedRavenFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (details.server() && (details.sharedMappings() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapRaven.class, parameters -> {
					parameters.getBrokenInnerClasses().set(details.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getServerRavenFile(minecraftVersion) : files.getIntermediaryServerRavenFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerRavenFile(minecraftVersion) : files.getNamedRavenFile(minecraftVersion));
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
			throw new IllegalStateException("cannot map Raven from " + srcNs + " to " + dstNs);
		}
	}
}
