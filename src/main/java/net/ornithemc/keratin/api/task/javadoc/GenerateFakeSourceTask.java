package net.ornithemc.keratin.api.task.javadoc;

import java.util.Arrays;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class GenerateFakeSourceTask extends MinecraftTask implements JavaExecution {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(JavaExecutionAction.class, parameters -> {
			parameters.getMainClass().set("net.fabricmc.mappingpoet.Main");
			parameters.getClasspath().set(project.getConfigurations().getByName(Configurations.MAPPING_POET).getFiles());
			parameters.getArgs().set(Arrays.asList(
				files.getMergedTinyV2NamedMappings(minecraftVersion).getAbsolutePath(),
				files.getNamedJar(minecraftVersion).getAbsolutePath(),
				files.getFakeSourceDirectory(minecraftVersion).getAbsolutePath(),
				files.getLibrariesCache().getAbsolutePath()
			));
		});
	}
}
