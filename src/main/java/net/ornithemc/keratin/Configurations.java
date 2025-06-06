package net.ornithemc.keratin;

public class Configurations {

	public static final String IMPLEMENTATION      = "implementation";
	public static final String minecraftLibraries(String minecraftVersion) {
		return "%s_minecraftLibraries".formatted(minecraftVersion);
	}
	public static final String DECOMPILE_CLASSPATH = "decompileClasspath";
	public static final String ENIGMA_RUNTIME      = "enigmaRuntime";
	public static final String javadocClasspath(String minecraftVersion) {
		return "%s_javadocClasspath".formatted(minecraftVersion);
	}

}
