package net.ornithemc.keratin.api.task.setup;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.api.task.processing.Condor;
import net.ornithemc.keratin.api.task.processing.Exceptor;
import net.ornithemc.keratin.api.task.processing.Nester;
import net.ornithemc.keratin.api.task.processing.SignaturePatcher;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SetupFiles;
import net.ornithemc.keratin.files.ExceptionsAndSignaturesDevelopmentFiles.SourceJars;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class MakeSourceJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		GameJarsCache gameJars = globalCache.getGameJarsCache();
		NestsCache nests = globalCache.getNestsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();
		SetupFiles setupFiles = files.getExceptionsAndSignaturesDevelopmentFiles().getSetupFiles();
		SourceJars sourceJars = files.getExceptionsAndSignaturesDevelopmentFiles().getSourceJars();

		if (minecraftVersion.hasSharedObfuscation()) {
			workQueue.submit(MakeSourceJar.class, parameters -> {
				parameters.getInput().set(gameJars.getMergedJar(minecraftVersion));
				parameters.getOutput().set(sourceJars.getMergedJar(minecraftVersion));
				parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
				parameters.getExceptions().set(setupFiles.getMergedExceptionsFile(minecraftVersion));
				parameters.getSignatures().set(setupFiles.getMergedSignaturesFile(minecraftVersion));
				parameters.getNests().set(nests.getMergedNestsFile(minecraftVersion));
			});
		} else {
			if (minecraftVersion.hasClient()) {
				workQueue.submit(MakeSourceJar.class, parameters -> {
					parameters.getInput().set(gameJars.getClientJar(minecraftVersion));
					parameters.getOutput().set(sourceJars.getClientJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getExceptions().set(setupFiles.getClientExceptionsFile(minecraftVersion));
					parameters.getSignatures().set(setupFiles.getClientSignaturesFile(minecraftVersion));
					parameters.getNests().set(nests.getClientNestsFile(minecraftVersion));
				});
			}
			if (minecraftVersion.hasServer()) {
				workQueue.submit(MakeSourceJar.class, parameters -> {
					parameters.getInput().set(gameJars.getServerJar(minecraftVersion));
					parameters.getOutput().set(sourceJars.getServerJar(minecraftVersion));
					parameters.getLibraries().set(libraries.getLibraries(minecraftVersion));
					parameters.getExceptions().set(setupFiles.getServerExceptionsFile(minecraftVersion));
					parameters.getSignatures().set(setupFiles.getServerSignaturesFile(minecraftVersion));
					parameters.getNests().set(nests.getServerNestsFile(minecraftVersion));
				});
			}
		}
	}

	public interface SourceJarParameters extends WorkParameters {

		Property<File> getInput();

		Property<File> getOutput();

		ListProperty<File> getLibraries();

		Property<File> getExceptions();

		Property<File> getSignatures();

		Property<File> getNests();

	}

	public static abstract class MakeSourceJar implements WorkAction<SourceJarParameters>, Condor, Exceptor, SignaturePatcher, Nester {

		@Override
		public void execute() {
			File input = getParameters().getInput().get();
			File output = getParameters().getOutput().get();
			List<File> libraries = getParameters().getLibraries().get();
			File exceptions = getParameters().getExceptions().get();
			File signatures = getParameters().getSignatures().get();
			File nests = getParameters().getNests().getOrNull();

			File tmp1 = new File(output.getParentFile(), ".tmp1.jar");
			File tmp2 = new File(output.getParentFile(), ".tmp2.jar");

			try {
				File jarIn = input;
				File jarOut = tmp1;

				lvtPatchJar(
					jarIn,
					jarOut,
					libraries,
					true
				);

				jarIn = jarOut;
				jarOut = tmp2;

				exceptionsPatchJar(
					jarIn,
					jarOut,
					exceptions
				);

				jarIn = jarOut;
				jarOut = tmp1;

				signaturePatchJar(
					jarIn,
					jarOut,
					signatures
				);
				if (nests != null) {
					jarIn = jarOut;
					jarOut = tmp2;

					nestJar(
						jarIn,
						jarOut,
						nests
					);
				}

				Files.copy(jarOut, output);

				tmp1.delete();
				tmp2.delete();
			} catch (IOException e) {
				throw new RuntimeException("error while making source jar", e);
			}
		}
	}
}
