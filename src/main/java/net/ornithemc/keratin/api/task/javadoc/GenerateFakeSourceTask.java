package net.ornithemc.keratin.api.task.javadoc;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class GenerateFakeSourceTask extends MinecraftTask {

	@OutputDirectory
	public abstract Property<File> getSourceDirectory();

	@TaskAction
	public void run() {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":generating fake source for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = KeratinGradleExtension.get(getProject());
		OrnitheFilesAPI files = keratin.getFiles();

		File mappings = files.getTinyV2NamedMappings(minecraftVersion);
		File jar = files.getNamedJar(minecraftVersion);
		File src = getSourceDirectory().get();
		File libraries = files.getLibrariesCache();

		getProject().javaexec(javaexec -> {
			javaexec.getMainClass().set("net.fabricmc.mappingpoet.Main");
			javaexec.getClasspath().plus(getProject().getConfigurations().getByName(Configurations.MAPPING_POET));
			javaexec.getArgs().add(mappings.getAbsolutePath());
			javaexec.getArgs().add(jar.getAbsolutePath());
			javaexec.getArgs().add(src.getAbsolutePath());
			javaexec.getArgs().add(libraries.getAbsolutePath());
		});
	}
}
