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
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

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
		OrnitheFilesAPI files = keratin.getFiles();

		List<MinecraftVersion> fromMinecraftVersions = new ArrayList<>();

		for (String fromMinecraftVersion : fromMinecraftVersionStrings) {
			fromMinecraftVersions.add(MinecraftVersion.parse(keratin, fromMinecraftVersion));
		}

		getProject().getLogger().lifecycle("extending the graph from Minecraft " + String.join("/", fromMinecraftVersionStrings) + " to " + minecraftVersion.id());

		File graphDir = files.getMappingsDirectory();
		String classNamePattern = getClassNamePattern().getOrElse("");

		File jar = files.getMainProcessedIntermediaryJar(minecraftVersion);
		List<File> fromJars = new ArrayList<>();
		for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
			fromJars.add(files.getMainProcessedIntermediaryJar(fromMinecraftVersion));
		}

		extendGraph(graphDir, minecraftVersion, fromMinecraftVersions, jar, fromJars, classNamePattern);
	}
}
