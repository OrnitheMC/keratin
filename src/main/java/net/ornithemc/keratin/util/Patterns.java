package net.ornithemc.keratin.util;

import java.util.regex.Pattern;

public class Patterns {

	public static final Pattern OPTIONAL_MC_VERSION_RANGE = Pattern.compile("(?:(.+)\\s)?\\.\\.(?:\\s?(.+))?");

}
