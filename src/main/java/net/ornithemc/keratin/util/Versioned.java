package net.ornithemc.keratin.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class Versioned<V, T> {

	private final Map<V, T> values;
	private final ThrowingFunction<V, T> factory;
	private final BiFunction<V, Exception, RuntimeException> exceptions;

	public Versioned(ThrowingFunction<V, T> factory) {
		this(factory, (version, e) -> new RuntimeException("no such element for version " + version, e));
	}

	public Versioned(ThrowingFunction<V, T> factory, BiFunction<V, Exception, RuntimeException> exceptions) {
		this.values = new HashMap<>();
		this.factory = factory;
		this.exceptions = exceptions;
	}

	public T get(V version) {
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
