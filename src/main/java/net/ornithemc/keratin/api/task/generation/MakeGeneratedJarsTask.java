package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class MakeGeneratedJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		Project project = getProject();
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		File builtJar = project.file("build/libs/" + project.getName() + ".jar");

		if (details.sharedMappings() || (details.client() && details.server())) {
			Files.copy(
				builtJar,
				files.getNamedGeneratedMergedJar(minecraftVersion)
			);
		} else {
			if (details.client()) {
				Files.copy(
					builtJar,
					files.getNamedGeneratedClientJar(minecraftVersion)
				);
			}
			if (details.server()) {
				Files.copy(
					builtJar,
					files.getNamedGeneratedServerJar(minecraftVersion)
				);
			}
		}
	}
}
