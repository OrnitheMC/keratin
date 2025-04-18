package net.ornithemc.keratin.api.task.javadoc;

import java.io.File;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.filament.mappingpoet.MappingPoet;

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
		OrnitheFiles files = keratin.getFiles();

		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		if (minecraftVersion.hasSharedVersioning() && minecraftVersion.canBeMerged()) {
			workQueue.submit(MappingPoetAction.class, parameters -> {
				parameters.getMappings().set(buildFiles.getMergedTinyV2MappingsFile(minecraftVersion.id()));
				parameters.getJar().set(buildFiles.getJavadocNamedJar(minecraftVersion.id()));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.id()));
				parameters.getOutputDirectory().set(buildFiles.getFakeSourceDirectory(minecraftVersion.id()));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MappingPoetAction.class, parameters -> {
					parameters.getMappings().set(buildFiles.getMergedTinyV2MappingsFile(minecraftVersion.client().id()));
					parameters.getJar().set(buildFiles.getJavadocNamedJar(minecraftVersion.client().id()));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.client().id()));
					parameters.getOutputDirectory().set(buildFiles.getFakeSourceDirectory(minecraftVersion.client().id()));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MappingPoetAction.class, parameters -> {
					parameters.getMappings().set(buildFiles.getMergedTinyV2MappingsFile(minecraftVersion.server().id()));
					parameters.getJar().set(buildFiles.getJavadocNamedJar(minecraftVersion.server().id()));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion.server().id()));
					parameters.getOutputDirectory().set(buildFiles.getFakeSourceDirectory(minecraftVersion.server().id()));
				});
			}
		}
	}

	public interface MappingPoetParameters extends WorkParameters {

		Property<File> getMappings();

		Property<File> getJar();

		ListProperty<File> getLibraries();

		Property<File> getOutputDirectory();

	}

	public static abstract class MappingPoetAction implements WorkAction<MappingPoetParameters> {

		@Override
		public void execute() {
			File mappings = getParameters().getMappings().get();
			File jar = getParameters().getJar().get();
			List<File> libs = getParameters().getLibraries().get();
			File outputDir = getParameters().getOutputDirectory().get();

			MappingPoet.generate(
				mappings.toPath(),
				jar.toPath(),
				outputDir.toPath(),
				libs.stream().map(File::toPath).toList()
			);
		}
	}
}
