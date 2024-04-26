package net.ornithemc.keratin.manifest;

import java.util.Map;

public record VersionDetails(boolean client, boolean server, boolean sharedMappings, Map<String, Download> downloads) {

	public record Download(String sha1, String url) {
	}
}
