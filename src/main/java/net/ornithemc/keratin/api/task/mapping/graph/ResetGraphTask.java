package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ResetGraphTask extends MinecraftTask implements MappingsGraph {

	@Internal
	public abstract Property<String> getClassNamePattern();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":resetting the graph with Minecraft " + minecraftVersion.id() + " as the root");

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		File rootMinecraftJar = files.getMainProcessedIntermediaryJar(minecraftVersion);
		String classNamePattern = getClassNamePattern().getOrElse("");

		resetGraph(graphDir, minecraftVersion, rootMinecraftJar, classNamePattern);
	}
}
