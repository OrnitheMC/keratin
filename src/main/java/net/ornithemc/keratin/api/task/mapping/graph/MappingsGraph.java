package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.mappingutils.MappingUtils;
import net.ornithemc.mappingutils.PropagationDirection;
import net.ornithemc.mappingutils.PropagationOptions;
import net.ornithemc.mappingutils.io.Format;
import net.ornithemc.mappingutils.io.MappingNamespace;
import net.ornithemc.mappingutils.io.MappingValidator;
import net.ornithemc.mappingutils.io.Mappings;
import net.ornithemc.mappingutils.io.diff.MappingsDiff;
import net.ornithemc.mappingutils.io.diff.MappingsDiffValidator;
import net.ornithemc.mappingutils.io.diff.graph.VersionGraph;

public interface MappingsGraph {

	Format GRAPH_FORMAT = Format.TINY_V2;

	default void generateDummyMappings(File jar, String classNamePattern, File output) throws IOException {
		MappingUtils.generateDummyMappings(GRAPH_FORMAT, MappingNamespace.INTERMEDIARY, MappingNamespace.NAMED, classNamePattern, jar.toPath(), output.toPath());
	}

	default void diffDummyMappings(File mappingsA, File mappingsB, File diffs) throws IOException {
		MappingUtils.diffMappings(GRAPH_FORMAT, mappingsA.toPath(), mappingsB.toPath(), diffs.toPath());
	}

	default void startGraph(File graphDir, MinecraftVersion rootMinecraftVersion, File rootMinecraftJar, String classNamePattern) throws IOException {
		File rootMappings = new File(graphDir, "%s.tiny".formatted(rootMinecraftVersion.id()));

		if (graphDir.exists()) {
			FileUtils.forceDelete(graphDir);
		}
		graphDir.mkdirs();

		generateDummyMappings(rootMinecraftJar, classNamePattern, rootMappings);
	}

	default void extendGraph(File graphDir, MinecraftVersion minecraftVersion, List<MinecraftVersion> fromMinecraftVersions, File jar, List<File> fromJars, String classNamePattern) throws IOException {
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());

		if (graph.hasVersion(minecraftVersion.id())) {
			throw new RuntimeException("cannot extend graph to " + minecraftVersion.id() + "S: version already exists in graph!");
		}
		for (int i = 0; i < fromMinecraftVersions.size(); i++) {
			MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(i);

			if (!graph.hasVersion(fromMinecraftVersion.id())) {
				throw new RuntimeException("cannot extend graph from " + fromMinecraftVersion.id() + ": version does not exist in the graph!");
			}
			if (!fromJars.get(i).exists()) {
				throw new RuntimeException("cannot extend graph from " + fromMinecraftVersion.id() + ": no processed intermediary mapped jar provided!");
			}
		}

		File tmpDir = new File(graphDir, ".tmp");
		File extendedMappings = new File(tmpDir, "mappings.tiny");
		File tmpGraphDir = new File(graphDir, ".mappings");

