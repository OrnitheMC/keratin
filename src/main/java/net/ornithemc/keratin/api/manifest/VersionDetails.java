package net.ornithemc.keratin.api.manifest;

public record VersionDetails(String id, String normalizedVersion, String releaseTime, boolean client, boolean server, boolean sharedMappings, Downloads downloads) {

	public record Downloads(Download client, Download server, Download server_zip) {

		public record Download(String sha1, String url) {
		}
	}
}
