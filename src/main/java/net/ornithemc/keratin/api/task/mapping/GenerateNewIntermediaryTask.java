package net.ornithemc.keratin.api.task.mapping;

import java.io.IOException;

import org.gradle.workers.WorkQueue;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.settings.BuildNumbers;
import net.ornithemc.keratin.files.GlobalCache;
import net.ornithemc.keratin.files.GlobalCache.GameJarsCache;
import net.ornithemc.keratin.files.GlobalCache.LibrariesCache;
import net.ornithemc.keratin.files.GlobalCache.NestsCache;
import net.ornithemc.keratin.files.IntermediaryDevelopmentFiles;
import net.ornithemc.keratin.files.OrnitheFiles;
import net.ornithemc.keratin.matching.Matches;

public abstract class GenerateNewIntermediaryTask extends GenerateIntermediaryTask {

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws IOException {
		getProject().getLogger().lifecycle(":generating intermediary for Minecraft " + minecraftVersion.id());

		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		GlobalCache globalCache = files.getGlobalCache();
		GameJarsCache gameJars = globalCache.getGameJarsCache();
		NestsCache nests = globalCache.getNestsCache();
		LibrariesCache libraries = globalCache.getLibrariesCache();
		IntermediaryDevelopmentFiles intermediary = files.getIntermediaryDevelopmentFiles();

		if (!minecraftVersion.hasClient() || !minecraftVersion.hasServer()) {
			throw new IllegalStateException("generating intermediary for client-only/server-only versions is not supported");
		}

		BuildNumbers nestsBuilds = keratin.getNestsBuilds(minecraftVersion);

		if (minecraftVersion.hasSharedObfuscation()) {
			IntermediaryUtil.MergedArgsBuilder args = mergedArgs(minecraftVersion)
				.newJarFile(gameJars.getMergedJar(minecraftVersion))
				.newNests(nests.getMergedNestsFile(minecraftVersion, nestsBuilds))
				.newLibraries(libraries.getLibraries(minecraftVersion))
				.newIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.id()));

			IntermediaryUtil.generateMappings(args.build());
		} else {
			Matches matches = keratin.findMatches("client", minecraftVersion.client().id(), "server", minecraftVersion.server().id());

			IntermediaryUtil.SplitArgsBuilder args = splitArgs(minecraftVersion)
				.newClientJarFile(gameJars.getClientJar(minecraftVersion))
				.newClientNests(nests.getClientNestsFile(minecraftVersion, nestsBuilds))
				.newClientLibraries(libraries.getLibraries(minecraftVersion.client().id()))
				.newServerJarFile(gameJars.getServerJar(minecraftVersion))
				.newServerNests(nests.getServerNestsFile(minecraftVersion, nestsBuilds))
				.newServerLibraries(libraries.getLibraries(minecraftVersion.server().id()))
				.clientServerMatchesFile(matches.file(), matches.inverted());

			if (minecraftVersion.hasSharedVersioning()) {
				args.newIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.id()));
			} else {
				args
					.newClientIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.client().id()))
					.newServerIntermediaryFile(intermediary.getTinyV1MappingsFile(minecraftVersion.server().id()));
			}

			IntermediaryUtil.generateMappings(args.build());
		}
	}
}