		for (int i = 0; i < fromMinecraftVersions.size(); i++) {
			MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(i);
			File fromJar = fromJars.get(i);

			File tmpTiny = new File(tmpGraphDir, "%s.tiny".formatted(minecraftVersion.id()));
			File tmpFromTiny = new File(tmpGraphDir, "%s.tiny".formatted(fromMinecraftVersion.id()));
			File tmpDiff = new File(tmpGraphDir, "%s#%s.tinydiff".formatted(fromMinecraftVersion.id(), minecraftVersion.id()));

			if (tmpGraphDir.exists()) {
				FileUtils.forceDelete(tmpGraphDir);
			}
			tmpGraphDir.mkdirs();

			generateDummyMappings(jar, classNamePattern, tmpTiny);
			generateDummyMappings(fromJar, classNamePattern, tmpFromTiny);
			diffDummyMappings(tmpFromTiny, tmpTiny, tmpDiff);

			FileUtils.forceDelete(tmpTiny);

			if (extendedMappings.exists()) {
				VersionGraph tmpGraph = VersionGraph.of(GRAPH_FORMAT, tmpGraphDir.toPath());

				Mappings current = MappingUtils.separateMappings(tmpGraph, minecraftVersion.id());
				Mappings working = GRAPH_FORMAT.readMappings(extendedMappings.toPath());
				MappingsDiff changes = MappingUtils.diffMappings(current, working);

				saveChanges(tmpGraph, changes, minecraftVersion);
			}

			VersionGraph tmpGraph = VersionGraph.of(GRAPH_FORMAT, tmpGraphDir.toPath());

			Mappings current = MappingUtils.separateMappings(tmpGraph, fromMinecraftVersion.id());
			Mappings working = MappingUtils.separateMappings(graph, fromMinecraftVersion.id());
			MappingsDiff changes = MappingUtils.diffMappings(current, working);

			saveChanges(tmpGraph, changes, fromMinecraftVersion);

			if (extendedMappings.exists()) {
				FileUtils.forceDelete(extendedMappings);
			}
			tmpDir.mkdirs();

			MappingUtils.separateMappings(GRAPH_FORMAT, tmpGraphDir.toPath(), extendedMappings.toPath(), minecraftVersion.id());
		}
		for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
			File diffFile = new File(graphDir, "%s#%s.tinydiff".formatted(fromMinecraftVersion.id(), minecraftVersion.id()));

			Mappings fromMappings = MappingUtils.separateMappings(graph, fromMinecraftVersion.id());
			Mappings mappings = GRAPH_FORMAT.readMappings(extendedMappings.toPath());
			MappingsDiff diff = MappingUtils.diffMappings(fromMappings, mappings);

			GRAPH_FORMAT.writeDiff(diffFile.toPath(), diff);
		}

		FileUtils.forceDelete(tmpDir);
		FileUtils.forceDelete(tmpGraphDir);
	}

	private static void saveChanges(VersionGraph graph, MappingsDiff changes, MinecraftVersion minecraftVersion) throws IOException {
		MappingUtils.insertMappings(new PropagationOptions.Builder().lenient().build(), graph, changes, minecraftVersion.id());
	}

	default void loadMappings(String minecraftVersion, File graphDir, File output, Format outputFormat) throws IOException {
		loadMappings(minecraftVersion, graphDir, output, outputFormat, null);
	}

	default void loadMappings(String minecraftVersion, File graphDir, File output, Format outputFormat, MappingValidator validator) throws IOException {
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());
		Mappings mappings = MappingUtils.separateMappings(graph, minecraftVersion);

		if (validator != null) {
			mappings.setValidator(validator);
		}

		outputFormat.writeMappings(output.toPath(), mappings);
	}

	default void saveMappings(String minecraftVersion, File graphDir, File input, Format inputFormat, PropagationDirection propagationDir) throws IOException {
		saveMappings(minecraftVersion, graphDir, input, inputFormat, null, propagationDir);
	}

	default void saveMappings(String minecraftVersion, File graphDir, File input, Format inputFormat, MappingsDiffValidator validator, PropagationDirection propagationDir) throws IOException {
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());

		Mappings separatedMappings = MappingUtils.separateMappings(graph, minecraftVersion);
		Mappings workingMappings = inputFormat.readMappings(input.toPath());
		// enigma format does not have namespace info...
		workingMappings.setSrcNamespace(separatedMappings.getSrcNamespace());
		workingMappings.setDstNamespace(separatedMappings.getDstNamespace());

		MappingsDiff changes = MappingUtils.diffMappings(separatedMappings, workingMappings);

		if (validator != null) {
			changes.setValidator(validator);
		}

		PropagationOptions options = new PropagationOptions.Builder().setPropagationDirection(propagationDir).lenient().build();
		MappingUtils.insertMappings(options, graph, changes, minecraftVersion);

		FileUtils.forceDelete(input);
	}
}
