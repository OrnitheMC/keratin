package net.ornithemc.keratin.api.task;

import javax.inject.Inject;

import org.gradle.api.Action;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

public abstract class MinecraftTask extends KeratinTask {

	private Action<String> configureMinecraftVersionAction;

	@Inject
	public abstract WorkerExecutor getWorkerExecutor();

	@Internal
	public abstract ListProperty<String> getMinecraftVersions();

	public void configureMinecraftVersion(Action<String> configureMinecraftVersionAction) {
		this.configureMinecraftVersionAction = configureMinecraftVersionAction;
	}

	@TaskAction
	public void run() throws Exception {
		WorkerExecutor workerExecutor = getWorkerExecutor();
		WorkQueue workQueue = workerExecutor.noIsolation();

		for (String minecraftVersion : getMinecraftVersions().get()) {
			if (configureMinecraftVersionAction != null) {
				configureMinecraftVersionAction.execute(minecraftVersion);
			}

			run(workQueue, minecraftVersion);
		}
	}

	protected abstract void run(WorkQueue workQueue, String minecraftVersion) throws Exception;

}
