package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.mapping.graph.MappingsGraph;
import net.ornithemc.keratin.api.task.mapping.graph.Validators;
import net.ornithemc.mappingutils.io.Format;

public abstract class BuildProcessedMappingsTask extends BuildTask implements MappingsGraph {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":building processed mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File graphDir = files.getMappingsDirectory();
		File output = files.getProcessedNamedMappings(minecraftVersion);

		loadMappings(minecraftVersion, graphDir, output, Format.TINY_V2, Validators.REMOVE_DUMMY_MAPPINGS);
	}
}
