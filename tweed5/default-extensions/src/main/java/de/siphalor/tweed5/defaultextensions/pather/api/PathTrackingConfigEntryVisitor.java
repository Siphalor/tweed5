package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.Arity;
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
	}

	@Override
	public boolean enterStructuredEntry(ConfigEntry<?> entry) {
		return delegate.enterStructuredEntry(entry);
	}

	@Override
	public boolean enterStructuredSubEntry(String key, Arity arity) {
		boolean enter = delegate.enterStructuredSubEntry(key, arity);
		if (enter) {
			pathTracking.pushPathPart(key);
		}
		return enter;
	}

	@Override
	public void leaveStructuredSubEntry(String key, Arity arity) {
		delegate.leaveStructuredSubEntry(key, arity);
		pathTracking.popPathPart();
	}

	@Override
	public void leaveStructuredEntry(ConfigEntry<?> entry) {
		delegate.leaveStructuredEntry(entry);
	}
}
