package net.ornithemc.keratin.api.task.mapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.DevelopmentPhase;
import net.ornithemc.keratin.api.JarType;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.IntermediaryDevelopmentFiles;
import net.ornithemc.keratin.files.KeratinFiles;
import net.ornithemc.keratin.matching.Matches;

public abstract class UpdateIntermediaryTask extends GenerateIntermediaryTask {

	@Internal
	public abstract ListProperty<String> getFromMinecraftVersions();

	public void fromMinecraftVersion(String minecraftVersion) {
		fromMinecraftVersions(minecraftVersion);
	}

	public void fromMinecraftVersions(String... minecraftVersions) {
		getFromMinecraftVersions().set(Arrays.asList(minecraftVersions));
	}

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		List<String> fromMinecraftVersionStrings = getFromMinecraftVersions().getOrNull();

		if (fromMinecraftVersionStrings == null || fromMinecraftVersionStrings.isEmpty()) {
			throw new IllegalStateException("no Minecraft version specified to update from");
		}

		KeratinGradleExtension keratin = getExtension();
		KeratinFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		GameJarsCache gameJars = globalCache.getGameJarsCache();
		NestsCache nests = globalCache.getNestsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();
		IntermediaryDevelopmentFiles intermediary = files.getIntermediaryDevelopmentFiles();

		List<MinecraftVersion> fromMinecraftVersions = new ArrayList<>();

		for (String fromMinecraftVersion : fromMinecraftVersionStrings) {
			fromMinecraftVersions.add(MinecraftVersion.parse(keratin, fromMinecraftVersion));
		}

		getProject().getLogger().lifecycle(":updating intermediary from Minecraft " + String.join("/", fromMinecraftVersionStrings) + " to " + minecraftVersion.id());

		if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
			if (!minecraftVersion.hasSharedObfuscation() && fromMinecraftVersions.size() > 2) {
				throw new RuntimeException("updating split intermediary from more than 2 versions to a client-and-server version is not supported");
			}

			boolean fromClient = false;
			boolean fromServer = false;

