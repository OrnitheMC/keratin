package net.ornithemc.keratin.api.task.mapping;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import net.fabricmc.stitch.util.IntermediaryUtil;

import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class GenerateIntermediaryTask extends MinecraftTask {

	@Internal
	public abstract Property<String> getTargetNamespace();

	@Internal
	public abstract Property<String> getTargetPackage();

	@Internal
	public abstract ListProperty<String> getObfuscationPatterns();

	@Internal
	public abstract Property<Integer> getNameLength();

	@Override
	public void run() throws Exception {
		if (getMinecraftVersions().get().size() != 1) {
			throw new RuntimeException("intermediary generation tasks can only be run for single versions at a time!");
		}

		super.run();
	}

	protected IntermediaryUtil.MergedArgsBuilder mergedArgs(MinecraftVersion minecraftVersion) {
		return withSharedOptions(minecraftVersion, IntermediaryUtil.mergedOptions());
	}

	protected IntermediaryUtil.SplitArgsBuilder splitArgs(MinecraftVersion minecraftVersion) {
		return withSharedOptions(minecraftVersion, IntermediaryUtil.splitOptions());
	}

	private <T extends IntermediaryUtil.ArgsBuilder> T withSharedOptions(MinecraftVersion minecraftVersion, T options) {
		if (getTargetNamespace().isPresent())
			options.targetNamespace(getTargetNamespace().get());
		if (getTargetPackage().isPresent())
			options.defaultPackage(getTargetPackage().get());
		if (getObfuscationPatterns().isPresent())
			options.obfuscationPatterns(getObfuscationPatterns().get());
		if (getNameLength().isPresent())
			options.nameLength(getNameLength().get());
		if (minecraftVersion.hasClient())
			options.clientHash(minecraftVersion.client().downloads().client().sha1());
		if (minecraftVersion.hasServer())
			if (minecraftVersion.hasServerJar())
				options.serverHash(minecraftVersion.server().downloads().server().sha1());
			else
				options.serverHash(minecraftVersion.server().downloads().server_zip().sha1());

		return options;
	}
}
