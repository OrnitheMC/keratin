package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Preen;

public abstract class ProcessSourceJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		File sourceJar = minecraftVersion.canBeMerged()
			? files.getNamedSourceMergedJar(minecraftVersion)
			: minecraftVersion.hasClient()
				? files.getNamedSourceClientJar(minecraftVersion)
				: files.getNamedSourceServerJar(minecraftVersion);
		File processedSourceJar = files.getProcessedNamedSourceJar(minecraftVersion);

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
