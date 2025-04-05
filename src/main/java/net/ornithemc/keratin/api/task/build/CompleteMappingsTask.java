package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.fabricmc.nameproposal.MappingNameCompleter;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.GlobalCache.MappingsCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class CompleteMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		getProject().getLogger().lifecycle(":completing mappings for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		MappedJarsCache mappedJars = files.getGlobalCache().getMappedJarsCache();
		MappingsCache mappings = files.getGlobalCache().getMappingsCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		workQueue.submit(CompleteMappings.class, parameters -> {
			parameters.getJar().set(mappedJars.getMainIntermediaryJar(minecraftVersion));
			parameters.getMappings().set(buildFiles.getMappingsFile(minecraftVersion));
			parameters.getIntermediary().set(mappings.getMainIntermediaryMappingsFile(minecraftVersion));
			parameters.getCompletedMappings().set(buildFiles.getCompletedMappingsFile(minecraftVersion));
		});
	}

	public interface BuildParameters extends WorkParameters {

		Property<File> getJar();

		Property<File> getMappings();

		Property<File> getIntermediary();

		Property<File> getCompletedMappings();

	}

	public static abstract class CompleteMappings implements WorkAction<BuildParameters> {

		@Override
		public void execute() {
			File jar = getParameters().getJar().get();
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
				throw new UncheckedIOException("error while completing mappings", e);
			}
		}
	}
}
