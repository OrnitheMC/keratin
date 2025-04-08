package net.ornithemc.keratin.api.settings;

public record ProcessorSettings(int processorVersion, // include in case processor deps change
                                boolean obfuscateLocalVariableNames,
                                BuildNumbers exceptionsBuilds,
                                BuildNumbers signaturesBuilds,
                                BuildNumbers nestsBuilds) {

	public static final int PROCESSOR_VERSION = 1;

	public static ProcessorSettings init(int processorVersion) {
		return new ProcessorSettings(processorVersion, false, BuildNumbers.none(), BuildNumbers.none(), BuildNumbers.none());
	}

	public ProcessorSettings withObfuscateLocalVariableNames(boolean obfuscateLocalVariableNames) {
		return new ProcessorSettings(processorVersion, obfuscateLocalVariableNames, exceptionsBuilds, signaturesBuilds, nestsBuilds);
	}

	public ProcessorSettings withExceptionsBuilds(BuildNumbers exceptionsBuilds) {
		return new ProcessorSettings(processorVersion, obfuscateLocalVariableNames, exceptionsBuilds, signaturesBuilds, nestsBuilds);
	}

	public ProcessorSettings withSignaturesBuilds(BuildNumbers signaturesBuilds) {
		return new ProcessorSettings(processorVersion, obfuscateLocalVariableNames, exceptionsBuilds, signaturesBuilds, nestsBuilds);
	}

	public ProcessorSettings withNestsBuilds(BuildNumbers nestsBuilds) {
		return new ProcessorSettings(processorVersion, obfuscateLocalVariableNames, exceptionsBuilds, signaturesBuilds, nestsBuilds);
	}
}
