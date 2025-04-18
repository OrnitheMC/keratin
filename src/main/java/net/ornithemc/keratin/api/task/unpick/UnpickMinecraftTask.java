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
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;

public abstract class UnpickMinecraftTask extends MinecraftTask implements Unpick {

	@Internal
	public abstract Property<File> getUnpickConstantsJar();

	@Internal
	public abstract Property<Boolean> getForDecompile();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();
		LibrariesCache libraries = files.getGlobalCache().getLibrariesCache();
		BuildFiles buildFiles = files.getMappingsDevelopmentFiles().getBuildFiles();

		ProcessorSettings processorSettings = getForDecompile().get()
			? keratin.getProcessorSettingsForDecompile(minecraftVersion)
			: keratin.getProcessorSettings(minecraftVersion);

		workQueue.submit(UnpickMinecraft.class, parameters -> {
			parameters.getInputJar().set(processedJars.getProcessedIntermediaryJar(minecraftVersion, processorSettings));
			parameters.getUnpickDefinitionsFile().set(buildFiles.getProcessedIntermediaryUnpickDefinitionsFile(minecraftVersion));
			parameters.getUnpickConstantsJar().set(getUnpickConstantsJar().get());
			parameters.getUnpickClasspath().set(libraries.getLibraries(minecraftVersion));
			parameters.getOutputJar().set(buildFiles.getUnpickedProcessedIntermediaryJar(minecraftVersion));
		});
	}
}
