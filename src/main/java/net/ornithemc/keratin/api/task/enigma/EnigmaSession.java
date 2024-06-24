package net.ornithemc.keratin.api.task.enigma;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.SetProperty;
import org.gradle.process.ExecOperations;
import org.gradle.workers.WorkAction;

import net.ornithemc.keratin.api.task.JavaExecution.JavaExecutionParameters;

public interface EnigmaSession {

	static String LOCK_FILE = "enigma_session.lock";

	default void checkSessionLock(String minecraftVersion, File lockFile) throws EnigmaSessionLockException {
		if (lockFile.exists()) {
			throw EnigmaSessionLockException.check(minecraftVersion);
		}
	}

	default void acquireSessionLock(String minecraftVersion, File lockFile) throws EnigmaSessionLockException {
		try {
			lockFile.createNewFile();
		} catch (IOException e) {
			throw EnigmaSessionLockException.acquire(minecraftVersion, e);
		}
	}

	default void releaseSessionLock(String minecraftVersion, File lockFile) throws EnigmaSessionLockException {
		try {
			lockFile.delete();
		} catch (Exception e) {
			throw EnigmaSessionLockException.release(minecraftVersion, e);
		}
	}

	interface EnigmaSessionParameters extends JavaExecutionParameters {

		Property<String> getMinecraftVersion();

		Property<File> getSessionLock();

		Property<String> getMainClass();

		SetProperty<File> getClasspath();

		ListProperty<String> getArgs();

	}

	abstract class EnigmaSessionAction implements WorkAction<EnigmaSessionParameters>, EnigmaSession {

		@Inject
		public abstract ExecOperations getExecOperations();

		@Override
		public void execute() {
			String minecraftVersion = getParameters().getMinecraftVersion().get();
			File lockFile = getParameters().getSessionLock().get();

			String mainClass = getParameters().getMainClass().get();
			Set<File> classpath = getParameters().getClasspath().get();
			Object[] args = getParameters().getArgs().get().toArray();

			try {
				checkSessionLock(minecraftVersion, lockFile);
				acquireSessionLock(minecraftVersion, lockFile);

				try {
					getExecOperations().javaexec(javaexec -> {
						javaexec.getMainClass().set(mainClass);
						javaexec.classpath(classpath);
						javaexec.args(args);
					});
				} catch (Exception e) {
					throw new RuntimeException("exception while launching Enigma for " + minecraftVersion, e);
				} finally {
					releaseSessionLock(minecraftVersion, lockFile);
				}
			} catch (EnigmaSessionLockException e) {
				throw new RuntimeException("exception with Enigma session lock for " + minecraftVersion);
			}
		}
	}
}
