package net.ornithemc.keratin.api.task.merging;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import net.fabricmc.stitch.merge.JarMerger;

import net.ornithemc.keratin.api.task.TaskAware;
import net.ornithemc.mappingutils.MappingUtils;

public interface Merger extends TaskAware {

	interface JarMergerParameters extends WorkParameters {

		Property<File> getClient();

		Property<File> getServer();

		Property<File> getMerged();

	}

	abstract class MergeJars implements WorkAction<JarMergerParameters> {

		@Override
		public void execute() {
			File client = getParameters().getClient().get();
			File server = getParameters().getServer().get();
			File merged = getParameters().getMerged().get();

			try (JarMerger merger = new JarMerger(client, server, merged)) {
				merger.merge();
			} catch (IOException e) {
				throw new UncheckedIOException("error while running merger", e);
			}
		}
	}

	interface NestsAndSparrowMergerParameters extends WorkParameters {

		Property<File> getClient();

		Property<File> getServer();

		Property<File> getMerged();

	}

	abstract class MergeNests implements WorkAction<NestsAndSparrowMergerParameters> {

		@Override
		public void execute() {
			File client = getParameters().getClient().get();
			File server = getParameters().getServer().get();
			File merged = getParameters().getMerged().get();

			try {
				MappingUtils.mergeNests(client.toPath(), server.toPath(), merged.toPath());
			} catch (IOException e) {
				throw new UncheckedIOException("error while running merger", e);
			}
		}
	}

	abstract class MergeSparrow implements WorkAction<NestsAndSparrowMergerParameters> {

		@Override
		public void execute() {
			File client = getParameters().getClient().get();
			File server = getParameters().getServer().get();
			File merged = getParameters().getMerged().get();

			try {
				MappingUtils.mergeSignatures(client.toPath(), server.toPath(), merged.toPath());
			} catch (IOException e) {
				throw new UncheckedIOException("error while running merger", e);
			}
		}
	}
}
