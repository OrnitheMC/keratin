package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ExtendGraphTask extends MinecraftTask implements MappingsGraph {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Internal
	public abstract Property<String> getFromFromMinecraftVersion();

	@Internal
	public abstract Property<String> getClassNamePattern();

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();
		String fromMinecraftVersion = getFromMinecraftVersion().get();
		String fromFromMinecraftVersion = getFromFromMinecraftVersion().getOrNull();

		if (fromFromMinecraftVersion == null) {
			getProject().getLogger().lifecycle("extending the graph from Minecraft " + fromMinecraftVersion + " to " + minecraftVersion);
		} else {
			getProject().getLogger().lifecycle("extending the graph from Minecraft " + fromFromMinecraftVersion + "/" + fromMinecraftVersion + " to " + minecraftVersion);
		}

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		String classNamePattern = getClassNamePattern().getOrElse("");

		File jar = files.getMainProcessedIntermediaryJar(minecraftVersion);
		File fromJar = files.getMainProcessedIntermediaryJar(fromMinecraftVersion);
		File fromFromJar = (fromFromMinecraftVersion == null) ? null : files.getMainProcessedIntermediaryJar(fromFromMinecraftVersion);

		this.extendGraph(graphDir, minecraftVersion, fromMinecraftVersion, fromFromMinecraftVersion, jar, fromJar, fromFromJar, classNamePattern);
	}
}
