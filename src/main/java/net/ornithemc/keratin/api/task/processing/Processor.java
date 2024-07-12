package net.ornithemc.keratin.api.task.processing;

import java.io.File;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import com.google.common.io.Files;

public interface Processor {

	interface MinecraftProcessorParameters extends WorkParameters {

		Property<File> getInputJar();

		Property<File> getRavenFile();

		Property<File> getExceptionsPatchedJar();

		Property<File> getSparrowFile();

		Property<File> getSignaturePatchedJar();

		Property<File> getNestsFile();

		Property<File> getNestedJar();

		Property<File> getOutputJar();

	}

	abstract class ProcessMinecraft implements WorkAction<MinecraftProcessorParameters>, Exceptor, SignaturePatcher, Nester {

		@Override
		public void execute() {
			try {
				File data;
				File jarIn;
				File jarOut = getParameters().getInputJar().get();

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

				data = getParameters().getNestsFile().getOrNull();

				if (data != null) {
					jarIn = jarOut;
					jarOut = getParameters().getNestedJar().get();

					nestJar(jarIn, jarOut, data);
				}

				jarIn = jarOut;
				jarOut = getParameters().getOutputJar().get();

				Files.copy(jarIn, jarOut);
			} catch (Exception e) {
				throw new RuntimeException("error while processing Minecraft", e);
			}
		}
	}
}
