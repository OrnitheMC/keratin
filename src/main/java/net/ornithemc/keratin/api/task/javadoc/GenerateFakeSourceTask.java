package net.ornithemc.keratin.api.task.javadoc;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.gradle.api.Project;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class GenerateFakeSourceTask extends MinecraftTask implements JavaExecution {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFiles files = keratin.getFiles();

		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		Set<File> classpath = project.getConfigurations().getByName(Configurations.MAPPING_POET).getFiles();

		if (minecraftVersion.hasSharedVersioning() && minecraftVersion.canBeMerged()) {
			submit(
				workQueue,
				buildFiles.getMergedTinyV2MappingsFile(minecraftVersion.id()),
				buildFiles.getJavadocNamedJar(minecraftVersion.id()),
				buildFiles.getFakeSourceDirectory(minecraftVersion.id()),
				libraries.getDirectory(),
				classpath
			);
		} else {
			if (minecraftVersion.hasClient()) {
				submit(
					workQueue,
					buildFiles.getMergedTinyV2MappingsFile(minecraftVersion.client().id()),
					buildFiles.getJavadocNamedJar(minecraftVersion.client().id()),
					buildFiles.getFakeSourceDirectory(minecraftVersion.client().id()),
					libraries.getDirectory(),
					classpath
				);
			}
			if (minecraftVersion.hasServer()) {
				submit(
					workQueue,
					buildFiles.getMergedTinyV2MappingsFile(minecraftVersion.server().id()),
					buildFiles.getJavadocNamedJar(minecraftVersion.server().id()),
					buildFiles.getFakeSourceDirectory(minecraftVersion.server().id()),
					libraries.getDirectory(),
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
