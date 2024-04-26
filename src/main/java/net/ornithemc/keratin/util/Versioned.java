package net.ornithemc.keratin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Versioned<T> {

	private final Map<String, T> values;
	private final ThrowingFunction<String, T> factory;
	private final BiFunction<String, Exception, RuntimeException> exceptions;

	public Versioned(ThrowingFunction<String, T> factory) {
		this(factory, (version, e) -> new RuntimeException("no such element for version " + version, e));
	}

	public Versioned(ThrowingFunction<String, T> factory, BiFunction<String, Exception, RuntimeException> exceptions) {
		this.values = new HashMap<>();
		this.factory = factory;
		this.exceptions = exceptions;
	}

	public T get(String version) {
		T value = values.get(version);

		if (value == null) {
			try {
				value = factory.apply(version);
				values.put(version, value);
			} catch (Exception e) {
				throw exceptions.apply(version, e);
			}
		}

		return value;
	}
}
