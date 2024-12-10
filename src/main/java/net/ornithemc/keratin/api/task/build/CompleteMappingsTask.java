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
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class CompleteMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		getProject().getLogger().lifecycle(":completing mappings for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(CompleteMappings.class, parameters -> {
			parameters.getJar().set(files.getMainIntermediaryJar(minecraftVersion));
			parameters.getMappings().set(files.getNamedMappings(minecraftVersion));
			parameters.getIntermediary().set(files.getMainIntermediaryMappings(minecraftVersion));
			parameters.getCompletedMappings().set(files.getCompletedNamedMappings(minecraftVersion));
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
