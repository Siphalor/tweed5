package de.siphalor.tweed5.serde.extension.api.path;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.List;

@RequiredArgsConstructor
public class EntryPath {
	@Getter
	private final List<@Nullable String> parts;
	private final String string;

	@Override
	public String toString() {
		return string;
	}
}
