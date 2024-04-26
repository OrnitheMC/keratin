package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ProcessMappingsTask extends MinecraftTask implements Nester {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":processing mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File data;
		File jarIn;
		File jarOut = files.getMainIntermediaryMappings(minecraftVersion);

		data = files.getMainIntermediaryNests(minecraftVersion);

		if (data != null) {
			getProject().getLogger().lifecycle("::applying nests");

			jarIn = jarOut;
			jarOut = files.getMainNestedIntermediaryMappings(minecraftVersion);

			nestJar(jarIn, jarOut, data);
		}

		jarIn = jarOut;
		jarOut = files.getMainProcessedIntermediaryMappings(minecraftVersion);

		Files.copy(jarIn, jarOut);
	}
}
