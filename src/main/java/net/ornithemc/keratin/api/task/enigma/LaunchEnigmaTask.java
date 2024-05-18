package net.ornithemc.keratin.api.task.enigma;

import java.util.Arrays;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class LaunchEnigmaTask extends MinecraftTask implements JavaExecution {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(JavaExecutionAction.class, parameters -> {
			parameters.getMainClass().set("cuchaz.enigma.gui.Main");
			parameters.getClasspath().set(project.getConfigurations().getByName(Configurations.ENIGMA_RUNTIME).getFiles());
			parameters.getArgs().set(Arrays.asList(
					"-jar"     , files.getMainProcessedIntermediaryJar(minecraftVersion).getAbsolutePath(),
					"-mappings", files.getRunDirectory(minecraftVersion).getAbsolutePath(),
					"-profile" , "enigma_profile.json"
			));
		});
	}
}
