package net.ornithemc.keratin.api.task.enigma;

import java.util.Arrays;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class LaunchEnigmaTask extends MinecraftTask implements JavaExecution, EnigmaSession {

	@Override
	public void run() throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		for (MinecraftVersion minecraftVersion : getMinecraftVersions().get()) {
			checkSessionLock(minecraftVersion.id(), files.getEnigmaSessionLock(minecraftVersion));
		}

		super.run();
	}

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(EnigmaSessionAction.class, parameters -> {
			parameters.getMinecraftVersion().set(minecraftVersion.id());
			parameters.getSessionLock().set(files.getEnigmaSessionLock(minecraftVersion));
			parameters.getMainClass().set("cuchaz.enigma.gui.Main");
			parameters.getClasspath().set(project.getConfigurations().getByName(Configurations.ENIGMA_RUNTIME).getFiles());
			parameters.getArgs().set(Arrays.asList(
				"-jar"     , files.getMainProcessedIntermediaryJar(minecraftVersion).getAbsolutePath(),
				"-mappings", files.getWorkingDirectory(minecraftVersion).getAbsolutePath(),
				"-profile" , files.getEnigmaProfile().getAbsolutePath()
			));
		});
	}
}
