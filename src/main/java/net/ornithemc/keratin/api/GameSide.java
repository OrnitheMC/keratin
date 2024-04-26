package net.ornithemc.keratin.api;

public enum GameSide {

	CLIENT("-client"),
	SERVER("-server"),
	MERGED("");

	private final String suffix;

	private GameSide(String suffix) {
		this.suffix = suffix;
	}

	public String suffix() {
		return suffix;
	}
}
