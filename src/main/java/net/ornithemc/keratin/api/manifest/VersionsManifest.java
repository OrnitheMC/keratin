package net.ornithemc.keratin.api.manifest;

import java.util.List;
import java.util.Optional;

public record VersionsManifest(List<Entry> versions) {

	public record Entry(String id, String url, String sha1, String details, String detailsSha1) {
	}

	public Optional<Entry> find(String version) {
		return versions.stream().filter(e -> e.id.equals(version)).findFirst();
	}

	public Entry findOrThrow(String version) {
		return find(version).orElseThrow();
	}
}
