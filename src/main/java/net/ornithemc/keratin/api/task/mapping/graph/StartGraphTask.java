package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.ProcessorSettings;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;

public abstract class StartGraphTask extends MinecraftTask implements MappingsGraph {

	@Internal
	public abstract Property<String> getClassNamePattern();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":resetting the graph with Minecraft " + minecraftVersion.id() + " as the root");

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();
		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();

		ProcessorSettings processorSettings = keratin.getProcessorSettings(minecraftVersion);

		File graphDir = mappings.getMappingsDirectory();
		File rootMinecraftJar = processedJars.getProcessedIntermediaryJar(minecraftVersion, processorSettings);
		String classNamePattern = getClassNamePattern().getOrElse("");

		startGraph(graphDir, minecraftVersion, rootMinecraftJar, classNamePattern);
	}
}
