package net.ornithemc.keratin.api.task.mapping;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MapMinecraftTask extends MappingTask implements Merger {

	@Override
	public void run(String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		getProject().getLogger().lifecycle(":mapping Minecraft " + minecraftVersion + " from " + srcNs + " to " + dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		if (fromOfficial ? details.sharedMappings() : (details.client() && details.server())) {
			mapJar(
				fromOfficial ? files.getMergedJar(minecraftVersion) : files.getIntermediaryMergedJar(minecraftVersion),
				fromOfficial ? files.getIntermediaryMergedJar(minecraftVersion) : files.getNamedJar(minecraftVersion),
				fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion),
				files.getLibraries(minecraftVersion),
				srcNs,
				dstNs
			);
		} else {
			if (details.client()) {
				mapJar(
					fromOfficial ? files.getClientJar(minecraftVersion) : files.getIntermediaryClientJar(minecraftVersion),
					fromOfficial ? files.getIntermediaryClientJar(minecraftVersion) : files.getNamedJar(minecraftVersion),
					fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion),
					files.getLibraries(minecraftVersion),
					srcNs,
					dstNs
				);
			}
			if (details.server()) {
				mapJar(
					fromOfficial ? files.getServerJar(minecraftVersion) : files.getIntermediaryServerJar(minecraftVersion),
					fromOfficial ? files.getIntermediaryServerJar(minecraftVersion) : files.getNamedJar(minecraftVersion),
					fromOfficial ? files.getServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion),
					files.getLibraries(minecraftVersion),
					srcNs,
					dstNs
				);
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
