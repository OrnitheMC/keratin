package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.KeratinTask;

public abstract class ProcessMinecraftTask extends KeratinTask implements Nester, SignaturePatcher {

	public abstract Property<String> getMinecraftVersion();

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":processing Minecraft version " + minecraftVersion);

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
