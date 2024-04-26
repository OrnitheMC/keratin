package net.ornithemc.keratin.api.task.merging;

import java.io.File;
import java.io.IOException;

import net.fabricmc.stitch.merge.JarMerger;

import net.ornithemc.keratin.api.task.TaskAware;
import net.ornithemc.mappingutils.MappingUtils;

public interface Merger extends TaskAware {

	default void mergeJars(File client, File server, File merged) throws IOException {
		try (JarMerger merger = new JarMerger(client, server, merged)) {
			merger.merge();
		}
	}

	default void mergeNests(File client, File server, File merged) throws IOException {
		MappingUtils.mergeNests(client.toPath(), server.toPath(), merged.toPath());
	}

	default void mergeSparrow(File client, File server, File merged) throws IOException {
		MappingUtils.mergeSignatures(client.toPath(), server.toPath(), merged.toPath());
	}
}
