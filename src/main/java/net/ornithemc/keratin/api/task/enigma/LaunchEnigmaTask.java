package net.ornithemc.keratin.api.task.enigma;

import java.io.File;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class LaunchEnigmaTask extends MinecraftTask {

	@Override
	public void run(String minecraftVersion) {
		getProject().getLogger().lifecycle(":launching Enigma");

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File jar = files.getMainProcessedIntermediaryJar(minecraftVersion);
		File dir = files.getRunDirectory(minecraftVersion);

		getProject().javaexec(javaexec -> {
			javaexec.getMainClass().set("cuchaz.enigma.gui.Main");
			javaexec.classpath(getProject().getConfigurations().getByName(Configurations.ENIGMA_RUNTIME));
			javaexec.args(
				"-jar"     , jar.getAbsolutePath(),
				"-mappings", dir.getAbsolutePath(),
				"-profile" , "enigma_profile.json"
			);
		});
	}
}
