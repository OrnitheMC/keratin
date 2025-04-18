package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.SharedFiles;

public abstract class SetUpSourceTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		Project project = getProject();
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		SharedFiles sharedFiles = files.getSharedFiles();

		File decompSrc = sharedFiles.getDecompiledSourceDirectory(minecraftVersion);
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
