package net.ornithemc.keratin.api;

import java.util.Objects;

import net.ornithemc.keratin.Constants;
import net.ornithemc.keratin.KeratinGradleExtension;
import net.ornithemc.keratin.api.manifest.VersionDetails;

public record MinecraftVersion(String id, VersionDetails client, VersionDetails server) {

	public static MinecraftVersion parse(KeratinGradleExtension keratin, String s) {
		String[] parts = s.split("[~]");
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
			if (clientDetails.releaseTime().compareTo(Constants.RELEASE_TIME_A1_0_15) < 0 || serverDetails.releaseTime().compareTo(Constants.RELEASE_TIME_A1_0_15) < 0) {
				throw new RuntimeException("Cannot combine different client and server versions older than a1.0.15/server-a0.1.0!");
			}
			if (clientDetails.releaseTime().compareTo(Constants.RELEASE_TIME_B1_0) >= 0 || serverDetails.releaseTime().compareTo(Constants.RELEASE_TIME_B1_0) >= 0) {
				throw new RuntimeException("Cannot combine different client and server versions since b1.0!");
			}

			// TODO: check that specific alpha versions can actually be combined? e.g. do not allow a1.2.6+server-a0.1.0
		}

		clientDetails = clientDetails.client() ? clientDetails : null;
		serverDetails = serverDetails.server() ? serverDetails : null;

		return new MinecraftVersion(s, clientDetails, serverDetails);
	}

	public String key(GameSide side) {
		if (side == GameSide.CLIENT) {
			if (client == null) {
				throw new IllegalStateException("client-side key requested, but Minecraft version " + id + " does not have a client!");
			}

			return !hasSharedObfuscation() && hasSharedVersioning() ? client.id() + side.suffix() : client.id();
		}
		if (side == GameSide.SERVER) {
			if (server == null) {
				throw new IllegalStateException("server-side key requested, but Minecraft version " + id + " does not have a server!");
			}

			return !hasSharedObfuscation() && hasSharedVersioning() ? server.id() + side.suffix() : server.id();
		}

		return id;
	}

	public boolean hasClient() {
		return client != null;
	}

	public boolean hasServer() {
		return server != null;
	}

	public boolean hasSharedObfuscation() {
		// either side could be missing but for versions >=1.3 we still consider them to have shared obfuscation
		return (client != null && client.sharedMappings()) || (server != null && server.sharedMappings());
	}

	public boolean hasSharedVersioning() {
		// since beta the client and server jars use the same versioning
		return (client != null ? client : server).releaseTime().compareTo(Constants.RELEASE_TIME_B1_0) >= 0;
	}

	public boolean canBeMerged() {
		return canBeMergedLikeAlpha() || canBeMergedLikeBeta() || canBeMergedLikeRelease();
	}

	public boolean canBeMergedAsObfuscated() {
		return canBeMergedLikeRelease();
	}

	public boolean canBeMergedLikeAlpha() {
		return client != null && server != null && client.releaseTime().compareTo(Constants.RELEASE_TIME_A1_0_15) >= 0 && client.releaseTime().compareTo(Constants.RELEASE_TIME_B1_0) < 0;
	}

	public boolean canBeMergedLikeBeta() {
		return client != null && server != null && client.releaseTime().compareTo(Constants.RELEASE_TIME_B1_0) >= 0 && client.releaseTime().compareTo(Constants.RELEASE_TIME_1_3) < 0;
	}

	public boolean canBeMergedLikeRelease() {
		return client != null && server != null && client.releaseTime().compareTo(Constants.RELEASE_TIME_1_3) >= 0;
	}

	public boolean hasBrokenInnerClasses() {
		return !hasSharedVersioning() || "13w07a".equals(id);
	}

	public boolean usesSerializableForLevelSaving() {
		return client != null &&
			// only some classic versions uses Java Serializable for level saving
			((client.normalizedVersion().compareTo("0.14") >= 0 && client.normalizedVersion().compareTo("0.31") < 0)
				// c0.0.13a-launcher is the odd one out, dunno what's up with that
				|| client.id().equals("c0.0.13a-launcher"));
	}

	public boolean isBefore(MinecraftVersion other) {
		return (client != null ? client : server).releaseTime().compareTo((other.client != null ? other.client : other.server).releaseTime()) < 0;
	}

	public boolean isAfter(MinecraftVersion other) {
		return (client != null ? client : server).releaseTime().compareTo((other.client != null ? other.client : other.server).releaseTime()) > 0;
	}
}
