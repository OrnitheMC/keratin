package net.ornithemc.keratin.api.task.build;

import javax.inject.Inject;

import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class BuildTask extends MinecraftTask {

	@Inject
	public BuildTask() {
		getProject().getTasks().getByName("build").dependsOn(this);
	}
}
