package net.ornithemc.keratin.api.manifest;

import java.util.List;

public record VersionInfo(List<Library> libraries) {

	public record Library(Downloads downloads) {

		public record Downloads(Artifact artifact) {

			public record Artifact(String url, String sha1) {
			}
		}
	}
}
