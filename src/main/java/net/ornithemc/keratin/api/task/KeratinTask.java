package net.ornithemc.keratin.api.task;

import javax.inject.Inject;

import org.gradle.api.DefaultTask;

import net.ornithemc.keratin.KeratinGradleExtension;

public abstract class KeratinTask extends DefaultTask implements TaskAware {

	@Inject
	public KeratinTask() {
		setGroup("keratin");
	}

	@Override
	public KeratinGradleExtension getExtension() {
		return KeratinGradleExtension.get(getProject());
	}

	@Override
	public boolean isRefreshDependencies() {
		return getProject().getGradle().getStartParameter().isRefreshDependencies();
	}
}
