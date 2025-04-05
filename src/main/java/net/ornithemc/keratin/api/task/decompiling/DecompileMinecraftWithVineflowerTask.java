package net.ornithemc.keratin.api.task.decompiling;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;
import net.ornithemc.keratin.files.SharedFiles;

public abstract class DecompileMinecraftWithVineflowerTask extends DecompileTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFiles files = keratin.getFiles();

		SharedFiles sharedFiles = files.getSharedFiles();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		submitJavaExecDecompileTask(
			workQueue,
			"org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler",
			project.getConfigurations().getByName(Configurations.DECOMPILE_CLASSPATH),
			new String[] {
				buildFiles.getProcessedNamedJar(minecraftVersion.id()).getAbsolutePath(),
				sharedFiles.getDecompiledSourceDirectory(minecraftVersion).getAbsolutePath()
			}
		);
	}
}
