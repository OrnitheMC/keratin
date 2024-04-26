package net.ornithemc.keratin.api.task;

import org.gradle.api.tasks.Internal;

import net.ornithemc.keratin.KeratinGradleExtension;

public interface TaskAware {

	@Internal
	KeratinGradleExtension getExtension();

	@Internal
	boolean isRefreshDependencies();

}
