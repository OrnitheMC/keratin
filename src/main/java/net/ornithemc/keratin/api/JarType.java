package net.ornithemc.keratin.api;

public enum JarType {

	CLIENT, SERVER, MERGED;

	public String getName() {
		return this.name().toLowerCase();
	}
}
