package net.ornithemc.keratin.api.task.unpick;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.ProcessorSettings;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class UnpickMinecraftTask extends MinecraftTask implements Unpick {

	@Internal
	public abstract Property<File> getUnpickConstantsJar();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();
		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		ProcessorSettings processorSettings = keratin.getProcessorSettings(minecraftVersion);

		workQueue.submit(UnpickMinecraft.class, parameters -> {
			parameters.getInputJar().set(processedJars.getProcessedIntermediaryJar(minecraftVersion, processorSettings));
			parameters.getUnpickDefinitionsFile().set(buildFiles.getProcessedIntermediaryUnpickDefinitionsFile(minecraftVersion));
			parameters.getUnpickConstantsJar().set(getUnpickConstantsJar().get());
			parameters.getUnpickClasspath().set(libraries.getLibraries(minecraftVersion));
			parameters.getOutputJar().set(buildFiles.getUnpickedProcessedIntermediaryJar(minecraftVersion));
		});
	}
}
