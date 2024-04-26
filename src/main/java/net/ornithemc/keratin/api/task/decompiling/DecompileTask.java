package net.ornithemc.keratin.api.task.decompiling;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Action;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.process.JavaExecSpec;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class DecompileTask extends MinecraftTask {

	@OutputDirectory
	public abstract Property<File> getSourceDirectory();

	protected void decompile(String decompilerName, Action<JavaExecSpec> configuration) throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":decompiling Minecraft " + minecraftVersion + " with " + decompilerName);

		File src = getSourceDirectory().get();
		FileUtils.forceDelete(src);

		getProject().javaexec(javaexec -> {
			javaexec.getClasspath().plus(getProject().getConfigurations().getByName(Configurations.DECOMPILE_CLASSPATH));
			configuration.execute(javaexec);
		});
	}

}
