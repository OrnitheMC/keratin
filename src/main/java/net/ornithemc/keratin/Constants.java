package net.ornithemc.keratin;

import net.ornithemc.keratin.api.GameSide;

public class Constants {

	public static final String ORNITHE_GLOBAL_CACHE_DIR = "ornithe-cache";
	public static final String ORNITHE_LOCAL_CACHE_DIR = "ornithe-cache";

	public static final String VERSIONS_MANIFEST_URL = "https://ornithemc.net/mc-versions/version_manifest.json";

	public static final String MAVEN_NAME = "Ornithe";
	public static final String MAVEN_URL = "https://maven.ornithemc.net/releases";
	public static final String META_URL = "https://meta.ornithemc.net";
	public static String metaUrl(String endpoint) {
		return META_URL + endpoint;
	}

	public static final String FABRIC_MAVEN_NAME = "Fabric";
	public static final String FABRIC_MAVEN_URL = "https://maven.fabricmc.net";

	public static final String QUILT_MAVEN_NAME = "Quilt";
	public static final String QUILT_MAVEN_URL = "https://maven.quiltmc.org/repository/release";

	public static final String VINEFLOWER_SNAPSHOTS_MAVEN_NAME = "Vineflower Snapshots";
	public static final String VINEFLOWER_SNAPSHOTS_MAVEN_URL = "https://s01.oss.sonatype.org/content/repositories/snapshots";

	public static final String MINECRAFT_LIBRARIES_MAVEN_NAME = "Minecraft Libraries";
	public static final String MINECRAFT_LIBRARIES_MAVEN_URL = "https://libraries.minecraft.net";

	public static String calamusGen2Url(String mc, int generation) {
		return MAVEN_URL + "/net/ornithemc/calamus-intermediary-gen" + generation + "/" + mc + "/calamus-intermediary-gen" + generation + "-" + mc + "-v2.jar";
	}

	public static String featherGen2Url(String mc, int generation, int build) {
		return MAVEN_URL + "/net/ornithemc/feather-gen" + generation + "/" + mc + "+build." + build + "/feather-gen" + generation + "-" + mc + "+build." + build + "-v2.jar";
	}

	public static String ravenUrl(String mc, GameSide side, int build) {
		return MAVEN_URL + "/net/ornithemc/raven/" + mc + side.suffix() + "+build." + build + "/raven-" + mc + side.suffix() + "+build." + build + ".jar";
	}

	public static String sparrowUrl(String mc, GameSide side, int build) {
		return MAVEN_URL + "/net/ornithemc/sparrow/" + mc + side.suffix() + "+build." + build + "/sparrow-" + mc + side.suffix() + "+build." + build + ".jar";
	}

	public static String nestsUrl(String mc, GameSide side, int build) {
		return MAVEN_URL + "/net/ornithemc/nests/" + mc + side.suffix() + "+build." + build + "/nests-" + mc + side.suffix() + "+build." + build + ".jar";
	}

	public static final String featherGen2Endpoint(int generation) {
		return "/v3/versions/gen" + generation + "/feather";
	}
	public static final String RAVEN_ENDPOINT = "/v3/versions/raven";
	public static final String SPARROW_ENDPOINT = "/v3/versions/sparrow";
	public static final String NESTS_ENDPOINT = "/v3/versions/nests";

	public static final String RELEASE_TIME_A1_0_15 = "2010-08-04T00:00:00+00:00";
	public static final String RELEASE_TIME_B1_0    = "2010-12-20T17:28:00+00:00";
	public static final String RELEASE_TIME_1_3     = "2012-07-26T12:49:00+00:00";
}
