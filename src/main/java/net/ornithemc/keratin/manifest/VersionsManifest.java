package net.ornithemc.keratin.manifest;

import java.util.List;
import java.util.Optional;

public record VersionsManifest(List<Entry> versions) {

	public record Entry(String id, String url, String details) {
	}

	public Optional<Entry> find(String version) {
		return versions.stream().filter(e -> e.id.equals(version)).findFirst();
	}

	public Entry findOrThrow(String version) {
		return find(version).orElseThrow();
	}
}
