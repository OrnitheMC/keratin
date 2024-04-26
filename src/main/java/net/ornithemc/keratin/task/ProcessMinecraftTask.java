package net.ornithemc.keratin.task;

import java.io.File;
import java.io.IOException;

import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.api.task.Nester;
import net.ornithemc.keratin.api.task.SignaturePatcher;

public class ProcessMinecraftTask extends KeratinTask implements Nester, SignaturePatcher {

	@TaskAction
	public void run() throws IOException {
		Project project = getProject();
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		project.getLogger().lifecycle(":processing Minecraft...");

		File data;
		File jarIn;
		File jarOut = files.getMainIntermediaryJar();

		data = files.getMainIntermediaryNests();

		if (data != null) {
			project.getLogger().lifecycle(":applying nests to Minecraft");

			jarIn = jarOut;
			jarOut = files.getMainNestedIntermediaryJar();

			nestJar(jarIn, jarOut, data);
		}

		data = files.getMainIntermediarySparrowFile();

		if (data != null) {
			project.getLogger().lifecycle(":applying signature patches to Minecraft");

			jarIn = jarOut;
			jarOut = files.getMainSignaturePatchedIntermediaryJar();

			signaturePatchJar(jarIn, jarOut, data);
		}

		jarIn = jarOut;
		jarOut = files.getMainProcessedIntermediaryJar();

		Files.copy(jarIn, jarOut);
	}
}
