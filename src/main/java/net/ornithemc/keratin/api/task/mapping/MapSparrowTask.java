package net.ornithemc.keratin.api.task.mapping;

import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.GameSide;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MapSparrowTask extends MappingTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		int clientBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.CLIENT);
		int serverBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.SERVER);
		int mergedBuild = keratin.getSparrowBuild(minecraftVersion, GameSide.MERGED);

		if ((details.client() && details.server()) && (!fromOfficial || details.sharedMappings())) {
			if (details.sharedMappings() ? (mergedBuild > 0) : (clientBuild > 0 || serverBuild > 0)) {
				workQueue.submit(MapSparrow.class, parameters -> {
					parameters.getBrokenInnerClasses().set(details.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getMergedSparrowFile(minecraftVersion) : files.getIntermediaryMergedSparrowFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryMergedSparrowFile(minecraftVersion) : files.getNamedSparrowFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
		} else {
			if (details.client() && (details.sharedMappings() ? (mergedBuild > 0) : (clientBuild > 0))) {
				workQueue.submit(MapSparrow.class, parameters -> {
					parameters.getBrokenInnerClasses().set(details.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getClientSparrowFile(minecraftVersion) : files.getIntermediaryClientSparrowFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryClientSparrowFile(minecraftVersion) : files.getNamedSparrowFile(minecraftVersion));
					parameters.getMappings().set(fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion));
				});
			}
			if (details.server() && (details.sharedMappings() ? (mergedBuild > 0) : (serverBuild > 0))) {
				workQueue.submit(MapSparrow.class, parameters -> {
					parameters.getBrokenInnerClasses().set(details.hasBrokenInnerClasses());
					parameters.getInput().set(fromOfficial ? files.getServerSparrowFile(minecraftVersion) : files.getIntermediaryServerSparrowFile(minecraftVersion));
					parameters.getOutput().set(fromOfficial ? files.getIntermediaryServerSparrowFile(minecraftVersion) : files.getNamedSparrowFile(minecraftVersion));
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
			throw new IllegalStateException("cannot map Sparrow from " + srcNs + " to " + dstNs);
		}
	}
}
