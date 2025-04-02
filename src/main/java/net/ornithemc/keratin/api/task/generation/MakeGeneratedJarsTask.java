package net.ornithemc.keratin.api.task.generation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class MakeGeneratedJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		Project project = getProject();
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File builtJar = project.file("build/libs/" + project.getName() + ".jar");

		if (minecraftVersion.canBeMerged()) {
			copyGeneratedJar(
				builtJar,
				files.getNamedGeneratedMergedJar(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				copyGeneratedJar(
					builtJar,
					files.getNamedGeneratedClientJar(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				copyGeneratedJar(
					builtJar,
					files.getNamedGeneratedServerJar(minecraftVersion)
				);
			}
		}
	}

	private void copyGeneratedJar(File from, File to) throws IOException {
		Files.deleteIfExists(to.toPath());
		Files.copy(from.toPath(), to.toPath());
	}
}
