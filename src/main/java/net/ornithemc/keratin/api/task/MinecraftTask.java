package net.ornithemc.keratin.api.task;

import javax.inject.Inject;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

public abstract class MinecraftTask extends KeratinTask {

	@Inject
	public MinecraftTask() {
		getMinecraftVersion().convention(getExtension().getMinecraftVersion());
		getMinecraftVersion().finalizeValueOnRead();
	}

	@Internal
	public abstract Property<String> getMinecraftVersion();

}
