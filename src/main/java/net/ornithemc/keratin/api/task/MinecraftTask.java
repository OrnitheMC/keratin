package net.ornithemc.keratin.api.task;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import net.ornithemc.keratin.api.MinecraftVersion;

public abstract class MinecraftTask extends KeratinTask {

	private Action<MinecraftVersion> configureMinecraftVersionAction;

	@Inject
	public abstract WorkerExecutor getWorkerExecutor();

	@Internal
	public abstract ListProperty<MinecraftVersion> getMinecraftVersions();

	public void configureMinecraftVersion(Action<MinecraftVersion> configureMinecraftVersionAction) {
		this.configureMinecraftVersionAction = configureMinecraftVersionAction;
	}

	@TaskAction
	public void run() throws Exception {
		WorkerExecutor workerExecutor = getWorkerExecutor();
		WorkQueue workQueue = workerExecutor.noIsolation();

		for (MinecraftVersion minecraftVersion : getMinecraftVersions().get()) {
			if (configureMinecraftVersionAction != null) {
				configureMinecraftVersionAction.execute(minecraftVersion);
			}

			run(workQueue, minecraftVersion);
		}
	}

	protected abstract void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) throws Exception;

}
