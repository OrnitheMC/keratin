package net.ornithemc.keratin.api.task.build;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import cuchaz.enigma.command.CheckMappingsCommand;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class CheckMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {

		getProject().getLogger().lifecycle(":checking mappings for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(CheckMappings.class, parameters -> {
			parameters.getJar().set(files.getMainIntermediaryJar(minecraftVersion));
			parameters.getMappings().set(files.getNamedMappings(minecraftVersion));
		});
	}

	public interface BuildParameters extends WorkParameters {

		Property<File> getJar();

		Property<File> getMappings();

	}

	public static abstract class CheckMappings implements WorkAction<BuildParameters> {

		@Override
		public void execute() {
			File jar = getParameters().getJar().get();
			File mappings = getParameters().getMappings().get();

			try {
				new CheckMappingsCommand().run(new String[] {
					jar.getAbsolutePath(),
					mappings.getAbsolutePath()
				});
			} catch (Exception e) {
			}
		}
	}
}
