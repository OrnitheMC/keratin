package net.ornithemc.keratin.api.settings;

public record BuildNumbers(int client, int server, int merged) {

	public static BuildNumbers none() {
		return new BuildNumbers(-1, -1, -1);
	}

	public BuildNumbers withBuilds(int client, int server) {
		return new BuildNumbers(client, server, merged);
	}

	public BuildNumbers withBuild(int build) {
		return new BuildNumbers(client, server, build);
	}

	public boolean isNone() {
		return client == -1 && server == -1 && merged == -1;
	}
}
