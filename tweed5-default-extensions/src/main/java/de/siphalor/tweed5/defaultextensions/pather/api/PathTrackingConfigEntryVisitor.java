package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PathTrackingConfigEntryVisitor implements ConfigEntryVisitor {
	private final ConfigEntryVisitor delegate;
	private final PathTracking pathTracking;

	@Override
	public void visitEntry(ConfigEntry<?> entry) {
		delegate.visitEntry(entry);
		entryVisited();
	}

	@Override
	public boolean enterCollectionEntry(ConfigEntry<?> entry) {
		boolean enter = delegate.enterCollectionEntry(entry);
		if (enter) {
			pathTracking.pushListContext();
		}
		return enter;
	}

	@Override
	public void leaveCollectionEntry(ConfigEntry<?> entry) {
		delegate.leaveCollectionEntry(entry);
		pathTracking.popContext();
		entryVisited();
	}

	@Override
	public boolean enterCompoundEntry(ConfigEntry<?> entry) {
		boolean enter = delegate.enterCompoundEntry(entry);
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
	public void leaveCompoundEntry(ConfigEntry<?> entry) {
		delegate.leaveCompoundEntry(entry);
		pathTracking.popContext();
		entryVisited();
	}

	private void entryVisited() {
		if (pathTracking.currentContext() == PathTracking.Context.LIST) {
			pathTracking.incrementListIndex();
		}
	}
}
