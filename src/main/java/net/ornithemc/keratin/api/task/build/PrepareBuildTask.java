package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.mapping.graph.MappingsGraph;
import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.io.Format;

public abstract class PrepareBuildTask extends MinecraftTask implements MappingsGraph {

	@Override
	public void run(String minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":preparing build for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File buildCache = files.getLocalBuildCache();
		File graphDir = files.getMappingsDirectory();
		File output = files.getNamedMappings(minecraftVersion);
		File processedOutput = files.getProcessedNamedMappings(minecraftVersion);
		File nests = files.getMainIntermediaryNests(minecraftVersion);

		if (!buildCache.exists()) {
			buildCache.mkdirs();
		}

		loadMappings(minecraftVersion, graphDir, processedOutput, Format.TINY_V2);

		if (nests == null) {
			Files.copy(processedOutput, output);
		} else {
			MappingUtils.undoNests(Format.TINY_V2, processedOutput.toPath(), output.toPath(), nests.toPath());
		}
	}
}
