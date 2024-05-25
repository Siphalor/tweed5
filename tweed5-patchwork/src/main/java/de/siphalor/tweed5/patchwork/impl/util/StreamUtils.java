package de.siphalor.tweed5.patchwork.impl.util;

import java.util.stream.Stream;

public class StreamUtils {
	@SafeVarargs
	public static <T> Stream<T> concat(Stream<T>... streams) {
		Stream<T> result = Stream.empty();
		for (Stream<T> stream : streams) {
			result = Stream.concat(result, stream);
		}
		return result;
	}
}
