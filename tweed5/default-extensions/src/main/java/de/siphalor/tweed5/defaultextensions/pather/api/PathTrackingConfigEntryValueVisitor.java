package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryValueVisitor;
import de.siphalor.tweed5.core.api.entry.StructuredConfigEntry;
import de.siphalor.tweed5.core.api.entry.SubEntryKey;
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
	public <T> boolean enterStructuredEntry(StructuredConfigEntry<T> entry, T value) {
		return delegate.enterStructuredEntry(entry, value);
	}

	@Override
	public boolean enterSubEntry(SubEntryKey subEntryKey) {
		boolean enter = delegate.enterSubEntry(subEntryKey);
		if (enter) {
			if (subEntryKey.value() != null) {
				pathTracking.pushPathPart(subEntryKey.entry(), subEntryKey.value());
			} else  {
				pathTracking.pushEmptyValuePathPart(subEntryKey.entry());
			}
		}
		return enter;
	}

	@Override
	public void leaveSubEntry(SubEntryKey subEntryKey) {
		delegate.leaveSubEntry(subEntryKey);
		pathTracking.popPathPart();
	}

	@Override
	public <T> void leaveStructuredEntry(StructuredConfigEntry<T> entry, T value) {
		delegate.leaveStructuredEntry(entry, value);
	}
}
