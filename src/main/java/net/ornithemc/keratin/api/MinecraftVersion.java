package net.ornithemc.keratin.api;

import java.util.Objects;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public record MinecraftVersion(String id, VersionDetails client, VersionDetails server) implements Comparable<MinecraftVersion> {

	public static MinecraftVersion parse(KeratinGradleExtension keratin, String s) {
		String[] parts = s.split("[&]");
		String minecraftClientVersion = parts.length == 2 ? parts[0] : s;
		String minecraftServerVersion = parts.length == 2 ? parts[1] : s;

		VersionDetails clientDetails = keratin.getVersionDetails(minecraftClientVersion);
		VersionDetails serverDetails = keratin.getVersionDetails(minecraftServerVersion);

		if (!clientDetails.client() && !serverDetails.server()) {
			throw new RuntimeException("Minecraft version " + s + " is neither client nor server! Weird!");
		}
		if (clientDetails.client() && serverDetails.server() && !Objects.equals(clientDetails.id(), serverDetails.id())) {
			// client id != server id, thus different client and server versions
			// this is only allowed for certain alpha versions
			if (clientDetails.compareTo(Constants.SEMVER_a1_0_15) < 0 || serverDetails.compareTo(Constants.SEMVER_a0_1_0) < 0) {
				throw new RuntimeException("Cannot combine different client and server versions older than a1.0.15/server-a0.1.0!");
			}
			if (clientDetails.compareTo(Constants.SEMVER_b1_0) >= 0 || serverDetails.compareTo(Constants.SEMVER_b1_0) >= 0) {
				throw new RuntimeException("Cannot combine different client and server versions since b1.0!");
			}

			// TODO: check that specific alpha versions can actually be combined? e.g. do not allow a1.2.6+server-a0.1.0
		}

		clientDetails = clientDetails.client() ? clientDetails : null;
		serverDetails = serverDetails.server() ? serverDetails : null;

		MinecraftVersion minecraftVersion = new MinecraftVersion(s, clientDetails, serverDetails);

		if (minecraftVersion.hasSharedObfuscation() && !minecraftVersion.hasSharedVersioning()) {
			throw new RuntimeException("Minecraft version " + minecraftVersion.id + " has shared obfuscation but not shared versioning - how?");
		}

		return minecraftVersion;
	}

	public String clientKey() {
		if (client == null) {
			throw new IllegalStateException("client-side key requested, but Minecraft version " + id + " does not have a client!");
		}

		return client.id() + "-client";
	}

	public String serverKey() {
		if (server == null) {
			throw new IllegalStateException("server-side key requested, but Minecraft version " + id + " does not have a server!");
		}

		return server.id() + "-server";
	}

	public boolean hasClient() {
		return client != null;
	}

	public boolean hasServer() {
		return server != null;
	}

	public boolean hasServerJar() {
		return server != null && server.downloads().server() != null;
	}

	public boolean hasServerZip() {
		return server != null && server.downloads().server_zip() != null;
	}

	public boolean hasSharedObfuscation() {
		// either side could be missing but for versions >=1.3 we still
		// consider them to have shared obfuscation if the value is true
		return (client != null ? client : server).sharedMappings();
	}

	public boolean hasSharedVersioning() {
		// since beta the client and server jars use the same versioning
		// alpha: 1.x.x vs 0.x.x - classic 0.x vs 1.x
		return (client != null ? client : server).compareTo(Constants.SEMVER_b1_0) >= 0;
	}

	public boolean canBeMerged() {
		return canBeMergedLikeAlpha() || canBeMergedLikeBeta() || canBeMergedLikeRelease();
	}

	public boolean canBeMergedAsObfuscated() {
		return canBeMergedLikeRelease();
	}

	public boolean canBeMergedAsMapped() {
		return canBeMergedLikeAlpha() || canBeMergedLikeBeta();
	}

	public boolean canBeMergedLikeAlpha() {
		return client != null && server != null && !hasSharedObfuscation() && !hasSharedVersioning();
	}

	public boolean canBeMergedLikeBeta() {
		return client != null && server != null && !hasSharedObfuscation() && hasSharedVersioning();
	}

	public boolean canBeMergedLikeRelease() {
		return client != null && server != null && hasSharedObfuscation();
	}

	public boolean hasBrokenInnerClasses() {
		// some versions are obfuscated in such a way that class names will
		// reveal some inner classes but either not all of them, or the actual
		// inner class attributes are missing from the class files
		return !hasSharedVersioning() || id.equals(Constants.VERSION_13w07a);
	}

	public boolean usesSerializableForLevelSaving() {
		// only some classic versions uses Java Serializable for level saving
		// we just hard-code these checks because the alternative is to download
		// the all the jars and check them manually, and just... no
		return client != null &&
			((client.compareTo(Constants.SEMVER_c0_0_14a) >= 0 && client.compareTo(Constants.SEMVER_INDEV) < 0)
				// c0.0.13a-launcher is the odd one out, dunno what's up with that
				|| client.id().equals(Constants.VERSION_c0_0_13a_launcher));
	}

	public boolean hasCommonSide(MinecraftVersion o) {
		return (hasClient() && o.hasClient()) || (hasServer() && o.hasServer());
	}

	@Override
	public int compareTo(MinecraftVersion o) {
		MinecraftVersion v = this;
		MinecraftVersion ov = o;

		boolean sharedVersioning = hasSharedVersioning();
		boolean oSharedVersioning = o.hasSharedVersioning();

		boolean bothSides = hasClient() && hasServer();
		boolean oBothSides = o.hasClient() && o.hasServer();

		if (!sharedVersioning && !oSharedVersioning) {
			if (bothSides && oBothSides) {
				throw new UnsupportedOperationException("cannot compare two combined pre-Beta versions (" + id + " and " + o.id + ")");
			}
			if (!hasCommonSide(o)) {
				throw new UnsupportedOperationException("cannot compare two pre-Beta versions without a common side (" + id + " and " + o.id + ")");
			}
		}

		if (!sharedVersioning && bothSides) {
			v = this;
			ov = o;
		}
		if (!oSharedVersioning && oBothSides) {
			v = o;
			ov = this;
		}

		return compare(v, ov);
	}

	private static int compare(MinecraftVersion v, MinecraftVersion ov) {
		int cc = 0;
		int sc = 0;

		if (v.hasClient() && (ov.hasClient() || ov.hasSharedVersioning())) {
			cc = compare(v.client(), ov.hasClient() ? ov.client() : ov.server());
		}
		if (v.hasServer() && (ov.hasServer() || ov.hasSharedVersioning())) {
			sc = compare(v.server(), ov.hasServer() ? ov.server() : ov.client());
		}

		if ((cc < 0 && sc > 0) || (cc > 0 && sc < 0)) {
			throw new UnsupportedOperationException("ambiguous version comparison: " + v.client().id() + ".compareTo(" + ov.client().id() + ") = " + cc + " / " + v.server().id() + ".compareTo(" + ov.server().id() + ") = " + sc);
		}

		return cc == 0 ? sc : cc;
	}

	private static int compare(VersionDetails v, VersionDetails ov) {
		return v.compareTo(ov);
	}
}
