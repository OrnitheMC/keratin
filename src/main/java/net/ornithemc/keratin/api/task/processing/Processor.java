package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;

public interface Processor {

	interface MinecraftProcessorParameters extends WorkParameters {

		Property<Boolean> getOverwrite();

		Property<File> getInputJar();

		Property<File> getOutputJar();

		ListProperty<File> getLibraries();

		Property<Boolean> getObfuscateVariableNames();

		Property<File> getExceptionsFile();

		Property<File> getSignaturesFile();

		Property<File> getNestsFile();

	}

	abstract class ProcessMinecraft implements WorkAction<MinecraftProcessorParameters>, Condor, Exceptor, SignaturePatcher, Preen, Nester {

		@Override
		public void execute() {
			boolean overwrite = getParameters().getOverwrite().get();

			File data;
			File jarIn = getParameters().getInputJar().get();
			File jarOut = getParameters().getOutputJar().get();

			try {
				if (KeratinGradleExtension.validateOutput(jarOut, overwrite)) {
					return;
				}

				{
					Files.copy(jarIn, jarOut);
				}

				data = null;

				{
					jarIn = jarOut;
					jarOut = getParameters().getOutputJar().get();
					List<File> libraries = getParameters().getLibraries().get();
					boolean obfuscateNames = getParameters().getObfuscateVariableNames().get();

					lvtPatchJar(jarIn, jarOut, libraries, obfuscateNames);
				}

				data = getParameters().getExceptionsFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getOutputJar().get();

					exceptionsPatchJar(jarIn, jarOut, data);
				}

				data = getParameters().getSignaturesFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getOutputJar().get();

					signaturePatchJar(jarIn, jarOut, data);
				}

				data = null;

				{
					jarIn = jarOut;
					jarOut = getParameters().getOutputJar().get();

					modifyMergedBridgeMethodsAccess(jarIn, jarOut);
				}

				data = getParameters().getNestsFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getOutputJar().get();

					nestJar(jarIn, jarOut, data);
				}
			} catch (Exception e) {
				throw new RuntimeException("error while processing Minecraft", e);
			}
		}
	}
}
