package net.ornithemc.keratin.api.task.build;

import java.io.File;

import org.gradle.api.tasks.TaskAction;

import cuchaz.enigma.command.CheckMappingsCommand;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class CheckMappingsTask extends BuildTask {

	@TaskAction
	public void run() throws Exception {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":checking mappings for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File jar = files.getMainIntermediaryJar(minecraftVersion);
		File mappings = files.getNamedMappings(minecraftVersion);

		try {
			new CheckMappingsCommand().run(new String[] {
				jar.getAbsolutePath(),
				mappings.getAbsolutePath()
			});
		} catch (Exception e) {
		}
	}
}
