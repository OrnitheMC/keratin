package net.ornithemc.keratin.api.task.mapping;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.api.task.merging.Merger;

public abstract class MapMinecraftTask extends KeratinTask implements Mapper, Merger {

	public abstract Property<String> getMinecraftVersion();

	public abstract Property<String> getSourceNamespace();

	public abstract Property<String> getTargetNamespace();

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();
		String srcNs = getSourceNamespace().get();
		String dstNs = getTargetNamespace().get();

		validateNamespaces(srcNs, dstNs);

		getProject().getLogger().lifecycle(":mapping Minecraft version " + minecraftVersion + " from " + srcNs + " to " + dstNs);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		boolean fromOfficial = "official".equals(srcNs);

		if (fromOfficial ? details.sharedMappings() : (details.client() && details.server())) {
			mapJar(
				files.getMergedJar(srcNs),
				files.getMergedJar(dstNs),
				files.getMergedMappings(minecraftVersion, dstNs),
				files.getLibraries(minecraftVersion),
				srcNs,
				dstNs
			);
		} else {
			if (details.client()) {
				mapJar(
					files.getClientJar(srcNs),
					files.getClientJar(dstNs),
					files.getClientMappings(minecraftVersion, dstNs),
					files.getLibraries(minecraftVersion),
					srcNs,
					dstNs
				);
			}
			if (details.server()) {
				mapJar(
					files.getServerJar(srcNs),
					files.getServerJar(dstNs),
					files.getServerMappings(minecraftVersion, dstNs),
					files.getLibraries(minecraftVersion),
					srcNs,
					dstNs
				);
			}

			if (fromOfficial && keratin.getIntermediaryGen().get() > 1) {
				getProject().getLogger().lifecycle(":merging " + dstNs + " Minecraft jars");

				mergeJars(
					files.getClientJar(dstNs),
					files.getServerJar(dstNs),
					files.getMergedJar(dstNs)
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
