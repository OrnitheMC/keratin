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

import net.ornithemc.keratin.KeratinGradleExtension;

public interface MappingSplitter {

	interface MappingSplitterParameters extends WorkParameters {

		Property<Boolean> getOverwrite();

		Property<File> getMerged();

		Property<File> getClient();

		Property<File> getServer();

	}

	abstract class SplitMappingsAction implements WorkAction<MappingSplitterParameters> {

		@Override
		public void execute() {
			boolean overwrite = getParameters().getOverwrite().get();
			File merged = getParameters().getMerged().get();
			File client = getParameters().getClient().getOrNull();
			File server = getParameters().getServer().getOrNull();

			try {
				boolean clientValid = (client == null || KeratinGradleExtension.validateOutput(client, overwrite));
				boolean serverValid = (server == null || KeratinGradleExtension.validateOutput(server, overwrite));

				if (clientValid && serverValid) {
					return;
				}

				MemoryMappingTree mergedMappings = new MemoryMappingTree();
				MappingReader.read(merged.toPath(), mergedMappings);

				if (client != null) {
					try (MappingWriter writer = MappingWriter.create(client.toPath(), MappingFormat.TINY_2_FILE)) {
						MappingVisitor visitor = new MappingDstNsReorder(writer, Mapper.INTERMEDIARY);
						visitor = new MappingSourceNsSwitch(visitor, Mapper.OFFICIAL, true);
						visitor = new MappingNsRenamer(visitor, Map.of(Mapper.CLIENT_OFFICIAL, Mapper.OFFICIAL));
						mergedMappings.accept(visitor);
					}
				}
				if (server != null) {
					try (MappingWriter writer = MappingWriter.create(server.toPath(), MappingFormat.TINY_2_FILE)) {
						MappingVisitor visitor = new MappingDstNsReorder(writer, Mapper.INTERMEDIARY);
						visitor = new MappingSourceNsSwitch(visitor, Mapper.OFFICIAL, true);
						visitor = new MappingNsRenamer(visitor, Map.of(Mapper.SERVER_OFFICIAL, Mapper.OFFICIAL));
						mergedMappings.accept(visitor);
					}
				}
			} catch (IOException e) {
				throw new UncheckedIOException("error while running mapping splitter", e);
			}
		}
	}
}
