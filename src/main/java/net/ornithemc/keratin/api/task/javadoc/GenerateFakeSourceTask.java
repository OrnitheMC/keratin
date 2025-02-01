package net.ornithemc.keratin.api.task.javadoc;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class GenerateFakeSourceTask extends MinecraftTask implements JavaExecution {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		File libraries = files.getLibrariesCache();
		Set<File> classpath = project.getConfigurations().getByName(Configurations.MAPPING_POET).getFiles();

		if (minecraftVersion.hasSharedVersioning() && minecraftVersion.canBeMerged()) {
			submit(
				workQueue,
				files.getMergedTinyV2NamedMappings(minecraftVersion.id()),
				files.getJavadocNamedJar(minecraftVersion.id()),
				files.getFakeSourceDirectory(minecraftVersion.id()),
				libraries,
				classpath
			);
		} else {
			if (minecraftVersion.hasClient()) {
				submit(
					workQueue,
					files.getMergedTinyV2NamedMappings(minecraftVersion.client().id()),
					files.getJavadocNamedJar(minecraftVersion.client().id()),
					files.getFakeSourceDirectory(minecraftVersion.client().id()),
					libraries,
					classpath
				);
			}
			if (minecraftVersion.hasServer()) {
				submit(
					workQueue,
					files.getMergedTinyV2NamedMappings(minecraftVersion.server().id()),
					files.getJavadocNamedJar(minecraftVersion.server().id()),
					files.getFakeSourceDirectory(minecraftVersion.server().id()),
					libraries,
					classpath
				);
			}
		}
	}

	private void submit(WorkQueue workQueue, File mappings, File jar, File dir, File libs, Iterable<File> classpath) {
		workQueue.submit(JavaExecutionAction.class, parameters -> {
			parameters.getMainClass().set("net.fabricmc.mappingpoet.Main");
			parameters.getClasspath().set(classpath);
			parameters.getArgs().set(Arrays.asList(
				mappings.getAbsolutePath(),
				jar.getAbsolutePath(),
				dir.getAbsolutePath(),
				libs.getAbsolutePath()
			));
		});
	}
}
