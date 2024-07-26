package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
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
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.matching.Matches;

public abstract class UpdateIntermediaryTask extends GenerateIntermediaryTask {

	@Internal
	public abstract ListProperty<String> getFromMinecraftVersions();

	public void fromMinecraftVersions(String... minecraftVersions) {
		getFromMinecraftVersions().set(Arrays.asList(minecraftVersions));
	}

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws IOException {
		List<String> fromMinecraftVersions = getFromMinecraftVersions().getOrNull();

		if (fromMinecraftVersions == null || fromMinecraftVersions.isEmpty()) {
			throw new IllegalStateException("no Minecraft version specified to update from");
		}

		getProject().getLogger().lifecycle(":updating intermediary from Minecraft " + String.join("/", fromMinecraftVersions) + " to " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		File dir = files.getMappingsDirectory();
		File file = new File(dir, "%s.tiny".formatted(minecraftVersion));
		List<File> fromFiles = new ArrayList<>();
		for (String fromMinecraftVersion : fromMinecraftVersions) {
			fromFiles.add(new File(dir, "%s.tiny".formatted(fromMinecraftVersion)));
		}

		for (int i = 0; i < fromMinecraftVersions.size(); i++) {
			if (!fromFiles.get(i).exists()) {
				throw new IllegalStateException("cannot update intermediary from Minecraft " + fromMinecraftVersions.get(i) + ": no mappings for it exist");
			}
		}

		VersionDetails details = keratin.getVersionDetails(minecraftVersion);
		List<VersionDetails> fromDetailses = new ArrayList<>();
		for (int i = 0; i < fromMinecraftVersions.size(); i++) {
			fromDetailses.add(keratin.getVersionDetails(fromMinecraftVersions.get(i)));
		}

		if (details.sharedMappings()) {
			if (!details.client() || !details.server()) {
				throw new IllegalStateException("updating intermediary to shared-mappings client-only/server-only versions is not supported");
			}
			for (VersionDetails fromDetails : fromDetailses) {
				if (fromDetails.sharedMappings()) {
					if (!fromDetails.client() || !fromDetails.server()) {
						throw new RuntimeException("updating intermediary from shared-mappings client-only/server-only versions to shared-mappings merged versions is not supported");
					}
				} else {
					throw new RuntimeException("updating intermediary from split-mappings versions to shared-mappings versions is not supported");
				}
			}
		} else {
			if (details.client() && details.server()) {
				if (fromDetailses.size() == 1) {
					VersionDetails fromDetails = fromDetailses.get(0);

					if (!details.client() || !details.server()) {
						throw new RuntimeException("update intermediary to a split-mappings version requires both a client and server to update from!");
					}
				} else if (fromDetailses.size() == 2) {
					VersionDetails fromDetails0 = fromDetailses.get(0);
					VersionDetails fromDetails1 = fromDetailses.get(1);

					if ((fromDetails0.client() && !fromDetails1.server()) && (fromDetails0.server() && !fromDetails1.client())) {
						throw new RuntimeException("update intermediary to a split-mappings version requires both a client and server to update from!");
					}
				} else {
					throw new RuntimeException("updating split intermediary from more than 2 versions to a client-and-server version is not supported");
				}
			} else {
				if (fromDetailses.size() == 1) {
					VersionDetails fromDetails = fromDetailses.get(0);

					if (details.client() && !fromDetails.client()) {
						throw new RuntimeException("update intermediary to a split-mappings client-only version requires a client to update from!");
					}
					if (details.server() && !fromDetails.server()) {
						throw new RuntimeException("update intermediary to a split-mappings server-only version requires a server to update from!");
					}
				} else {
					throw new RuntimeException("updating split intermediary from more than 1 version to a client-only or server-only version is not supported");
				}
			}
		}

		OptionsBuilder options = getOptions(details);

		if (details.sharedMappings()) {
			List<File> fromJars = new ArrayList<>();
			List<List<File>> fromLibs = new ArrayList<>();
			List<File> fromIntermediaries = new ArrayList<>();
			List<File> matches = new ArrayList<>();
			boolean[] invertMatches = new boolean[fromMinecraftVersions.size()];

			for (int i = 0; i < fromMinecraftVersions.size(); i++) {
				String fromMinecraftVersion = fromMinecraftVersions.get(i);

				fromJars.add(files.getMergedJar(fromMinecraftVersion));
				fromLibs.add(Collections.emptyList());
				fromIntermediaries.add(fromFiles.get(i));
				Matches m = keratin.findMatches("merged", fromMinecraftVersion, "merged", minecraftVersion);
				matches.add(m.file());
				invertMatches[i] = m.inverted();
			}

			IntermediaryUtil.updateIntermediary(
				fromJars,
				fromLibs,
				files.getMergedJar(minecraftVersion),
				files.getMergedNests(minecraftVersion),
				files.getLibraries(minecraftVersion),
				fromIntermediaries,
				file,
				matches,
				invertMatches,
				options.build()
			);
		} else {
			if (details.client() && details.server()) {
				if (fromDetailses.size() == 1) {
					String fromMinecraftVersion = fromMinecraftVersions.get(0);
					File fromFile = fromFiles.get(0);
					VersionDetails fromDetails = fromDetailses.get(0);

					if (fromDetails.sharedMappings()) {
						Matches clientMatches = keratin.findMatches("merged", fromMinecraftVersion, "client", minecraftVersion);
						Matches serverMatches = keratin.findMatches("merged", fromMinecraftVersion, "server", minecraftVersion);
						Matches clientServerMatches = keratin.findMatches("client", minecraftVersion, "server", minecraftVersion);

						IntermediaryUtil.updateIntermediary(
							files.getMergedJar(fromMinecraftVersion),
							Collections.emptyList(),
							files.getClientJar(minecraftVersion),
							files.getClientNests(minecraftVersion),
							files.getLibraries(minecraftVersion),
							files.getServerJar(minecraftVersion),
							files.getServerNests(minecraftVersion),
							files.getLibraries(minecraftVersion),
							fromFile,
							file,
							clientMatches.file(),
							serverMatches.file(),
							clientServerMatches.file(),
							clientMatches.inverted(),
							serverMatches.inverted(),
							clientServerMatches.inverted(),
							options.build()
						);
					} else {
						Matches clientMatches = keratin.findMatches("client", fromMinecraftVersion, "client", minecraftVersion);
						Matches serverMatches = keratin.findMatches("server", fromMinecraftVersion, "server", minecraftVersion);
						Matches clientServerMatches = keratin.findMatches("client", minecraftVersion, "server", minecraftVersion);

						IntermediaryUtil.updateIntermediary(
							files.getClientJar(fromMinecraftVersion),
							Collections.emptyList(),
							files.getServerJar(fromMinecraftVersion),
							Collections.emptyList(),
							files.getClientJar(minecraftVersion),
							files.getClientNests(minecraftVersion),
							files.getLibraries(minecraftVersion),
							files.getServerJar(minecraftVersion),
							files.getServerNests(minecraftVersion),
							files.getLibraries(minecraftVersion),
							fromFile,
							file,
							clientMatches.file(),
							serverMatches.file(),
							clientServerMatches.file(),
							clientMatches.inverted(),
							serverMatches.inverted(),
							clientServerMatches.inverted(),
							options.build()
						);
					}
				} else if (fromDetailses.size() == 2) {
					VersionDetails fromDetails0 = fromDetailses.get(0);
					VersionDetails fromDetails1 = fromDetailses.get(1);

					String fromClientVersion = null;
					String fromServerVersion = null;
					File fromClientFile = null;
					File fromServerFile = null;

					if (fromDetails0.client()) {
						fromClientVersion = fromMinecraftVersions.get(0);
						fromServerVersion = fromMinecraftVersions.get(1);
						fromClientFile = fromFiles.get(0);
						fromServerFile = fromFiles.get(1);
					}
					if (fromDetails0.server()) {
						fromClientVersion = fromMinecraftVersions.get(1);
						fromServerVersion = fromMinecraftVersions.get(0);
						fromClientFile = fromFiles.get(1);
						fromServerFile = fromFiles.get(0);
					}

					Matches clientMatches = keratin.findMatches("client", fromClientVersion, "client", minecraftVersion);
					Matches serverMatches = keratin.findMatches("server", fromServerVersion, "server", minecraftVersion);
					Matches clientServerMatches = keratin.findMatches("client", minecraftVersion, "server", minecraftVersion);

					IntermediaryUtil.updateIntermediary(
						files.getClientJar(fromClientVersion),
						Collections.emptyList(),
						files.getServerJar(fromServerVersion),
						Collections.emptyList(),
						files.getClientJar(minecraftVersion),
						files.getClientNests(minecraftVersion),
						files.getLibraries(minecraftVersion),
						files.getServerJar(minecraftVersion),
						files.getServerNests(minecraftVersion),
						files.getLibraries(minecraftVersion),
						fromClientFile,
						fromServerFile,
						file,
						clientMatches.file(),
						serverMatches.file(),
						clientServerMatches.file(),
						clientMatches.inverted(),
						serverMatches.inverted(),
						clientServerMatches.inverted(),
						options.build()
					);
				}
			} else {
				if (details.client()) {
					String fromMinecraftVersion = fromMinecraftVersions.get(0);
					File fromFile = fromFiles.get(0);

					Matches clientMatches = keratin.findMatches("client", fromMinecraftVersion, "client", minecraftVersion);

					IntermediaryUtil.updateIntermediary(
						files.getClientJar(fromMinecraftVersion),
						Collections.emptyList(),
						null,
						Collections.emptyList(),
						files.getClientJar(minecraftVersion),
						files.getClientNests(minecraftVersion),
						files.getLibraries(minecraftVersion),
						null,
						null,
						Collections.emptyList(),
						fromFile,
						null,
						file,
						clientMatches.file(),
						null,
						null,
						clientMatches.inverted(),
						false,
						false,
						options.build()
					);
				}
				if (details.server()) {
					String fromMinecraftVersion = fromMinecraftVersions.get(0);
					File fromFile = fromFiles.get(0);

					Matches serverMatches = keratin.findMatches("server", fromMinecraftVersion, "server", minecraftVersion);

					IntermediaryUtil.updateIntermediary(
						null,
						Collections.emptyList(),
						files.getServerJar(fromMinecraftVersion),
						Collections.emptyList(),
						null,
						null,
						Collections.emptyList(),
						files.getServerJar(minecraftVersion),
						files.getServerNests(minecraftVersion),
						files.getLibraries(minecraftVersion),
						null,
						fromFile,
						file,
						null,
						serverMatches.file(),
						null,
						false,
						serverMatches.inverted(),
						false,
						options.build()
					);
				}
			}
		}
	}
}
