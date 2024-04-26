package net.ornithemc.keratin.api.task.mapping;

import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MapNestsTask extends MappingTask {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		getProject().getLogger().lifecycle(":mapping Nests for Minecraft " + minecraftVersion + " from " + srcNs + " to " + dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		if ("official".equals(srcNs) ? details.sharedMappings() : (details.client() && details.server())) {
			mapNests(
				fromOfficial ? files.getMergedNests(minecraftVersion) : files.getIntermediaryMergedNests(minecraftVersion),
				fromOfficial ? files.getIntermediaryMergedNests(minecraftVersion) : files.getNamedNests(minecraftVersion),
				fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion)
			);
		} else {
			if (details.client()) {
				mapNests(
					fromOfficial ? files.getClientNests(minecraftVersion) : files.getIntermediaryClientNests(minecraftVersion),
					fromOfficial ? files.getIntermediaryClientNests(minecraftVersion) : files.getNamedNests(minecraftVersion),
					fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion)
				);
			}
			if (details.server()) {
				mapNests(
					fromOfficial ? files.getServerNests(minecraftVersion) : files.getIntermediaryServerNests(minecraftVersion),
					fromOfficial ? files.getIntermediaryServerNests(minecraftVersion) : files.getNamedNests(minecraftVersion),
					fromOfficial ? files.getServerIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion)
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
			throw new IllegalStateException("cannot map Nests from " + srcNs + " to " + dstNs);
		}
	}
}
