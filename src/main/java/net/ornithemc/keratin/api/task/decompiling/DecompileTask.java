package net.ornithemc.keratin.api.task.decompiling;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class DecompileTask extends MinecraftTask {

	protected void submitDecompileTask(WorkQueue workQueue, String mainClass, FileCollection classpath, String[] args) {
		workQueue.submit(Decompile.class, parameters -> {
			parameters.getMainClass().set(mainClass);
			parameters.getClasspath().set(classpath.getFiles());
			parameters.getArgs().set(Arrays.asList(args));
		});
	}

	public interface DecompilerParameters extends WorkParameters {

		Property<String> getMainClass();

		SetProperty<File> getClasspath();

		ListProperty<String> getArgs();

	}

	public static abstract class Decompile implements WorkAction<DecompilerParameters> {

		@Inject
		public abstract ExecOperations getExecOperations();

		@Override
		public void execute() {
			String mainClass = getParameters().getMainClass().get();
			Set<File> classpath = getParameters().getClasspath().get();
			Object[] args = getParameters().getArgs().get().toArray();

			getExecOperations().javaexec(javaexec -> {
				javaexec.getMainClass().set(mainClass);
				javaexec.classpath(classpath);
				javaexec.args(args);
			});
		}
	}
}
