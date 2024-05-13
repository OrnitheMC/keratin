package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.matching.Matches;

public abstract class UpdateIntermediaryTask extends GenerateIntermediaryTask {

	@Internal
	public abstract Property<String> getFromMinecraftVersion();

	@Internal
	public abstract Property<String> getFromFromMinecraftVersion();

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();
		String fromMinecraftVersion = getFromMinecraftVersion().getOrNull();
		String fromFromMinecraftVersion = getFromFromMinecraftVersion().getOrNull();

		if (fromMinecraftVersion == null) {
			throw new IllegalStateException("no Minecraft version specified to update from");
		}

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		File dir = files.getMappingsDirectory();
		File file = new File(dir, "%s.tiny".formatted(minecraftVersion));
		File fromFile = new File(dir, "%s.tiny".formatted(fromMinecraftVersion));
		File fromFromFile = (fromFromMinecraftVersion == null) ? null : new File(dir, "%s.tiny".formatted(fromFromMinecraftVersion));

		if (!fromFile.exists()) {
			throw new IllegalStateException("cannot update intermediary from Minecraft " + fromMinecraftVersion + ": no mappings for it exist");
		}
		if (fromFromMinecraftVersion != null && !fromFromFile.exists()) {
			throw new IllegalStateException("cannot update intermediary from Minecraft " + fromFromMinecraftVersion + ": no mappings for it exist");
		}

		if (fromFromMinecraftVersion == null) {
			getProject().getLogger().lifecycle(":updating intermediary from Minecraft " + fromMinecraftVersion + " to " + minecraftVersion);
		} else {
			getProject().getLogger().lifecycle(":updating intermediary from Minecraft " + fromFromMinecraftVersion + "/" + fromMinecraftVersion + " to " + minecraftVersion);
		}

		VersionDetails details = keratin.getVersionDetails(minecraftVersion);
		VersionDetails fromDetails = keratin.getVersionDetails(fromMinecraftVersion);
		VersionDetails fromFromDetails = (fromFromMinecraftVersion == null) ? null : keratin.getVersionDetails(fromFromMinecraftVersion);

		if (details.sharedMappings() && (!details.client() || !details.server())) {
			throw new IllegalStateException("updating intermediary to shared-mappings client-only/server-only versions is not supported");
		}

		OptionsBuilder options = new OptionsBuilder();

		if (details.sharedMappings()) {
			if (fromDetails.sharedMappings()) {
				if (fromDetails.client() && fromDetails.server()) {
					Matches matches = keratin.findMatches("merged", fromMinecraftVersion, "merged", minecraftVersion);

					IntermediaryUtil.updateIntermediary(
						files.getMergedJar(fromMinecraftVersion),
						Collections.emptyList(),
						files.getMergedJar(minecraftVersion),
						files.getMergedNests(minecraftVersion),
						files.getLibraries(minecraftVersion),
						fromFile,
						file,
						matches.file(),
						matches.inverted(),
						options.build()
					);
				} else {
					throw new RuntimeException("updating intermediary from shared-mappings client-only/server-only versions to shared-mappings merged versions is not supported");
				}
			} else {
				throw new RuntimeException("updating intermediary from separate-mappings versions to shared-mappings versions is not supported");
			}
		} else {
			if (details.client() && details.server()) {
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
					if (fromDetails.client() && fromDetails.server()) {
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
							files.getServerNests(fromMinecraftVersion),
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
						if (fromFromMinecraftVersion == null) {
							throw new RuntimeException("second mapping source is missing");
						}
						if ((fromDetails.client() && !fromFromDetails.server()) || (fromDetails.server() && !fromFromDetails.client())) {
							throw new RuntimeException("incompatible sources");
						}

						if (fromDetails.client()) {
							Matches clientMatches = keratin.findMatches("client", fromMinecraftVersion, "client", minecraftVersion);
							Matches serverMatches = keratin.findMatches("server", fromFromMinecraftVersion, "server", minecraftVersion);
							Matches clientServerMatches = keratin.findMatches("client", minecraftVersion, "server", minecraftVersion);

							IntermediaryUtil.updateIntermediary(
								files.getClientJar(fromMinecraftVersion),
								Collections.emptyList(),
								files.getServerJar(fromFromMinecraftVersion),
								Collections.emptyList(),
								files.getClientJar(minecraftVersion),
								files.getClientNests(minecraftVersion),
								files.getLibraries(minecraftVersion),
								files.getServerJar(minecraftVersion),
								files.getServerNests(minecraftVersion),
								files.getLibraries(minecraftVersion),
								fromFile,
								fromFromFile,
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
							Matches clientMatches = keratin.findMatches("client", fromFromMinecraftVersion, "client", minecraftVersion);
							Matches serverMatches = keratin.findMatches("server", fromMinecraftVersion, "server", minecraftVersion);
							Matches clientServerMatches = keratin.findMatches("client", minecraftVersion, "server", minecraftVersion);

							IntermediaryUtil.updateIntermediary(
								files.getClientJar(fromFromMinecraftVersion),
								Collections.emptyList(),
								files.getServerJar(fromMinecraftVersion),
								Collections.emptyList(),
								files.getClientJar(minecraftVersion),
								files.getClientNests(minecraftVersion),
								files.getLibraries(minecraftVersion),
								files.getServerJar(minecraftVersion),
								files.getServerNests(minecraftVersion),
								files.getLibraries(minecraftVersion),
								fromFromFile,
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
					}
				}
			} else {
				if ((details.client() && !fromDetails.client()) || (details.server() && !fromDetails.server())) {
					throw new RuntimeException("incompatible source");
				}

				if (details.client()) {
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
				} else {
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
