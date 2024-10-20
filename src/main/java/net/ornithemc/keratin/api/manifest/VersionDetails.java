package net.ornithemc.keratin.api.manifest;

import java.util.Map;

import net.ornithemc.keratin.Constants;

public record VersionDetails(String id, String releaseTime, boolean client, boolean server, boolean sharedMappings, Map<String, Download> downloads) {

	public record Download(String sha1, String url) {
	}

	public boolean isPreBeta() {
		return releaseTime.compareTo(Constants.RELEASE_TIME_B1_0) < 0;
	}
}
