package net.ornithemc.keratin.api.task;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

public interface JavaExecution {

	interface JavaExecutionParameters extends WorkParameters {

		Property<String> getMainClass();

		SetProperty<File> getClasspath();

		ListProperty<String> getArgs();

	}

	abstract class JavaExecutionAction implements WorkAction<JavaExecutionParameters> {

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
