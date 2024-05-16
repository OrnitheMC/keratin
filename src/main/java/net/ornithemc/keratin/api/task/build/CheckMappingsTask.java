package net.ornithemc.keratin.api.task.build;

import java.io.File;

import cuchaz.enigma.command.CheckMappingsCommand;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class CheckMappingsTask extends BuildTask {

	@Override
	public void run(String minecraftVersion) throws Exception {

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
