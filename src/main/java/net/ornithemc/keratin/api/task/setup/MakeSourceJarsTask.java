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
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.keratin.api.task.processing.Nester;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;

public abstract class MakeSourceJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MakeSourceJar.class, parameters -> {
				parameters.getInput().set(files.getMergedJar(minecraftVersion));
				parameters.getOutput().set(files.getSourceMergedJar(minecraftVersion));
				parameters.getExceptions().set(files.getSetupMergedExceptions(minecraftVersion));
				parameters.getSignatures().set(files.getSetupMergedSignatures(minecraftVersion));
				parameters.getNests().set(files.getMergedNests(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MakeSourceJar.class, parameters -> {
					parameters.getInput().set(files.getClientJar(minecraftVersion));
					parameters.getOutput().set(files.getSourceClientJar(minecraftVersion));
					parameters.getExceptions().set(files.getSetupClientExceptions(minecraftVersion));
					parameters.getSignatures().set(files.getSetupClientSignatures(minecraftVersion));
					parameters.getNests().set(files.getClientNests(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MakeSourceJar.class, parameters -> {
					parameters.getInput().set(files.getServerJar(minecraftVersion));
					parameters.getOutput().set(files.getSourceServerJar(minecraftVersion));
					parameters.getExceptions().set(files.getSetupServerExceptions(minecraftVersion));
					parameters.getSignatures().set(files.getSetupServerSignatures(minecraftVersion));
					parameters.getNests().set(files.getServerNests(minecraftVersion));
				});
			}
		}
	}

	public interface SourceJarParameters extends WorkParameters {

		Property<File> getInput();

		Property<File> getOutput();

		Property<File> getExceptions();

		Property<File> getSignatures();

		Property<File> getNests();

	}

	public static abstract class MakeSourceJar implements WorkAction<SourceJarParameters>, Exceptor, SignaturePatcher, Nester {

		@Override
		public void execute() {
			File input = getParameters().getInput().get();
			File output = getParameters().getOutput().get();
			File exceptions = getParameters().getExceptions().get();
			File signatures = getParameters().getSignatures().get();
			File nests = getParameters().getNests().getOrNull();

			File tmp = new File(output.getParentFile(), ".tmp.jar");

			try {
				exceptionsPatchJar(
					input,
					tmp,
					exceptions
				);
				signaturePatchJar(
					tmp,
					nests == null ? output:  tmp,
					signatures
				);
				if (nests != null) {
					nestJar(
						tmp,
						output,
						nests
					);
				}

				tmp.delete();
			} catch (IOException e) {
				throw new RuntimeException("error while making source jar", e);
			}
		}
	}
}
