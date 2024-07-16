package net.ornithemc.keratin.api.task.setup;

import java.io.File;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.decompiling.DecompileTask;

public abstract class MakeSourceTask extends DecompileTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		File setupJar = (details.sharedMappings() || (details.client() && details.server()))
			? files.getNamedSetupMergedJar(minecraftVersion)
			: details.client()
				? files.getNamedSetupClientJar(minecraftVersion)
				: files.getNamedSetupServerJar(minecraftVersion);
		File decompSrcDir = files.getDecompiledSourceDirectory(minecraftVersion);

		submitJavaExecDecompileTask(
			workQueue,
			"org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler",
			project.getConfigurations().getByName(Configurations.DECOMPILE_CLASSPATH),
			new String[] {
				setupJar.getAbsolutePath(),
				decompSrcDir.getAbsolutePath()
			}
		);
	}
}
