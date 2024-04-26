package net.ornithemc.keratin.api.task.merging;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class MergeTask extends MinecraftTask implements Merger {

	@Internal
	public abstract Property<String> getNamespace();

}
