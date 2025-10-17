package net.ornithemc.keratin.api.manifest;

import com.vdurmont.semver4j.Semver;

public record VersionDetails(String id, String normalizedVersion, boolean client, boolean server, boolean sharedMappings, Downloads downloads) implements Comparable<VersionDetails> {

	public record Downloads(Download client, Download server, Download server_zip) {

		public record Download(String sha1, String url) {
		}
	}

	@Override
	public int compareTo(VersionDetails o) {
		return new Semver(normalizedVersion).compareTo(new Semver(o.normalizedVersion));
	}

	public int compareTo(String o) {
		return new Semver(normalizedVersion).compareTo(new Semver(o));
	}
}
