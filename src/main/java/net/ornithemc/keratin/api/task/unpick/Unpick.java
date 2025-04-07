package net.ornithemc.keratin.api.task.unpick;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import daomephsta.unpick.cli.Main;

public interface Unpick {

	interface UnpickParameters extends WorkParameters {

		Property<File> getInputJar();

		Property<File> getUnpickDefinitionsFile();

		Property<File> getUnpickConstantsJar();

		SetProperty<File> getUnpickClasspath();

		Property<File> getOutputJar();

	}

	abstract class UnpickMinecraft implements WorkAction<UnpickParameters>, Unpick {

		@Override
		public void execute() {
			File input = getParameters().getInputJar().get();
			File unpickDefinitions = getParameters().getUnpickDefinitionsFile().get();
			File unpickConstants = getParameters().getUnpickConstantsJar().get();
			Set<File> unpickClasspath = getParameters().getUnpickClasspath().get();
			File output = getParameters().getOutputJar().get();

			try {
				unpickJar(input, output, unpickDefinitions, unpickConstants, unpickClasspath);
			} catch (IOException e) {
				throw new RuntimeException("error while unpicking Minecraft", e);
			}
		}
	}

	default void unpickJar(File input, File output, File unpickDefinitions, File unpickConstants, Collection<File> unpickClasspath) throws IOException {
		_unpickJar(input, output, unpickDefinitions, unpickConstants, unpickClasspath);
	}

	static void _unpickJar(File input, File output, File unpickDefinitions, File unpickConstants, Collection<File> unpickClasspath) throws IOException {
		List<String> args = new ArrayList<>();

		args.add(input.getAbsolutePath());
		args.add(output.getAbsolutePath());
		args.add(unpickDefinitions.getAbsolutePath());
		args.add(unpickConstants.getAbsolutePath());

		for (File jar : unpickClasspath) {
			args.add(jar.getAbsolutePath());
		}

		Main.main(args.toArray(String[]::new));
	}
}
