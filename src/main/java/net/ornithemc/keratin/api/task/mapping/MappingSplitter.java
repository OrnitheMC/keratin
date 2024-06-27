package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import net.fabricmc.mappingio.MappingReader;
import net.fabricmc.mappingio.MappingVisitor;
import net.fabricmc.mappingio.MappingWriter;
import net.fabricmc.mappingio.adapter.MappingDstNsReorder;
import net.fabricmc.mappingio.adapter.MappingNsRenamer;
import net.fabricmc.mappingio.adapter.MappingSourceNsSwitch;
import net.fabricmc.mappingio.format.MappingFormat;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public interface MappingSplitter {

	interface MappingSplitterParameters extends WorkParameters {

		Property<File> getMerged();

		Property<File> getClient();

		Property<File> getServer();

	}

	abstract class SplitMappingsAction implements WorkAction<MappingSplitterParameters> {

		@Override
		public void execute() {
			try {
				File merged = getParameters().getMerged().get();
				File client = getParameters().getClient().getOrNull();
				File server = getParameters().getServer().getOrNull();

				MemoryMappingTree mergedMappings = new MemoryMappingTree();
				MappingReader.read(merged.toPath(), mergedMappings);

				if (client != null) {
					try (MappingWriter writer = MappingWriter.create(client.toPath(), MappingFormat.TINY_2_FILE)) {
						MappingVisitor visitor = new MappingDstNsReorder(writer, "intermediary");
						visitor = new MappingSourceNsSwitch(visitor, "official", true);
						visitor = new MappingNsRenamer(visitor, Map.of("clientOfficial", "official"));
						mergedMappings.accept(visitor);
					}
				}
				if (server != null) {
					try (MappingWriter writer = MappingWriter.create(server.toPath(), MappingFormat.TINY_2_FILE)) {
						MappingVisitor visitor = new MappingDstNsReorder(writer, "intermediary");
						visitor = new MappingSourceNsSwitch(visitor, "official", true);
						visitor = new MappingNsRenamer(visitor, Map.of("serverOfficial", "official"));
						mergedMappings.accept(visitor);
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException("error while running mapping splitter", e);
			}
		}
	}
}
