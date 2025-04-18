package net.ornithemc.keratin.api.task.build;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import cuchaz.enigma.command.CheckMappingsCommand;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.MappedJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class CheckMappingsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {

		getProject().getLogger().lifecycle(":checking mappings for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappedJarsCache mappedJars = files.getGlobalCache().getMappedJarsCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		workQueue.submit(CheckMappings.class, parameters -> {
			parameters.getJar().set(mappedJars.getMainIntermediaryJar(minecraftVersion));
			parameters.getMappings().set(buildFiles.getMappingsFile(minecraftVersion));
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
