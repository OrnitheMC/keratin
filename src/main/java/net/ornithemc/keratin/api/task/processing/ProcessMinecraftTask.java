package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class ProcessMinecraftTask extends MinecraftTask implements Nester, SignaturePatcher {

	@Override
	public void run(String minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":processing Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File data;
		File jarIn;
		File jarOut = files.getMainIntermediaryJar(minecraftVersion);

		data = files.getMainIntermediaryNests(minecraftVersion);

		if (data != null) {
			getProject().getLogger().lifecycle("::applying nests");

			jarIn = jarOut;
			jarOut = files.getMainNestedIntermediaryJar(minecraftVersion);

			nestJar(jarIn, jarOut, data);
		}

		data = files.getMainIntermediarySparrowFile(minecraftVersion);

		if (data != null) {
			getProject().getLogger().lifecycle("::applying signature patches");

			jarIn = jarOut;
			jarOut = files.getMainSignaturePatchedIntermediaryJar(minecraftVersion);

			signaturePatchJar(jarIn, jarOut, data);
		}

		jarIn = jarOut;
		jarOut = files.getMainProcessedIntermediaryJar(minecraftVersion);

		Files.copy(jarIn, jarOut);
	}
}
