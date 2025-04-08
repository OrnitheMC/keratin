package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class ExtendGraphTask extends MinecraftTask implements MappingsGraph {

	@Internal
	public abstract ListProperty<String> getFromMinecraftVersions();

	public void fromMinecraftVersions(String... minecraftVersions) {
		getFromMinecraftVersions().set(Arrays.asList(minecraftVersions));
	}

	@Internal
	public abstract Property<String> getClassNamePattern();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		List<String> fromMinecraftVersionStrings = getFromMinecraftVersions().getOrNull();

		if (fromMinecraftVersionStrings == null || fromMinecraftVersionStrings.isEmpty()) {
			throw new IllegalStateException("no Minecraft version specified to extend from");
		}

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();
		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();

		List<MinecraftVersion> fromMinecraftVersions = new ArrayList<>();

		for (String fromMinecraftVersion : fromMinecraftVersionStrings) {
			fromMinecraftVersions.add(MinecraftVersion.parse(keratin, fromMinecraftVersion));
		}

		getProject().getLogger().lifecycle("extending the graph from Minecraft " + String.join("/", fromMinecraftVersionStrings) + " to " + minecraftVersion.id());

		File graphDir = mappings.getMappingsDirectory();
		String classNamePattern = getClassNamePattern().getOrElse("");

		File jar = processedJars.getProcessedIntermediaryJar(minecraftVersion, keratin.getProcessorSettings(minecraftVersion));
		List<File> fromJars = new ArrayList<>();
		for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
			fromJars.add(processedJars.getProcessedIntermediaryJar(fromMinecraftVersion, keratin.getProcessorSettings(fromMinecraftVersion)));
		}

		extendGraph(graphDir, minecraftVersion, fromMinecraftVersions, jar, fromJars, classNamePattern);
	}
}