			for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
				fromClient |= fromMinecraftVersion.hasClient();
				fromServer |= fromMinecraftVersion.hasServer();
			}

			if (!fromClient || !fromServer) {
				throw new RuntimeException("updating intermediary to a client+server version requires both a client and server to update from!");
			}
		} else {
			for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
				if (minecraftVersion.hasClient() && !fromMinecraftVersion.hasClient()) {
					throw new RuntimeException("updating intermediary to a client-only version requires a client to update from!");
				}
				if (minecraftVersion.hasServer() && !fromMinecraftVersion.hasServer()) {
					throw new RuntimeException("updating intermediary to a server-only version requires a client to update from!");
				}
			}
		}

		BuildNumbers nestsBuilds = keratin.getNestsBuilds(minecraftVersion);

		if (minecraftVersion.hasSharedObfuscation() || minecraftVersion.isBefore(DevelopmentPhase.ALPHA)) {
			IntermediaryUtil.MergedArgsBuilder args = mergedArgs(minecraftVersion);

			if (minecraftVersion.canBeMerged()) {
				args
					.newJarFile(gameJars.getMergedJar(minecraftVersion))
					.newNests(nests.getMergedNestsFile(minecraftVersion, nestsBuilds));
			} else {
				if (minecraftVersion.hasClient()) {
					args
						.newJarFile(gameJars.getClientJar(minecraftVersion))
						.newNests(nests.getClientNestsFile(minecraftVersion, nestsBuilds));
				}
				if (minecraftVersion.hasServer()) {
					args
						.newJarFile(gameJars.getServerJar(minecraftVersion))
						.newNests(nests.getServerNestsFile(minecraftVersion, nestsBuilds));
				}
			}

			args
				.newLibraries(libraries.getLibraries(minecraftVersion))
				.newCheckSerializable(minecraftVersion.usesSerializableForLevelSaving())
				.newIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.id()));

			for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
				BuildNumbers fromNestsBuilds = keratin.getNestsBuilds(fromMinecraftVersion);
				Matches m = null;

				if (minecraftVersion.canBeMerged() && fromMinecraftVersion.canBeMerged()) {
					m = keratin.findMatches(JarType.MERGED, fromMinecraftVersion.id(), JarType.MERGED, minecraftVersion.id());
					
					args
						.addOldJarFile(gameJars.getMergedJar(fromMinecraftVersion))
						.addOldNests(nests.getMergedNestsFile(fromMinecraftVersion, fromNestsBuilds));
				} else {
					if (minecraftVersion.hasClient() && fromMinecraftVersion.hasClient()) {
						m = keratin.findMatches(JarType.CLIENT, fromMinecraftVersion.client().id(), JarType.CLIENT, minecraftVersion.client().id());

						args
							.addOldJarFile(gameJars.getClientJar(fromMinecraftVersion))
							.addOldNests(fromMinecraftVersion.canBeMerged()
								? nests.getMergedNestsFile(fromMinecraftVersion, fromNestsBuilds)
								: nests.getClientNestsFile(fromMinecraftVersion, fromNestsBuilds));
					}
					if (minecraftVersion.hasServer() && fromMinecraftVersion.hasServer()) {
						m = keratin.findMatches(JarType.SERVER, fromMinecraftVersion.server().id(), JarType.SERVER, minecraftVersion.server().id());

						args
							.addOldJarFile(gameJars.getServerJar(fromMinecraftVersion))
							.addOldNests(fromMinecraftVersion.canBeMerged()
								? nests.getMergedNestsFile(fromMinecraftVersion, fromNestsBuilds)
								: nests.getServerNestsFile(fromMinecraftVersion, fromNestsBuilds));
					}
				}

				args
					.addOldLibraries(Collections.emptyList())
					.addOldCheckSerializable(fromMinecraftVersion.usesSerializableForLevelSaving())
					.addOldIntermediaryFile(intermediary.getTinyV1MappingsFile(fromMinecraftVersion.id()))
					.addMatchesFile(m.file(), m.inverted());
			}

			IntermediaryUtil.generateMappings(args.build());
		} else {
			IntermediaryUtil.SplitArgsBuilder args = splitArgs(minecraftVersion);

			if (minecraftVersion.hasClient()) {
				args
					.newClientJarFile(gameJars.getClientJar(minecraftVersion))
					.newClientNests(nests.getClientNestsFile(minecraftVersion, nestsBuilds))
					.newClientLibraries(libraries.getLibraries(minecraftVersion.client().id()))
					.newClientCheckSerializable(minecraftVersion.usesSerializableForLevelSaving());
			}
			if (minecraftVersion.hasServer()) {
				args
					.newServerJarFile(gameJars.getServerJar(minecraftVersion))
					.newServerNests(nests.getServerNestsFile(minecraftVersion, nestsBuilds))
					.newServerLibraries(libraries.getLibraries(minecraftVersion.server().id()))
					.newServerCheckSerializable(minecraftVersion.usesSerializableForLevelSaving());
			}
			if (minecraftVersion.hasSharedVersioning()) {
				args.newIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.id()));
			} else {
				if (minecraftVersion.hasClient()) {
					args.newClientIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.client().id()));
				}
				if (minecraftVersion.hasServer()) {
					args.newServerIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.server().id()));
				}
			}
			if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
				Matches m = keratin.findMatches(JarType.CLIENT, minecraftVersion.client().id(), JarType.SERVER, minecraftVersion.server().id());
				args.clientServerMatchesFile(m.file(), m.inverted());
			}

			if (fromMinecraftVersions.size() == 1 && fromMinecraftVersions.get(0).hasSharedObfuscation()) {
				MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(0);
				BuildNumbers fromNestsBuilds = keratin.getNestsBuilds(fromMinecraftVersion);

				args
					.oldJarFile(gameJars.getMergedJar(fromMinecraftVersion))
					.oldNests(nests.getMergedNestsFile(fromMinecraftVersion, fromNestsBuilds))
					.oldCheckSerializable(fromMinecraftVersion.usesSerializableForLevelSaving())
					.oldIntermediaryFile(intermediary.getTinyV1MappingsFile(fromMinecraftVersion.id()));

				if (minecraftVersion.hasClient()) {
					Matches m = keratin.findMatches(JarType.MERGED, fromMinecraftVersion.id(), JarType.CLIENT, minecraftVersion.client().id());
					args.clientMatchesFile(m.file(), m.inverted());
				}
				if (minecraftVersion.hasServer()) {
					Matches m = keratin.findMatches(JarType.MERGED, fromMinecraftVersion.id(), JarType.SERVER, minecraftVersion.server().id());
					args.serverMatchesFile(m.file(), m.inverted());
				}
			} else {
				MinecraftVersion fromClientVersion = null;
				MinecraftVersion fromServerVersion = null;

				if (fromMinecraftVersions.size() == 1) {
					MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(0);

					if (minecraftVersion.hasClient() && fromMinecraftVersion.hasClient()) {
						fromClientVersion = fromMinecraftVersion;
					}
					if (minecraftVersion.hasServer() && fromMinecraftVersion.hasServer()) {
						fromServerVersion = fromMinecraftVersion;
					}
				}
				if (fromMinecraftVersions.size() == 2) {
					MinecraftVersion fromMinecraftVersion0 = fromMinecraftVersions.get(0);
					MinecraftVersion fromMinecraftVersion1 = fromMinecraftVersions.get(1);

					if (minecraftVersion.hasClient()) {
						if (fromMinecraftVersion0.hasClient() && fromMinecraftVersion1.hasServer()) {
							fromClientVersion = fromMinecraftVersion0;
						}
						if (fromMinecraftVersion1.hasClient() && fromMinecraftVersion0.hasServer()) {
							fromClientVersion = fromMinecraftVersion1;
						}
					}
					if (minecraftVersion.hasServer()) {
						if (fromMinecraftVersion0.hasServer() && fromMinecraftVersion1.hasClient()) {
							fromServerVersion = fromMinecraftVersion0;
						}
						if (fromMinecraftVersion1.hasServer() && fromMinecraftVersion0.hasClient()) {
							fromServerVersion = fromMinecraftVersion1;
						}
					}
				}

				if (fromClientVersion != null) {
					BuildNumbers fromNestsBuilds = keratin.getNestsBuilds(fromClientVersion);
					Matches m = keratin.findMatches(JarType.CLIENT, fromClientVersion.client().id(), JarType.CLIENT, minecraftVersion.client().id());

					args
						.oldClientJarFile(gameJars.getClientJar(fromClientVersion))
						.oldClientNests(nests.getClientNestsFile(fromClientVersion, fromNestsBuilds))
						.oldClientCheckSerializable(fromClientVersion.usesSerializableForLevelSaving())
						.oldClientIntermediaryFile(intermediary.getTinyV1MappingsFile(fromClientVersion.client().id()))
						.clientMatchesFile(m.file(), m.inverted());
				}
				if (fromServerVersion != null) {
					BuildNumbers fromNestsBuilds = keratin.getNestsBuilds(fromServerVersion);
					Matches m = keratin.findMatches(JarType.SERVER, fromServerVersion.server().id(), JarType.SERVER, minecraftVersion.server().id());

					args
						.oldServerJarFile(gameJars.getServerJar(fromServerVersion))
						.oldServerNests(nests.getServerNestsFile(fromServerVersion, fromNestsBuilds))
						.oldServerCheckSerializable(fromServerVersion.usesSerializableForLevelSaving())
						.oldServerIntermediaryFile(intermediary.getTinyV1MappingsFile(fromServerVersion.server().id()))
						.serverMatchesFile(m.file(), m.inverted());
				}
			}

			IntermediaryUtil.generateMappings(args.build());
		}
	}
}
