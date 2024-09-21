package net.ornithemc.keratin.api.task.generation;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import com.google.common.io.Files;

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
			Files.copy(
				builtJar,
				files.getNamedGeneratedMergedJar(minecraftVersion)
			);
		} else {
			if (minecraftVersion.hasClient()) {
				Files.copy(
					builtJar,
					files.getNamedGeneratedClientJar(minecraftVersion)
				);
			}
			if (minecraftVersion.hasServer()) {
				Files.copy(
					builtJar,
					files.getNamedGeneratedServerJar(minecraftVersion)
				);
			}
		}
	}
}
