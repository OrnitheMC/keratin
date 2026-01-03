package net.ornithemc.keratin.api.task.minecraft;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;

public abstract class StripMinecraftJarsTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GameJarsCache gameJars = files.getGlobalCache().getGameJarsCache();

		if (minecraftVersion.hasClient()) {
			File junkJar = gameJars.getClientJarWithJunk(minecraftVersion);
			File strippedJar = gameJars.getClientJar(minecraftVersion);
			String filter = getClassFilter(minecraftVersion.client());

			workQueue.submit(StripJarFilter.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getInput().set(junkJar);
				parameters.getOutput().set(strippedJar);
				parameters.getFilter().set(filter);
			});
		}
		if (minecraftVersion.hasServer()) {
			File junkJar = gameJars.getServerJarWithJunk(minecraftVersion);
			File strippedJar = gameJars.getServerJar(minecraftVersion);
			String filter = getClassFilter(minecraftVersion.server());

			workQueue.submit(StripJarFilter.class, parameters -> {
				parameters.getOverwrite().set(keratin.isCacheInvalid());
				parameters.getInput().set(junkJar);
				parameters.getOutput().set(strippedJar);
				parameters.getFilter().set(filter);
			});
		}
	}

	private String getClassFilter(VersionDetails minecraftVersion) {
		if (minecraftVersion.compareTo("1.5.2") > 0) {
			// for later versions, mc classes are in default package,
			// or in net/minecraft/, or in com/mojang/
			return "^((?!/).)*$|^net/minecraft/.*$|^com/mojang/.*$";
		} else {
			return ".*"; // default filter: allow anything
		}
	}

	public static interface StripJarParameters extends WorkParameters {

		Property<Boolean> getOverwrite();

		Property<File> getInput();

		Property<File> getOutput();

		Property<String> getFilter();

	}

	public static abstract class StripJarFilter implements WorkAction<StripJarParameters> {

		@Override
		public void execute() {
			boolean overwrite = getParameters().getOverwrite().get();
			File input = getParameters().getInput().get();
			File output = getParameters().getOutput().get();
			String filter = getParameters().getFilter().get();

			try {
				if (KeratinGradleExtension.validateOutput(output, overwrite)) {
					return;
				}

				copyJarWithClassFilter(input, output, filter);
			} catch (IOException e) {
				throw new UncheckedIOException("error while copying jar with class filter", e);
			}
		}

		private void copyJarWithClassFilter(File input, File output, String filter) throws IOException {
			try (JarOutputStream jos = new JarOutputStream(new FileOutputStream(output))) {
				byte[] buffer = new byte[4096];
				int read = 0;

				try (JarInputStream jis = new JarInputStream(new FileInputStream(input))) {
					for (JarEntry entry; (entry = jis.getNextJarEntry()) != null; ) {
						String fileName = entry.getName();

						if (!fileName.endsWith(".class")) {
							continue;
						}

						String className = fileName.substring(0, fileName.length() - ".class".length());

						if (!className.matches(filter)) {
							continue;
						}

						jos.putNextEntry(new JarEntry(entry.getName()));

						while ((read = jis.read(buffer)) > 0) {
							jos.write(buffer, 0, read);
						}

						jos.flush();
						jos.closeEntry();
					}
				}

				jos.finish();
			} catch (IOException e) {
				try {
					output.delete();
				} catch (Exception ee) {
					new IOException("unable to delete corrupted jar file after error", ee).printStackTrace();
				}

				throw new IOException("error while copying " + input.getName() + " to " + output.getName() + " with class filter " + filter, e);
			}
		}
	}
}
