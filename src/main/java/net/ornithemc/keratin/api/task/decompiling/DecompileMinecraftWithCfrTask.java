package net.ornithemc.keratin.api.task.decompiling;

import java.io.File;
import java.io.IOException;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;

public abstract class DecompileMinecraftWithCfrTask extends DecompileTask {

	@Override
	public void run(String minecraftVersion) throws IOException {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File jar = files.getProcessedNamedJar(minecraftVersion);
		File src = files.getDecompiledSourceDirectory(minecraftVersion);

		decompile(minecraftVersion, "CFR", javaexec -> {
			javaexec.getMainClass().set("org.benf.cfr.reader.Main");
			javaexec.args(
				jar.getAbsolutePath(),
				"--outputdir",
				src.getAbsolutePath()
			);
		});
	}
}
