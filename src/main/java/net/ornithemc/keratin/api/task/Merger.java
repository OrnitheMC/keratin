package net.ornithemc.keratin.api.task;

import java.io.File;
import java.io.IOException;

import net.fabricmc.stitch.merge.JarMerger;

public interface Merger extends TaskAware {

	default void mergeJars(File client, File server, File merged) throws IOException {
		try (JarMerger merger = new JarMerger(client, server, merged)) {
			merger.merge();
		}
	}
}
