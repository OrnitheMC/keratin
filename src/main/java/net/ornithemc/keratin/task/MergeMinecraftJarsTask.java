package net.ornithemc.keratin.task;

import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.api.task.Merger;
import net.ornithemc.keratin.manifest.VersionDetails;

public abstract class MergeMinecraftJarsTask extends KeratinTask implements Merger {

	@TaskAction
	public void run() throws IOException {
		getProject().getLogger().lifecycle(":merging official Minecraft jars");

		KeratinGradleExtension keratin = getExtension();
		VersionDetails details = keratin.getVersionDetails();

		if (details.sharedMappings() && details.client() && details.server()) {
			OrnitheFilesAPI files = keratin.getFiles();

			mergeJars(
				files.getClientJar(),
				files.getServerJar(),
				files.getMergedJar()
			);
		}
	}
}
