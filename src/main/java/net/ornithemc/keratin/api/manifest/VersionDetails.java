package net.ornithemc.keratin.api.manifest;

import java.util.Map;

public record VersionDetails(String releaseTime, boolean client, boolean server, boolean sharedMappings, Map<String, Download> downloads) {

	public record Download(String sha1, String url) {
	}
}
