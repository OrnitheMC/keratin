package net.ornithemc.keratin.api.task;

import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.manifest.VersionDetails;

public abstract class MergeMinecraftJarsTask extends KeratinTask implements Merger {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":merging official jars for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (details.sharedMappings() && details.client() && details.server()) {
			OrnitheFilesAPI files = keratin.getFiles();

			mergeJars(
				files.getClientJar(minecraftVersion),
				files.getServerJar(minecraftVersion),
				files.getMergedJar(minecraftVersion)
			);
		}
	}
}
