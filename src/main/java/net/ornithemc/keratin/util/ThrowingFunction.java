package net.ornithemc.keratin.util;

public interface ThrowingFunction<T, R> {

	R apply(T t) throws Exception;

}
