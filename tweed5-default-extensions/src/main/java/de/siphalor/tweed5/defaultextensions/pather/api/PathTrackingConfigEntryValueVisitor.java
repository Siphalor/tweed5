package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@RequiredArgsConstructor
public class PathTrackingConfigEntryValueVisitor implements ConfigEntryValueVisitor {
	private final ConfigEntryValueVisitor delegate;
	private final ValuePathTracking pathTracking;

	@Override
	public <T extends @Nullable Object> void visitEntry(ConfigEntry<T> entry, T value) {
		delegate.visitEntry(entry, value);
	}

	@Override
	public <T> boolean enterStructuredEntry(ConfigEntry<T> entry, T value) {
		return delegate.enterStructuredEntry(entry, value);
	}

	@Override
	public boolean enterStructuredSubEntry(String entryKey, String valueKey) {
		boolean enter = delegate.enterStructuredSubEntry(entryKey, valueKey);
		if (enter) {
			pathTracking.pushPathPart(entryKey, valueKey);
		}
		return enter;
	}

	@Override
	public void leaveStructuredSubEntry(String entryKey, String valueKey) {
		delegate.leaveStructuredSubEntry(entryKey, valueKey);
		pathTracking.popPathPart();
	}

	@Override
	public <T> void leaveStructuredEntry(ConfigEntry<T> entry, T value) {
		delegate.leaveStructuredEntry(entry, value);
	}
}
