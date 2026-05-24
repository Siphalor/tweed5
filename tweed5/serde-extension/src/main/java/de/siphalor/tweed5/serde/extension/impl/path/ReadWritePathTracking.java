package de.siphalor.tweed5.serde.extension.impl.path;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReadWritePathTracking {
	private final StringBuilder pathBuilder = new StringBuilder(256);
	private final List<@Nullable String> pathParts = new ArrayList<>(50);

	public void push(@Nullable String part) {
		pathParts.add(part);
		if (part != null) {
			pathBuilder.append(".").append(part);
		}
	}

	public void pop() {
		if (!pathParts.isEmpty()) {
			String poppedPart = pathParts.remove(pathParts.size() - 1);
			if (poppedPart != null) {
				pathBuilder.setLength(pathBuilder.length() - poppedPart.length() - 1);
			}
		}
	}

	public String currentPath() {
		return pathBuilder.toString();
	}

	public List<@Nullable String> currentPathParts() {
		return Collections.unmodifiableList(new ArrayList<>(pathParts));
	}
}
