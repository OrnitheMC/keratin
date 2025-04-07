package net.ornithemc.keratin.api.task.enigma;

import java.io.File;
import java.util.Arrays;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.MinecraftVersion;
import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;
import net.ornithemc.keratin.files.GlobalCache.ProcessedJarsCache;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles;
import net.ornithemc.keratin.files.MappingsDevelopmentFiles.BuildFiles;
import net.ornithemc.keratin.files.OrnitheFiles;

public abstract class LaunchEnigmaTask extends MinecraftTask implements JavaExecution, EnigmaSession {

	@Internal
	public abstract Property<Boolean> getUnpicked();

	@Override
	public void run() throws Exception {
		KeratinGradleExtension keratin = getExtension();
		OrnitheFiles files = keratin.getFiles();

		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();

		for (MinecraftVersion minecraftVersion : getMinecraftVersions().get()) {
			checkSessionLock(minecraftVersion.id(), mappings.getEnigmaSessionLock(minecraftVersion));
		}

		super.run();
	}

	@Override
	public void run(WorkQueue workQueue, MinecraftVersion minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFiles files = keratin.getFiles();

		ProcessedJarsCache processedJars = files.getGlobalCache().getProcessedJarsCache();
		MappingsDevelopmentFiles mappings = files.getMappingsDevelopmentFiles();
		BuildFiles buildFiles = mappings.getBuildFiles();

		File processedJar = getUnpicked().get()
			? buildFiles.getUnpickedProcessedIntermediaryJar(minecraftVersion)
			: processedJars.getMainProcessedIntermediaryJar(minecraftVersion);

		workQueue.submit(EnigmaSessionAction.class, parameters -> {
			parameters.getMinecraftVersion().set(minecraftVersion.id());
			parameters.getSessionLock().set(mappings.getEnigmaSessionLock(minecraftVersion));
			parameters.getMainClass().set("org.quiltmc.enigma.gui.Main");
			parameters.getClasspath().set(project.getConfigurations().getByName(Configurations.ENIGMA_RUNTIME).getFiles());
			parameters.getArgs().set(Arrays.asList(
				"-jar"     , processedJar.getAbsolutePath(),
				"-mappings", mappings.getWorkingDirectory(minecraftVersion).getAbsolutePath(),
				"-profile" , mappings.getEnigmaProfileJson().getAbsolutePath()
			));
		});
	}
}
