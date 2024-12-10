package net.ornithemc.keratin.api.task.merging;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;
import net.fabricmc.stitch.merge.JarMerger;

import net.ornithemc.keratin.api.task.TaskAware;
import net.ornithemc.keratin.api.task.mapping.Mapper;
import net.ornithemc.mappingutils.MappingUtils;

public interface Merger extends TaskAware {

	interface MergerParameters extends WorkParameters {

		Property<File> getClient();

		Property<File> getServer();

		Property<File> getMerged();

	}

	abstract class MergeAction implements WorkAction<MergerParameters> {

		@Override
		public void execute() {
			File client = getParameters().getClient().get();
			File server = getParameters().getServer().get();
			File merged = getParameters().getMerged().get();

			try {
				run(client, server, merged);
			} catch (IOException e) {
				throw new UncheckedIOException("error while running merger", e);
			}
		}

		protected abstract void run(File client, File server, File merged) throws IOException;

	}

	abstract class MergeIntermediary extends MergeAction {

		@Override
		protected void run(File client, File server, File merged) throws IOException {
			MemoryMappingTree mappings = new MemoryMappingTree();

			MappingReader.read(client.toPath(), new MappingNsRenamer(new MappingSourceNsSwitch(mappings, Mapper.INTERMEDIARY, true), Map.of(Mapper.OFFICIAL, Mapper.CLIENT_OFFICIAL)));
			MappingReader.read(server.toPath(), new MappingNsRenamer(new MappingSourceNsSwitch(mappings, Mapper.INTERMEDIARY, true), Map.of(Mapper.OFFICIAL, Mapper.SERVER_OFFICIAL)));

			try (MappingWriter writer = MappingWriter.create(merged.toPath(), MappingFormat.TINY_2_FILE)) {
				mappings.accept(writer);
			}
		}
	}

	abstract class MergeJars extends MergeAction {

		@Override
		protected void run(File client, File server, File merged) throws IOException {
			Merger._mergeJars(client, server, merged);
		}
	}

	abstract class MergeRaven extends MergeAction {

		@Override
		protected void run(File client, File server, File merged) throws IOException {
			Merger._mergeRaven(client, server, merged);
		}
	}

	abstract class MergeSparrow extends MergeAction {

		@Override
		protected void run(File client, File server, File merged) throws IOException {
			Merger._mergeSparrow(client, server, merged);
		}
	}

	abstract class MergeNests extends MergeAction {

		@Override
		protected void run(File client, File server, File merged) throws IOException {
			Merger._mergeNests(client, server, merged);
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

	default void mergeRaven(File client, File server, File merged) throws IOException {
		_mergeRaven(client, server, merged);
	}

	static void _mergeRaven(File client, File server, File merged) throws IOException {
		MappingUtils.mergeExceptions(client.toPath(), server.toPath(), merged.toPath());
	}

	default void mergeSparrow(File client, File server, File merged) throws IOException {
		_mergeSparrow(client, server, merged);
	}

	static void _mergeSparrow(File client, File server, File merged) throws IOException {
		MappingUtils.mergeSignatures(client.toPath(), server.toPath(), merged.toPath());
	}

	default void mergeNests(File client, File server, File merged) throws IOException {
		_mergeNests(client, server, merged);
	}

	static void _mergeNests(File client, File server, File merged) throws IOException {
		MappingUtils.mergeNests(client.toPath(), server.toPath(), merged.toPath());
	}
}
