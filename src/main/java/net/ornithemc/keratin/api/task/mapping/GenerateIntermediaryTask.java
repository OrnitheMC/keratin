package net.ornithemc.keratin.api.task.mapping;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import net.ornithemc.keratin.api.manifest.VersionDetails;
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

	protected OptionsBuilder getOptions(VersionDetails details) {
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

		return options;
	}
}
