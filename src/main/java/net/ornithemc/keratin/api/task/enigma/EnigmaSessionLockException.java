package net.ornithemc.keratin.api.task.enigma;

public class EnigmaSessionLockException extends Exception {

	private static final long serialVersionUID = 1L;

	public EnigmaSessionLockException(String message) {
		super(message);
	}

	public EnigmaSessionLockException(String message, Throwable cause) {
		super(message, cause);
	}

	public static EnigmaSessionLockException check(String minecraftVersion) {
		return new EnigmaSessionLockException("Enigma session lock found: another Enigma session for " + minecraftVersion + " is already running!");
	}

	public static EnigmaSessionLockException acquire(String minecraftVersion, Throwable cause) {
		return new EnigmaSessionLockException("unable to acquire Enigma session lock for " + minecraftVersion, cause);
	}

	public static EnigmaSessionLockException release(String minecraftVersion, Throwable cause) {
		return new EnigmaSessionLockException("unable to release Enigma session lock for " + minecraftVersion, cause);
	}
}
