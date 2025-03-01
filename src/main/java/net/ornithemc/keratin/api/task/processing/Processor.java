package net.ornithemc.keratin.api.task.processing;

import java.io.File;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import com.google.common.io.Files;

public interface Processor {

	interface MinecraftProcessorParameters extends WorkParameters {

		Property<File> getInputJar();

		ListProperty<File> getLibraries();

		Property<Boolean> getObfuscateVariableNames();

		Property<File> getLvtPatchedJar();

		Property<File> getRavenFile();

		Property<File> getExceptionsPatchedJar();

		Property<File> getSparrowFile();

		Property<File> getSignaturePatchedJar();

		Property<File> getPreenedJar();

		Property<File> getNestsFile();

		Property<File> getNestedJar();

		Property<File> getOutputJar();

	}

	abstract class ProcessMinecraft implements WorkAction<MinecraftProcessorParameters>, Condor, Exceptor, SignaturePatcher, Preen, Nester {

		@Override
		public void execute() {
			try {
				File data;
				File jarIn;
				File jarOut = getParameters().getInputJar().get();

				data = null;

				{
					jarIn = jarOut;
					jarOut = getParameters().getLvtPatchedJar().get();
					List<File> libraries = getParameters().getLibraries().get();
					boolean obfuscateNames = getParameters().getObfuscateVariableNames().get();

					lvtPatchJar(jarIn, jarOut, libraries, obfuscateNames);
				}

				data = getParameters().getRavenFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getExceptionsPatchedJar().get();

					exceptionsPatchJar(jarIn, jarOut, data);
				}

				data = getParameters().getSparrowFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getSignaturePatchedJar().get();

					signaturePatchJar(jarIn, jarOut, data);
				}

				data = null;

				{
					jarIn = jarOut;
					jarOut = getParameters().getPreenedJar().get();

					modifyMergedBridgeMethodsAccess(jarIn, jarOut);
				}

				data = getParameters().getNestsFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getNestedJar().get();

					nestJar(jarIn, jarOut, data);
				}

				jarIn = jarOut;
				jarOut = getParameters().getOutputJar().get();

				if (jarOut.exists()) {
					jarOut.delete();
				}

				Files.copy(jarIn, jarOut);
			} catch (Exception e) {
				throw new RuntimeException("error while processing Minecraft", e);
			}
		}
	}
}
