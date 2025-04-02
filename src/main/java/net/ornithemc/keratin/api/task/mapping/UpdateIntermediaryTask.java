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
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
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
		OrnitheFilesAPI files = keratin.getFiles();

		List<MinecraftVersion> fromMinecraftVersions = new ArrayList<>();

		for (String fromMinecraftVersion : fromMinecraftVersionStrings) {
			fromMinecraftVersions.add(MinecraftVersion.parse(keratin, fromMinecraftVersion));
		}

		if (minecraftVersion.hasSharedObfuscation()) {
			if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
				throw new IllegalStateException("updating intermediary to shared-mappings client-only/server-only versions is not supported");
			}
			for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
				if (fromMinecraftVersion.hasSharedObfuscation()) {
					if (!fromMinecraftVersion.hasClient() || !fromMinecraftVersion.hasServer()) {
						throw new RuntimeException("updating intermediary from shared-mappings client-only/server-only versions to shared-mappings merged versions is not supported");
					}
				} else {
					throw new RuntimeException("updating intermediary from split-mappings versions to shared-mappings versions is not supported");
				}
			}
		} else {
			if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
				if (fromMinecraftVersions.size() == 1) {
					MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(0);

					if (!fromMinecraftVersion.hasClient() || !fromMinecraftVersion.hasServer()) {
						throw new RuntimeException("update intermediary to a split-mappings version requires both a client and server to update from!");
					}
				} else if (fromMinecraftVersions.size() == 2) {
					MinecraftVersion fromMinecraftVersion0 = fromMinecraftVersions.get(0);
					MinecraftVersion fromMinecraftVersion1 = fromMinecraftVersions.get(1);

					if ((fromMinecraftVersion0.hasClient() && !fromMinecraftVersion1.hasServer()) && (fromMinecraftVersion0.hasServer() && !fromMinecraftVersion1.hasClient())) {
						throw new RuntimeException("update intermediary to a split-mappings version requires both a client and server to update from!");
					}
				} else {
					throw new RuntimeException("updating split intermediary from more than 2 versions to a client-and-server version is not supported");
				}
			} else {
				if (fromMinecraftVersions.size() == 1) {
					MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(0);

					if (minecraftVersion.hasClient() && !fromMinecraftVersion.hasClient()) {
						throw new RuntimeException("update intermediary to a split-mappings client-only version requires a client to update from!");
					}
					if (minecraftVersion.hasServer() && !fromMinecraftVersion.hasServer()) {
						throw new RuntimeException("update intermediary to a split-mappings server-only version requires a server to update from!");
					}
				} else if (fromMinecraftVersions.size() == 2) {
					MinecraftVersion fromMinecraftVersion0 = fromMinecraftVersions.get(0);
					MinecraftVersion fromMinecraftVersion1 = fromMinecraftVersions.get(1);

					if (minecraftVersion.hasClient() && !fromMinecraftVersion0.hasClient() && !fromMinecraftVersion1.hasClient()) {
						throw new RuntimeException("update intermediary to a split-mappings client-only version requires a client to update from!");
					}
					if (minecraftVersion.hasServer() && !fromMinecraftVersion0.hasServer() && !fromMinecraftVersion1.hasServer()) {
						throw new RuntimeException("update intermediary to a split-mappings server-only version requires a server to update from!");
					}
				} else {
					throw new RuntimeException("updating split intermediary from more than 2 versions to a client-only or server-only version is not supported");
				}
			}
		}

		getProject().getLogger().lifecycle(":updating intermediary from Minecraft " + String.join("/", fromMinecraftVersionStrings) + " to " + minecraftVersion.id());

		if (minecraftVersion.hasSharedObfuscation()) {
			IntermediaryUtil.MergedArgsBuilder args = mergedArgs(minecraftVersion)
				.newJarFile(files.getMergedJar(minecraftVersion))
				.newNests(files.getMergedNestsFile(minecraftVersion))
				.newLibraries(files.getLibraries(minecraftVersion))
				.newCheckSerializable(minecraftVersion.usesSerializableForLevelSaving())
				.newIntermediaryFile(files.getIntermediaryFile(minecraftVersion.id()));

			for (MinecraftVersion fromMinecraftVersion : fromMinecraftVersions) {
				Matches m = keratin.findMatches("merged", fromMinecraftVersion.id(), "merged", minecraftVersion.id());

				args
					.addOldJarFile(files.getMergedJar(fromMinecraftVersion))
					.addOldLibraries(Collections.emptyList())
					.addOldCheckSerializable(fromMinecraftVersion.usesSerializableForLevelSaving())
					.addOldIntermediaryFile(files.getIntermediaryFile(fromMinecraftVersion.id()))
					.addMatchesFile(m.file(), m.inverted());
			}

			IntermediaryUtil.generateMappings(args.build());
		} else {
			IntermediaryUtil.SplitArgsBuilder args = splitArgs(minecraftVersion);

			if (minecraftVersion.hasClient()) {
				args
					.newClientJarFile(files.getClientJar(minecraftVersion))
					.newClientNests(files.getClientNestsFile(minecraftVersion))
					.newClientLibraries(files.getLibraries(minecraftVersion.client().id()))
					.newClientCheckSerializable(minecraftVersion.usesSerializableForLevelSaving());
			}
			if (minecraftVersion.hasServer()) {
				args
					.newServerJarFile(files.getServerJar(minecraftVersion))
					.newServerNests(files.getServerNestsFile(minecraftVersion))
					.newServerLibraries(files.getLibraries(minecraftVersion.server().id()))
					.newServerCheckSerializable(minecraftVersion.usesSerializableForLevelSaving());
			}
			if (minecraftVersion.hasSharedVersioning()) {
				args.newIntermediaryFile(files.getIntermediaryFile(minecraftVersion.id()));
			} else {
				if (minecraftVersion.hasClient()) {
					args.newClientIntermediaryFile(files.getIntermediaryFile(minecraftVersion.client().id()));
				}
				if (minecraftVersion.hasServer()) {
					args.newServerIntermediaryFile(files.getIntermediaryFile(minecraftVersion.server().id()));
				}
			}
			if (minecraftVersion.hasClient() && minecraftVersion.hasServer()) {
				Matches m = keratin.findMatches("client", minecraftVersion.client().id(), "server", minecraftVersion.server().id());
				args.clientServerMatchesFile(m.file(), m.inverted());
			}

			if (fromMinecraftVersions.size() == 1 && fromMinecraftVersions.get(0).hasSharedObfuscation()) {
				MinecraftVersion fromMinecraftVersion = fromMinecraftVersions.get(0);

				args
					.oldJarFile(files.getMergedJar(fromMinecraftVersion))
					.oldCheckSerializable(fromMinecraftVersion.usesSerializableForLevelSaving())
					.oldIntermediaryFile(files.getIntermediaryFile(fromMinecraftVersion.id()));

				if (minecraftVersion.hasClient()) {
					Matches m = keratin.findMatches("merged", fromMinecraftVersion.id(), "client", minecraftVersion.client().id());
					args.clientMatchesFile(m.file(), m.inverted());
				}
				if (minecraftVersion.hasServer()) {
					Matches m = keratin.findMatches("merged", fromMinecraftVersion.id(), "server", minecraftVersion.server().id());
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
					Matches m = keratin.findMatches("client", fromClientVersion.client().id(), "client", minecraftVersion.client().id());

					args
						.oldClientJarFile(files.getClientJar(fromClientVersion))
						.oldClientCheckSerializable(fromClientVersion.usesSerializableForLevelSaving())
						.oldClientIntermediaryFile(files.getIntermediaryFile(fromClientVersion.client().id()))
						.clientMatchesFile(m.file(), m.inverted());
				}
				if (fromServerVersion != null) {
					Matches m = keratin.findMatches("server", fromServerVersion.server().id(), "server", minecraftVersion.server().id());

					args
						.oldServerJarFile(files.getServerJar(fromServerVersion))
						.oldServerCheckSerializable(fromServerVersion.usesSerializableForLevelSaving())
						.oldServerIntermediaryFile(files.getIntermediaryFile(fromServerVersion.server().id()))
						.serverMatchesFile(m.file(), m.inverted());
				}
			}

			IntermediaryUtil.generateMappings(args.build());
		}
	}
}
