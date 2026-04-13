package de.siphalor.tweed5.defaultextensions.pather.impl;

import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class PathTrackingImpl implements PathTracking {
	private final StringBuilder pathBuilder = new StringBuilder(256);
	private final Deque<@Nullable String> pathParts = new ArrayDeque<>(50);

	@Override
	public void pushPathPart(String entryPathPart) {
		pathParts.push(entryPathPart);
		pathBuilder.append(".").append(entryPathPart);
	}

	@Override
	public void pushEmptyPathPart() {
		pathParts.push(null);
	}

	@Override
	public void popPathPart() {
		if (!pathParts.isEmpty()) {
			String poppedPart = pathParts.pop();
			if (poppedPart != null) {
				pathBuilder.setLength(pathBuilder.length() - poppedPart.length() - 1);
			}
		}
	}

	@Override
	public String currentPath() {
		return pathBuilder.toString();
	}

	@Override
	public String[] currentPathParts() {
		return pathParts.toArray(new String[0]);
	}
}
