package net.ornithemc.keratin.api.task.mapping;

import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public abstract class MapSparrowTask extends MappingTask {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		getProject().getLogger().lifecycle(":mapping Sparrow for Minecraft " + minecraftVersion + " from " + srcNs + " to " + dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		if ("official".equals(srcNs) ? details.sharedMappings() : (details.client() && details.server())) {
			mapSparrow(
				fromOfficial ? files.getMergedSparrowFile(minecraftVersion) : files.getIntermediaryMergedSparrowFile(minecraftVersion),
				fromOfficial ? files.getIntermediaryMergedSparrowFile(minecraftVersion) : files.getNamedSparrowFile(minecraftVersion),
				fromOfficial ? files.getMergedIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion)
			);
		} else {
			if (details.client()) {
				mapSparrow(
					fromOfficial ? files.getClientSparrowFile(minecraftVersion) : files.getIntermediaryClientSparrowFile(minecraftVersion),
					fromOfficial ? files.getIntermediaryClientSparrowFile(minecraftVersion) : files.getNamedSparrowFile(minecraftVersion),
					fromOfficial ? files.getClientIntermediaryMappings(minecraftVersion) : files.getNamedMappings(minecraftVersion)
				);
			}
			if (details.server()) {
				mapSparrow(
					fromOfficial ? files.getServerSparrowFile(minecraftVersion) : files.getIntermediaryServerSparrowFile(minecraftVersion),
					fromOfficial ? files.getIntermediaryServerSparrowFile(minecraftVersion) : files.getNamedSparrowFile(minecraftVersion),
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
			throw new IllegalStateException("cannot map Sparrow from " + srcNs + " to " + dstNs);
		}
	}
}
