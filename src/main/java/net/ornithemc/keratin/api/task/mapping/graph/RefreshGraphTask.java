package net.ornithemc.keratin.api.task.mapping.graph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import com.google.common.io.Files;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.api.task.processing.Nester;
import net.ornithemc.keratin.cache.BuildNumbersCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.mappingutils.PropagationDirection;
import net.ornithemc.mappingutils.io.Format;
import net.ornithemc.mappingutils.io.diff.graph.Version;
import net.ornithemc.mappingutils.io.diff.graph.VersionGraph;

public abstract class RefreshGraphTask extends KeratinTask implements MappingsGraph {

	@Internal
	public abstract Property<String> getClassNamePattern();

	@TaskAction
	public void run() throws IOException {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();

		File graphDir = mappings.getMappingsDirectory();
		File tmpDir = new File(graphDir.getParentFile(), ".tmp.refresh." + graphDir.getName());

		tmpDir.mkdirs();

		Set<MinecraftVersion> minecraftVersions = getMinecraftVersions(graphDir);
		BuildNumbersCache nestsBuilds = keratin.getNestsBuilds();

		loadMappings(graphDir, tmpDir, minecraftVersions);
		rebuildGraph(graphDir, getClassNamePattern().get());
		upgradeMappings(graphDir, tmpDir, minecraftVersions, nestsBuilds.getBackup(), nestsBuilds);

		FileUtils.forceDelete(tmpDir);
	}

	private Set<MinecraftVersion> getMinecraftVersions(File graphDir) throws IOException {
		KeratinGradleExtension keratin = getExtension();

		Set<MinecraftVersion> minecraftVersions = new HashSet<>();
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());

		graph.walk(v -> {
			minecraftVersions.add(keratin.getMinecraftVersion(v.toString()));
		}, p -> { });

		return minecraftVersions;
	}

	private void loadMappings(File graphDir, File tmpDir, Set<MinecraftVersion> minecraftVersions) throws IOException {
		for (MinecraftVersion minecraftVersion : minecraftVersions) {
			loadMappings(
				minecraftVersion.id(),
				graphDir,
				new File(tmpDir, "old." + minecraftVersion.id()),
				Format.TINY_V2
			);
		}
	}

	private void rebuildGraph(File graphDir, String classNamePattern) throws IOException {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();

		MinecraftVersion rootMinecraftVersion = null;
		File rootMinecraftJar = null;

		Map<String, Set<String>> blueprint = getBuildPlan(graphDir);

		for (Map.Entry<String, Set<String>> e : blueprint.entrySet()) {
			String version = e.getKey();

			if (rootMinecraftVersion == null) {
				rootMinecraftVersion = keratin.getMinecraftVersion(version);
				rootMinecraftJar = processedJars.getProcessedIntermediaryJar(rootMinecraftVersion, keratin.getProcessorSettings(rootMinecraftVersion));

				startGraph(graphDir, rootMinecraftVersion, rootMinecraftJar, classNamePattern);
			} else {
				Set<String> parents = e.getValue();

				MinecraftVersion minecraftVersion = keratin.getMinecraftVersion(version);
				File minecraftJar = processedJars.getProcessedIntermediaryJar(minecraftVersion, keratin.getProcessorSettings(minecraftVersion));

				List<MinecraftVersion> fromMinecraftVersions = new ArrayList<>();
				List<File> fromMinecraftJars = new ArrayList<>();

				for (String parent : parents) {
					MinecraftVersion fromMinecraftVersion = keratin.getMinecraftVersion(parent);

					fromMinecraftVersions.add(fromMinecraftVersion);
					fromMinecraftJars.add(processedJars.getProcessedIntermediaryJar(fromMinecraftVersion, keratin.getProcessorSettings(fromMinecraftVersion)));
				}

				extendGraph(graphDir, minecraftVersion, fromMinecraftVersions, minecraftJar, fromMinecraftJars, classNamePattern);
			}
		}
	}

	private Map<String, Set<String>> getBuildPlan(File graphDir) throws IOException {
		Map<String, Set<String>> buildplan = new LinkedHashMap<>();
		VersionGraph graph = VersionGraph.of(GRAPH_FORMAT, graphDir.toPath());

		graph.walk(v -> {
			buildplan.computeIfAbsent(v.toString(), key -> {
				Set<String> parents = new LinkedHashSet<>();

				for (Version p : v.getParents()) {
					parents.add(p.toString());
				}

				return parents;
			});
		}, p -> { });

		return buildplan;
	}

	private void upgradeMappings(File graphDir, File tmpDir, Set<MinecraftVersion> minecraftVersions, BuildNumbersCache oldNestsBuilds, BuildNumbersCache nestsBuilds) throws IOException {
		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		NestsCache nestsCache = files.getGlobalCache().getNestsCache();

		for (MinecraftVersion minecraftVersion : minecraftVersions) {
			File oldMappings = new File(tmpDir, "old." + minecraftVersion.id());
			File mappings = new File(tmpDir, minecraftVersion.id());

			BuildNumbers oldBuilds = oldNestsBuilds.getBuildNumbers(minecraftVersion);
			BuildNumbers builds = nestsBuilds.getBuildNumbers(minecraftVersion);

			File oldNests = oldBuilds.isNone() ? null : nestsCache.getMainIntermediaryNestsFile(minecraftVersion, oldBuilds);
			File nests = builds.isNone() ? null : nestsCache.getMainIntermediaryNestsFile(minecraftVersion, builds);

			if (Objects.equals(oldNests, nests)) { // both null or same file
				Files.copy(oldMappings, mappings);
			} else {
				File notNestedMappings = new File(tmpDir, "not-nested." + minecraftVersion.id());

				if (oldNests == null) {
					Files.copy(oldMappings, notNestedMappings);
				} else {
					Nester._unnestMappings(oldMappings, notNestedMappings, oldNests);
				}

				if (nests == null) {
					Files.copy(notNestedMappings, mappings);
				} else {
					Nester._nestMappings(notNestedMappings, mappings, nests);
				}
			}

			saveMappings(minecraftVersion.id(), graphDir, mappings, Format.TINY_V2, Validators.insertDummyMappings(), PropagationDirection.NONE);
		}
	}
}
