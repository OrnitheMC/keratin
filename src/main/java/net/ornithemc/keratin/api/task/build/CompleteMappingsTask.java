package net.ornithemc.keratin.api.task.build;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.fabricmc.nameproposal.MappingNameCompleter;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class CompleteMappingsTask extends BuildTask {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":completing mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File jar = files.getMainIntermediaryJar(minecraftVersion);
		File mappings = files.getNamedMappings(minecraftVersion);
		File intermediary = files.getMainIntermediaryMappings(minecraftVersion);
		File completedMappings = files.getCompletedNamedMappings(minecraftVersion);

		MappingNameCompleter.completeNames(
			jar.toPath(),
			mappings.toPath(),
			intermediary.toPath(),
			completedMappings.toPath()
		);
	}
}
