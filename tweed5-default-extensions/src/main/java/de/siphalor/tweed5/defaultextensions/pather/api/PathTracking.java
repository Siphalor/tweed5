package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.defaultextensions.pather.impl.PathTrackingImpl;

public interface PathTracking {
	static PathTracking create() {
		return new PathTrackingImpl();
	}

	void pushPathPart(String pathPart);

	void popPathPart();

	String currentPath();
}
