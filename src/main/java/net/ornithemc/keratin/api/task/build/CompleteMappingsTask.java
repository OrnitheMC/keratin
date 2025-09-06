package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.nameproposal.MappingNameCompleter;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.keratin.api.task.setup.MappingsFiller;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class CompleteMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		getProject().getLogger().lifecycle(":completing mappings for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();
		MappedJarsCache mappedJars = globalCache.getMappedJarsCache();
		MappingsCache mappings = globalCache.getMappingsCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		workQueue.submit(CompleteMappings.class, parameters -> {
			parameters.getJar().set(mappedJars.getMainIntermediaryJar(minecraftVersion));
			parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
			parameters.getMappings().set(buildFiles.getMappingsFile(minecraftVersion));
			parameters.getIntermediary().set(mappings.getMainIntermediaryMappingsFile(minecraftVersion));
			parameters.getCompletedMappings().set(buildFiles.getCompletedMappingsFile(minecraftVersion));
		});
	}

	public interface BuildParameters extends WorkParameters {

		Property<File> getJar();

		ListProperty<File> getLibraries();

		Property<File> getMappings();

		Property<File> getIntermediary();

		Property<File> getCompletedMappings();

	}

	public static abstract class CompleteMappings implements WorkAction<BuildParameters>, MappingsFiller {

		@Override
		public void execute() {
			File jar = getParameters().getJar().get();
			List<File> libraries = getParameters().getLibraries().get();
			File mappings = getParameters().getMappings().get();
			File intermediary = getParameters().getIntermediary().get();
			File completedMappings = getParameters().getCompletedMappings().get();

			try {
				MappingNameCompleter.completeNames(
					jar.toPath(),
					mappings.toPath(),
					intermediary.toPath(),
					completedMappings.toPath()
				);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running mappings completer", e);
			}

			try {
				fillSpecializedMethodMappings(
					completedMappings,
					completedMappings,
					jar,
					libraries,
					Mapper.NAMED
				);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running mappings filler", e);
			}
		}
	}
}
