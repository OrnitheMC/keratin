package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;

import net.ornithemc.keratin.api.task.TaskAware;
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

public interface MappingsGraph extends TaskAware {

	Format GRAPH_FORMAT = Format.TINY_V2;

	default void generateDummyMappings(File jar, String classNamePattern, File output) throws IOException {
		MappingUtils.generateDummyMappings(GRAPH_FORMAT, MappingNamespace.INTERMEDIARY, MappingNamespace.NAMED, classNamePattern, jar.toPath(), output.toPath());
	}

	default void diffDummyMappings(File mappingsA, File mappingsB, File diffs) throws IOException {
		MappingUtils.diffMappings(GRAPH_FORMAT, mappingsA.toPath(), mappingsB.toPath(), diffs.toPath());
	}

	default void resetGraph(File graphDir, String rootMinecraftVersion, File rootMinecraftJar, String classNamePattern) throws IOException {
		File rootMappings = new File(graphDir, "%s.tiny".formatted(rootMinecraftVersion));

		if (graphDir.exists()) {
			FileUtils.forceDelete(graphDir);
		}
		graphDir.mkdirs();

		generateDummyMappings(rootMinecraftJar, classNamePattern, rootMappings);
	}

	default void extendGraph(File graphDir, String minecraftVersion, String fromMinecraftVersion, String fromFromMinecraftVersion, File jar, File fromJar, File fromFromJar, String classNamePattern) throws IOException {
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());

		if (graph.hasVersion(minecraftVersion)) {
			throw new RuntimeException("cannot extend graph to " + minecraftVersion + "S: version already exists in graph!");
		}
		if (!graph.hasVersion(fromMinecraftVersion)) {
			throw new RuntimeException("cannot extend graph from " + fromMinecraftVersion + ": version does not exist in the graph!");
		}
		if (fromFromMinecraftVersion != null && !graph.hasVersion(fromFromMinecraftVersion)) {
			throw new RuntimeException("cannot extend graph from " + fromFromMinecraftVersion + ": version does not exist in the graph!");
		}

		if (!Files.exists(fromJar.toPath())) {
			throw new RuntimeException("cannot extend graph from " + fromMinecraftVersion + ": no processed intermediary mapped jar provided!");
		}
		if (fromFromMinecraftVersion != null && !Files.exists(fromFromJar.toPath())) {
			throw new RuntimeException("cannot extend graph from " + fromFromMinecraftVersion + ": no processed intermediary mapped jar found!");
		}

		File diff1 = new File(graphDir, "%s#%s.tinydiff".formatted(fromFromMinecraftVersion, minecraftVersion));
		File diff2 = fromFromMinecraftVersion == null ? null : new File(graphDir, "%s#%s.tinydiff".formatted(fromFromMinecraftVersion, fromMinecraftVersion));

		File tmpGraphDir = new File(".mappings");
		File dummy = new File(tmpGraphDir, "%s.tiny".formatted(minecraftVersion));
		File fromDummy = new File(tmpGraphDir, "%s.tiny".formatted(fromMinecraftVersion));
		File fromFromDummy = fromFromMinecraftVersion == null ? null : new File(tmpGraphDir, "%s.tiny".formatted(fromFromMinecraftVersion));
		File tmpDiff = new File(tmpGraphDir, "%s#%s.tinydiff".formatted(fromMinecraftVersion, minecraftVersion));
		File tmpDiff1 = fromFromMinecraftVersion == null ? null : new File(tmpGraphDir, "%s#%s.tinydiff".formatted(fromFromMinecraftVersion, minecraftVersion));
		File tmpDiff2 = fromFromMinecraftVersion == null ? null : new File(tmpGraphDir, "%s#%s.tinydiff".formatted(fromFromMinecraftVersion, fromMinecraftVersion));

		if (tmpGraphDir.exists()) {
			FileUtils.forceDelete(tmpGraphDir);
		}
		tmpGraphDir.mkdirs();

		generateDummyMappings(jar, classNamePattern, dummy);
		generateDummyMappings(fromJar, classNamePattern, fromDummy);
		if (fromFromMinecraftVersion != null) {
			generateDummyMappings(fromFromJar, classNamePattern, fromFromDummy);
		}
		diffDummyMappings(fromDummy, dummy, tmpDiff);
		if (fromFromMinecraftVersion != null) {
			diffDummyMappings(fromFromDummy, dummy, tmpDiff1);
			diffDummyMappings(fromFromDummy, fromDummy, tmpDiff2);
		}

		FileUtils.forceDelete(dummy);
		if (fromFromMinecraftVersion != null) {
			FileUtils.forceDelete(fromDummy);
		}

		VersionGraph tmpGraph = VersionGraph.of(Format.TINY_V2, tmpGraphDir.toPath());

		if (fromFromMinecraftVersion != null) {
			Mappings mappings = MappingUtils.separateMappings(graph, fromFromMinecraftVersion);
			Mappings tmpMappings = MappingUtils.separateMappings(tmpGraph, fromFromMinecraftVersion);
			MappingsDiff changes = MappingUtils.diffMappings(tmpMappings, mappings);

			saveChanges(tmpGraph, changes, fromFromMinecraftVersion);
		}

		Mappings mappings = MappingUtils.separateMappings(graph, fromMinecraftVersion);
		Mappings tmpMappings = MappingUtils.separateMappings(tmpGraph, fromMinecraftVersion);
		MappingsDiff changes = MappingUtils.diffMappings(tmpMappings, mappings);

		saveChanges(tmpGraph, changes, fromMinecraftVersion);

		Files.copy(tmpDiff2.toPath(), diff2.toPath());
		if (fromFromMinecraftVersion != null) {
			Files.copy(tmpDiff1.toPath(), diff1.toPath());
		}

		FileUtils.forceDelete(tmpGraphDir);
	}

	private static void saveChanges(VersionGraph graph, MappingsDiff changes, String minecraftVersion) throws IOException {
		MappingUtils.insertMappings(new PropagationOptions.Builder().lenient().build(), graph, changes, minecraftVersion);
	}

	default void loadMappings(String minecraftVersion, File graphDir, File output, Format outputFormat) throws IOException {
		loadMappings(minecraftVersion, graphDir, output, outputFormat, null);
	}

	default void loadMappings(String minecraftVersion, File graphDir, File output, Format outputFormat, MappingValidator validator) throws IOException {
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());
		Mappings mappings = MappingUtils.separateMappings(graph, minecraftVersion);

		if (validator != null) {
			mappings.setValidator(Validators.REMOVE_DUMMY_MAPPINGS);
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
		changes.setValidator(Validators.INSERT_DUMMY_MAPPINGS);

		PropagationOptions options = new PropagationOptions.Builder().setPropagationDirection(propagationDir).lenient().build();
		MappingUtils.insertMappings(options, graph, changes, minecraftVersion);

		FileUtils.forceDelete(input);
	}
}
