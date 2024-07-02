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

			try {
				Merger._mergeJars(client, server, merged);
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
				Merger._mergeNests(client, server, merged);
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
				Merger._mergeSparrow(client, server, merged);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running merger", e);
			}
		}
	}

	default void mergeJars(File client, File server, File merged) throws IOException {
		_mergeJars(client, server, merged);
	}

	static void _mergeJars(File client, File server, File merged) throws IOException {
		try (JarMerger merger = new JarMerger(client, server, merged)) {
			merger.merge();
		}
	}

	default void mergeNests(File client, File server, File merged) throws IOException {
		_mergeNests(client, server, merged);
	}

	static void _mergeNests(File client, File server, File merged) throws IOException {
		MappingUtils.mergeNests(client.toPath(), server.toPath(), merged.toPath());
	}

	default void mergeSparrow(File client, File server, File merged) throws IOException {
		_mergeSparrow(client, server, merged);
	}

	static void _mergeSparrow(File client, File server, File merged) throws IOException {
		MappingUtils.mergeSignatures(client.toPath(), server.toPath(), merged.toPath());
	}
}
