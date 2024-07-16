package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class SetUpSourceTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		Project project = getProject();
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File decompSrc = files.getDecompiledSourceDirectory(minecraftVersion);
		File srcJava = project.file("src/main/java/");

		project.delete(srcJava);

		for (Path from : Files.walk(decompSrc.toPath()).toList()) {
			Path path = decompSrc.toPath().relativize(from);
			String pathName = path.toString();

			if (pathName.endsWith(".java")) {
				Path to = srcJava.toPath().resolve(pathName);

				Files.createDirectories(to.getParent());
				Files.copy(from, to);
			}
		};

		project.delete(decompSrc);
	}
}
