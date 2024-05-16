package net.ornithemc.keratin.api.task;

import org.gradle.api.Action;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;

public abstract class MinecraftTask extends KeratinTask {

	private Action<String> configureMinecraftVersionAction;

	@Internal
	public abstract ListProperty<String> getMinecraftVersions();

	public void configureMinecraftVersion(Action<String> configureMinecraftVersionAction) {
		this.configureMinecraftVersionAction = configureMinecraftVersionAction;
	}

	@TaskAction
	public void run() throws Exception {
		for (String minecraftVersion : getMinecraftVersions().get()) {
			configureMinecraftVersionAction.execute(minecraftVersion);
			run(minecraftVersion);
		}
	}

	protected abstract void run(String minecraftVersion) throws Exception;

}
