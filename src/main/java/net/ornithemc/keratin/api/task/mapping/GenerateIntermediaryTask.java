package net.ornithemc.keratin.api.task.mapping;

import java.io.File;
import java.io.IOException;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.manifest.VersionDetails;
import net.ornithemc.keratin.api.task.KeratinTask;
import net.ornithemc.keratin.matching.Matches;

public abstract class GenerateIntermediaryTask extends KeratinTask {

	public abstract Property<String> getMinecraftVersion();

	public abstract Property<String> getTargetNamespace();

	public abstract Property<String> getTargetPackage();

	public abstract ListProperty<String> getObfuscationPatterns();

	public abstract Property<Integer> getNameLength();

	@TaskAction
	public void run() throws IOException {
		String minecraftVersion = getMinecraftVersion().get();

		getProject().getLogger().lifecycle(":generating intermediary for Minecraft version " + minecraftVersion);

		KeratinGradleExtension keratin = getExtension();
		OrnitheFilesAPI files = keratin.getFiles();
		VersionDetails details = keratin.getVersionDetails(minecraftVersion);

		if (!details.client() || !details.server()) {
			throw new IllegalStateException("generating intermediary for client-only/server-only versions is not supported");
		}

		File dir = files.getMappingsDirectory();
		File file = new File(dir, "%s.tiny".formatted(minecraftVersion));

		if (!dir.exists()) {
			dir.mkdirs();
		}

		OptionsBuilder options = new OptionsBuilder();

		if (getTargetNamespace().isPresent())
			options.targetNamespace(getTargetNamespace().get());
		if (getTargetPackage().isPresent())
			options.targetPackage(getTargetPackage().get());
		if (getObfuscationPatterns().isPresent())
			for (String obfuscationPattern : getObfuscationPatterns().get()) {
				options.obfuscationPattern(obfuscationPattern);
			}
		if (getNameLength().isPresent())
			options.nameLength(getNameLength().get());
		if (details.client())
			options.clientHash(details.downloads().get("client").sha1());
		if (details.server())
			options.serverHash(details.downloads().get("server").sha1());

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
