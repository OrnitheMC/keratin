package net.ornithemc.keratin.api.task.setup;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.decompiling.DecompileTask;

public abstract class MakeSourceTask extends DecompileTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		File sourceJar = files.getProcessedNamedSourceJar(minecraftVersion);
		File decompSrcDir = files.getDecompiledSourceDirectory(minecraftVersion);

		submitJavaExecDecompileTask(
			workQueue,
			"org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler",
			project.getConfigurations().getByName(Configurations.DECOMPILE_CLASSPATH),
			new String[] {
				"--indent-string=\t",
				sourceJar.getAbsolutePath(),
				decompSrcDir.getAbsolutePath()
			}
		);
	}
}
