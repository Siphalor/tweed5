package de.siphalor.tweed5.defaultextensions.pather.api;

import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "create")
public final class ValuePathTracking implements PathTracking {
	private final PathTracking entryPathTracking = PathTracking.create();
	private final PathTracking valuePathTracking = PathTracking.create();

	@Override
	public void pushPathPart(String pathPart) {
		this.pushPathPart(pathPart, pathPart);
	}

	public void pushPathPart(String entryPathPart, String valuePathPart) {
		entryPathTracking.pushPathPart(entryPathPart);
		valuePathTracking.pushPathPart(valuePathPart);
	}

	@Override
	public void pushEmptyPathPart() {
		entryPathTracking.pushEmptyPathPart();
		valuePathTracking.pushEmptyPathPart();
	}

	public void pushEmptyValuePathPart(String entryPathPart) {
		entryPathTracking.pushPathPart(entryPathPart);
		valuePathTracking.pushEmptyPathPart();
	}

	@Override
	public void popPathPart() {
		valuePathTracking.popPathPart();
		entryPathTracking.popPathPart();
	}

	@Override
	public String currentPath() {
		return valuePathTracking.currentPath();
	}

	public String currentEntryPath() {
		return entryPathTracking.currentPath();
	}

	@Override
	public String[] currentPathParts() {
		return valuePathTracking.currentPathParts();
	}

	public String[] currentEntryPathParts() {
		return entryPathTracking.currentPathParts();
	}
}
