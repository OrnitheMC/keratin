package net.ornithemc.keratin.api.task.decompiling;

import java.io.File;
import java.io.IOException;

import org.gradle.api.tasks.TaskAction;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class DecompileMinecraftWithCfrTask extends DecompileTask {

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File jar = files.getProcessedNamedJar(minecraftVersion);
		File src = getSourceDirectory().get();

		decompile("CFR", javaexec -> {
			javaexec.getMainClass().set("org.benf.cfr.reader.Main");
			javaexec.getArgs().add(jar.getAbsolutePath());
			javaexec.getArgs().add("--outputdir");
			javaexec.getArgs().add(src.getAbsolutePath());
		});
	}
}
