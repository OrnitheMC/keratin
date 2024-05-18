package net.ornithemc.keratin.api.task.enigma;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.Configurations;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.OrnitheFilesAPI;
import net.ornithemc.keratin.api.task.MinecraftTask;

public abstract class LaunchEnigmaTask extends MinecraftTask {

	@Override
	public void run(WorkQueue workQueue, String minecraftVersion) {
		KeratinGradleExtension keratin = getExtension();
		Project project = keratin.getProject();
		OrnitheFilesAPI files = keratin.getFiles();

		workQueue.submit(LaunchEnigma.class, parameters -> {
			parameters.getJar().set(files.getMainProcessedIntermediaryJar(minecraftVersion));
			parameters.getMappings().set(files.getRunDirectory(minecraftVersion));
			parameters.getClasspath().set(project.getConfigurations().getByName(Configurations.ENIGMA_RUNTIME).getFiles());
		});
	}

	public interface EnigmaParameters extends WorkParameters {

		Property<File> getJar();

		Property<File> getMappings();

		SetProperty<File> getClasspath();

	}

	public static abstract class LaunchEnigma implements WorkAction<EnigmaParameters> {

		@Inject
		public abstract ExecOperations getExecOperations();

		@Override
		public void execute() {
			File jar = getParameters().getJar().get();
			File dir = getParameters().getMappings().get();
			Set<File> classpath = getParameters().getClasspath().get();

			getExecOperations().javaexec(javaexec -> {
				javaexec.getMainClass().set("cuchaz.enigma.gui.Main");
				javaexec.classpath(classpath);
				javaexec.args(
					"-jar"     , jar.getAbsolutePath(),
					"-mappings", dir.getAbsolutePath(),
					"-profile" , "enigma_profile.json"
				);
			});
		}
	}
}
