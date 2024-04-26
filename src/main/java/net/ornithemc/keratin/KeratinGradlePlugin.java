package net.ornithemc.keratin;

import java.util.Objects;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.PluginAware;

import net.ornithemc.keratin.api.KeratinGradleExtensionAPI;

public class KeratinGradlePlugin implements Plugin<PluginAware> {

	public static final String KERATIN_VERSION = Objects.requireNonNullElse(KeratinGradlePlugin.class.getPackage().getImplementationVersion(), "0.0.0+unknown");

	@Override
	public void apply(PluginAware target) {
//		target.getPlugins().apply(PloceusRepositoryPlugin.class);

		if (target instanceof Project project) {
			project.getLogger().lifecycle("Keratin: " + KERATIN_VERSION);

			project.getExtensions().create(KeratinGradleExtensionAPI.class, "keratin", KeratinGradleExtension.class);
		}
	}
}
