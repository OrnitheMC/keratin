package net.ornithemc.keratin.api.task.mapping;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MapProcessedMinecraftTask extends MappingTask implements Merger {

	@Override
	public void run(String minecraftVersion) {
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		getProject().getLogger().lifecycle(":mapping processed Minecraft " + minecraftVersion + " from " + srcNs + " to " + dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.client() && details.server()) {
			mapJar(
				files.getProcessedIntermediaryMergedJar(minecraftVersion),
				files.getProcessedNamedJar(minecraftVersion),
				files.getProcessedNamedMappings(minecraftVersion),
				files.getLibraries(minecraftVersion),
				srcNs,
				dstNs
			);
		} else {
			if (details.client()) {
				mapJar(
					files.getProcessedIntermediaryClientJar(minecraftVersion),
					files.getProcessedNamedJar(minecraftVersion),
					files.getProcessedNamedMappings(minecraftVersion),
					files.getLibraries(minecraftVersion),
					srcNs,
					dstNs
				);
			}
			if (details.server()) {
				mapJar(
					files.getProcessedIntermediaryServerJar(minecraftVersion),
					files.getProcessedNamedJar(minecraftVersion),
					files.getProcessedNamedMappings(minecraftVersion),
					files.getLibraries(minecraftVersion),
					srcNs,
					dstNs
				);
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
