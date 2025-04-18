package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Preen;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceJars;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class ProcessSourceJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		SourceJars sourceJars = files.getExceptionsAndSignaturesDevelopmentFiles().getSourceJars();

		File sourceJar = minecraftVersion.canBeMerged()
			? sourceJars.getNamedMergedJar(minecraftVersion)
			: minecraftVersion.hasClient()
				? sourceJars.getNamedClientJar(minecraftVersion)
				: sourceJars.getNamedServerJar(minecraftVersion);
		File processedSourceJar = sourceJars.getProcessedNamedJar(minecraftVersion);

		workQueue.submit(ProcessSourceJar.class, parameters -> {
			parameters.getInput().set(sourceJar);
			parameters.getOutput().set(processedSourceJar);
		});
	}

	public interface ProcessSourceJarParameters extends WorkParameters {

		Property<File> getInput();

		Property<File> getOutput();

	}

	public static abstract class ProcessSourceJar implements WorkAction<ProcessSourceJarParameters>, Preen {

		@Override
		public void execute() {
			File input = getParameters().getInput().get();
			File output = getParameters().getOutput().get();

			try {
				splitMergedBridgeMethods(input, output);
			} catch (IOException e) {
				throw new RuntimeException("error while processing jar", e);
			}
		}
	}
}
