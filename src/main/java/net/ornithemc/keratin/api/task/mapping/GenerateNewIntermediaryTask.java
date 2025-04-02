package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;

import org.gradle.workers.WorkQueue;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.matching.Matches;

public abstract class GenerateNewIntermediaryTask extends GenerateIntermediaryTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":generating intermediary for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();

		if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
			throw new IllegalStateException("generating intermediary for client-only/server-only versions is not supported");
		}

		File dir = files.getMappingsDirectory();

		if (minecraftVersion.hasSharedObfuscation()) {
			IntermediaryUtil.MergedArgsBuilder args = mergedArgs(minecraftVersion)
				.newJarFile(files.getMergedJar(minecraftVersion))
				.newNests(files.getMergedNestsFile(minecraftVersion))
				.newLibraries(files.getLibraries(minecraftVersion))
				.newIntermediaryFile(new File(dir, "%s.tiny".formatted(minecraftVersion.id())));

			IntermediaryUtil.generateMappings(args.build());
		} else {
			Matches matches = keratin.findMatches("client", minecraftVersion.client().id(), "server", minecraftVersion.server().id());

			IntermediaryUtil.SplitArgsBuilder args = splitArgs(minecraftVersion)
				.newClientJarFile(files.getClientJar(minecraftVersion))
				.newClientNests(files.getClientNestsFile(minecraftVersion))
				.newClientLibraries(files.getLibraries(minecraftVersion.client().id()))
				.newServerJarFile(files.getServerJar(minecraftVersion))
				.newServerNests(files.getServerNestsFile(minecraftVersion))
				.newServerLibraries(files.getLibraries(minecraftVersion.server().id()))
				.clientServerMatchesFile(matches.file(), matches.inverted());

			if (minecraftVersion.hasSharedVersioning()) {
				args.newIntermediaryFile(new File(dir, "%s.tiny".formatted(minecraftVersion.id())));
			} else {
				args
					.newClientIntermediaryFile(new File(dir, "%s.tiny".formatted(minecraftVersion.client().id())))
					.newServerIntermediaryFile(new File(dir, "%s.tiny".formatted(minecraftVersion.server().id())));
			}

			IntermediaryUtil.generateMappings(args.build());
		}
	}
}
