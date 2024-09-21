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
import net.ornithemc.mappingutils.PropagationDirection;
import net.ornithemc.mappingutils.io.Format;

public abstract class SaveMappingsIntoGraphTask extends MinecraftTask implements MappingsGraph {

	@Internal
	public abstract Property<PropagationDirection> getPropagationDirection();

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":saving mappings into the graph for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		File input = files.getWorkingDirectory(minecraftVersion);
		PropagationDirection propagationDir = getPropagationDirection().get();

		saveMappings(minecraftVersion.id(), graphDir, input, Format.ENIGMA_DIR, Validators.insertDummyMappings(), propagationDir);
		loadMappings(minecraftVersion.id(), graphDir, input, Format.ENIGMA_DIR, Validators.removeDummyMappings(false));
	}
}
