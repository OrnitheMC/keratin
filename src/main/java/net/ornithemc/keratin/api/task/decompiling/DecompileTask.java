package net.ornithemc.keratin.api.task.decompiling;

import java.util.Arrays;

import org.gradle.api.file.FileCollection;
import org.gradle.workers.WorkQueue;

import net.ornithemc.keratin.api.task.JavaExecution;
import net.ornithemc.keratin.api.task.MinecraftTask;

abstract class DecompileTask extends MinecraftTask implements JavaExecution {

	protected void submitJavaExecDecompileTask(WorkQueue workQueue, String mainClass, FileCollection classpath, String[] args) {
		workQueue.submit(JavaExecutionAction.class, parameters -> {
			parameters.getMainClass().set(mainClass);
			parameters.getClasspath().set(classpath.getFiles());
			parameters.getArgs().set(Arrays.asList(args));
		});
	}
}
