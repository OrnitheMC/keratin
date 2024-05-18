package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;

import org.gradle.workers.WorkQueue;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.matching.Matches;

public abstract class GenerateNewIntermediaryTask extends GenerateIntermediaryTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":generating intermediary for Minecraft " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.client() || !details.server()) {
			throw new IllegalStateException("generating intermediary for client-only/server-only versions is not supported");
		}

		File dir = files.getMappingsDirectory();
		File file = new File(dir, "%s.tiny".formatted(minecraftVersion));

		OptionsBuilder options = getOptions(details);

		if (details.sharedMappings()) {
			IntermediaryUtil.generateIntermediary(
				files.getMergedJar(minecraftVersion),
				files.getMergedNests(minecraftVersion),
				files.getLibraries(minecraftVersion),
				file,
				options.build()
			);
		} else {
			Matches matches = keratin.findMatches("client", minecraftVersion, "server", minecraftVersion);

			IntermediaryUtil.generateIntermediary(
				files.getClientJar(minecraftVersion),
				files.getClientNests(minecraftVersion),
				files.getLibraries(minecraftVersion),
				files.getServerJar(minecraftVersion),
				files.getServerNests(minecraftVersion),
				files.getLibraries(minecraftVersion),
				file,
				matches.file(),
				matches.inverted(),
				options.build()
			);
		}
	}
}
