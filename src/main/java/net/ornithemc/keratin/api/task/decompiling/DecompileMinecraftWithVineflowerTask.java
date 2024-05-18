package net.ornithemc.keratin.api.task.decompiling;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class DecompileMinecraftWithVineflowerTask extends DecompileTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		submitDecompileTask(
			workQueue,
			"org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler",
			project.getConfigurations().getByName(Configurations.DECOMPILE_CLASSPATH),
			new String[] {
				files.getProcessedNamedJar(minecraftVersion).getAbsolutePath(),
				files.getDecompiledSourceDirectory(minecraftVersion).getAbsolutePath()
			}
		);
	}
}
