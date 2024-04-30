package net.ornithemc.keratin.api.task.decompiling;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import org.gradle.api.Action;
import org.gradle.process.JavaExecSpec;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class DecompileTask extends MinecraftTask {

	protected void decompile(String decompilerName, Action<JavaExecSpec> configuration) throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":decompiling Minecraft " + minecraftVersion + " with " + decompilerName);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File src = files.getDecompiledSourceDirectory();
		FileUtils.forceDelete(src);

		getProject().javaexec(javaexec -> {
			javaexec.classpath(getProject().getConfigurations().getByName(Configurations.DECOMPILE_CLASSPATH));
			configuration.execute(javaexec);
		});
	}
}
