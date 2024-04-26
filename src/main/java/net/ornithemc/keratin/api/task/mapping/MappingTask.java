package net.ornithemc.keratin.api.task.mapping;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;

import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class MappingTask extends MinecraftTask implements Mapper {

	@Internal
	public abstract Property<String> getSourceNamespace();

	@Internal
	public abstract Property<String> getTargetNamespace();

}
