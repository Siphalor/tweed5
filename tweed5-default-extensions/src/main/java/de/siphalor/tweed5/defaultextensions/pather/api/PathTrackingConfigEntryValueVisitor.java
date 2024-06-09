package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PathTrackingConfigEntryValueVisitor implements ConfigEntryValueVisitor {
	private final ConfigEntryValueVisitor delegate;
	private final PathTracking pathTracking;

	@Override
	public <T> void visitEntry(ConfigEntry<T> entry, T value) {
		delegate.visitEntry(entry, value);
		entryVisited();
	}

	@Override
	public <T> boolean enterCollectionEntry(ConfigEntry<T> entry, T value) {
		boolean enter = delegate.enterCollectionEntry(entry, value);
		if (enter) {
			pathTracking.pushListContext();
		}
		return enter;
	}

	@Override
	public <T> void leaveCollectionEntry(ConfigEntry<T> entry, T value) {
		delegate.leaveCollectionEntry(entry, value);
		pathTracking.popContext();
		entryVisited();
	}

	@Override
	public <T> boolean enterCompoundEntry(ConfigEntry<T> entry, T value) {
		boolean enter = delegate.enterCompoundEntry(entry, value);
		if (enter) {
			pathTracking.pushMapContext();
		}
		return enter;
	}

	@Override
	public boolean enterCompoundSubEntry(String key) {
		boolean enter = delegate.enterCompoundSubEntry(key);
		if (enter) {
			pathTracking.pushPathPart(key);
		}
		return enter;
	}

	@Override
	public void leaveCompoundSubEntry(String key) {
		delegate.leaveCompoundSubEntry(key);
		pathTracking.popPathPart();
	}

	@Override
	public <T> void leaveCompoundEntry(ConfigEntry<T> entry, T value) {
		delegate.leaveCompoundEntry(entry, value);
		pathTracking.popContext();
		entryVisited();
	}

	private void entryVisited() {
		if (pathTracking.currentContext() == PathTracking.Context.LIST) {
			pathTracking.incrementListIndex();
		}
	}
}
