package net.ornithemc.keratin;

import net.ornithemc.keratin.api.GameSide;

public class Constants {

	public static final String ORNITHE_GLOBAL_CACHE_DIR = "ornithe-cache";
	public static final String ORNITHE_LOCAL_CACHE_DIR = "ornithe-cache";

	public static final String VERSIONS_MANIFEST_URL = "https://skyrising.github.io/mc-versions/version_manifest.json";

	public static final String MAVEN_NAME = "Ornithe";
	public static final String MAVEN_URL = "https://maven.ornithemc.net/releases";
	public static final String META_URL = "https://meta.ornithemc.net";
	public static String metaUrl(String endpoint) {
		return META_URL + endpoint;
	}

	public static final String FABRIC_MAVEN_NAME = "Fabric";
	public static final String FABRIC_MAVEN_URL = "https://maven.fabricmc.net";

	public static final String VINEFLOWER_SNAPSHOTS_MAVEN_NAME = "Vineflower Snapshots";
	public static final String VINEFLOWER_SNAPSHOTS_MAVEN_URL = "https://s01.oss.sonatype.org/content/repositories/snapshots/";

	public static String calamusGen1Url(String mc, GameSide side) {
		return MAVEN_URL + "/net/ornithemc/calamus-intermediary/" + mc + side.suffix() + "/calamus-intermediary-" + mc + side.suffix() + "-v2.jar";
	}
	public static String calamusGen2Url(String mc, int generation) {
		return MAVEN_URL + "/net/ornithemc/calamus-intermediary-gen" + generation + "/" + mc + "/calamus-intermediary-gen" + generation + "-" + mc + "-v2.jar";
	}

	public static String nestsUrl(String mc, GameSide side, int build) {
		return MAVEN_URL + "/net/ornithemc/nests/" + mc + side.suffix() + "+build." + build + "/nests-" + mc + side.suffix() + "+build." + build + ".jar";
	}

	public static String sparrowUrl(String mc, GameSide side, int build) {
		return MAVEN_URL + "/net/ornithemc/sparrow/" + mc + side.suffix() + "+build." + build + "/sparrow-" + mc + side.suffix() + "+build." + build + ".jar";
	}
}
