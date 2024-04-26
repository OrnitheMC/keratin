package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ResetGraphTask extends MinecraftTask implements MappingsGraph {

	@Internal
	public abstract Property<String> getClassNamePattern();

	@TaskAction
	public void run() throws IOException {
		String rootMinecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":resetting the graph with Minecraft " + rootMinecraftVersion + " as the root");

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		File rootMinecraftJar = files.getMainProcessedIntermediaryJar(rootMinecraftVersion);
		String classNamePattern = getClassNamePattern().get();

		resetGraph(graphDir, rootMinecraftVersion, rootMinecraftJar, classNamePattern);
	}
}
