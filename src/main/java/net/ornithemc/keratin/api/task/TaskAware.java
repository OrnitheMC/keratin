package net.ornithemc.keratin.api.task;

import net.ornithemc.keratin.KeratinGradleExtension;

public interface TaskAware {

	KeratinGradleExtension getExtension();

	boolean isRefreshDependencies();

}
