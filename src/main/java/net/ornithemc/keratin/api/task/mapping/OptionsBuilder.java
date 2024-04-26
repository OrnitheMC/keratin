package net.ornithemc.keratin.api.task.mapping;

import java.util.ArrayList;
import java.util.List;

public class OptionsBuilder {

	private String targetNamespace;
	private String targetPackage;
	private List<String> obfuscationPatterns = new ArrayList<>();
	private int nameLength = -1;
	private String clientHash;
	private String serverHash;

	public OptionsBuilder targetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;
		return this;
	}

	public OptionsBuilder targetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
		return this;
	}

	public OptionsBuilder obfuscationPattern(String obfuscationPattern) {
		this.obfuscationPatterns.add(obfuscationPattern);
		return this;
	}

	public OptionsBuilder nameLength(int nameLength) {
		this.nameLength = nameLength;
		return this;
	}

	public OptionsBuilder clientHash(String clientHash) {
		this.clientHash = clientHash;
		return this;
	}

	public OptionsBuilder serverHash(String serverHash) {
		this.serverHash = serverHash;
		return this;
	}

	public String[] build() {
		List<String> options = new ArrayList<>();

		if (targetNamespace != null) {
			options.add("--target-namespace");
			options.add(targetNamespace);
		}
		if (targetPackage != null) {
			options.add("--default-package");
			options.add(targetPackage);
		}
		for (String obfuscationPattern : obfuscationPatterns) {
			options.add("--obfuscation-pattern");
			options.add(obfuscationPattern);
		}
		if (nameLength > 0) {
			options.add("--name-length");
			options.add(Integer.toString(nameLength));
		}
		if (clientHash != null) {
			options.add("--client-hash");
			options.add(clientHash);
		}
		if (serverHash != null) {
			options.add("--server-hash");
			options.add(serverHash);
		}

		return options.toArray(String[]::new);
	}
}
