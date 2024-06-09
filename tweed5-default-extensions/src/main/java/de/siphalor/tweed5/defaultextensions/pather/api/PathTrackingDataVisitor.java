package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
public class PathTrackingDataVisitor implements TweedDataVisitor {
	private final TweedDataVisitor delegate;
	private final PathTracking pathTracking;

	@Override
	public void visitNull() {
		delegate.visitNull();
		valueVisited();
	}

	@Override
	public void visitBoolean(boolean value) {
		delegate.visitBoolean(value);
		valueVisited();
	}

	@Override
	public void visitByte(byte value) {
		delegate.visitByte(value);
		valueVisited();
	}

	@Override
	public void visitShort(short value) {
		delegate.visitShort(value);
		valueVisited();
	}

	@Override
	public void visitInt(int value) {
		delegate.visitInt(value);
		valueVisited();
	}

	@Override
	public void visitLong(long value) {
		delegate.visitLong(value);
		valueVisited();
	}

	@Override
	public void visitFloat(float value) {
		delegate.visitFloat(value);
		valueVisited();
	}

	@Override
	public void visitDouble(double value) {
		delegate.visitDouble(value);
		valueVisited();
	}

	@Override
	public void visitString(@NotNull String value) {
		delegate.visitString(value);
		valueVisited();
	}

	private void valueVisited() {
		if (pathTracking.currentContext() == PathTracking.Context.LIST) {
			pathTracking.incrementListIndex();
		} else {
			pathTracking.popPathPart();
		}
	}

	@Override
	public void visitListStart() {
		delegate.visitListStart();
		pathTracking.pushListContext();
	}

	@Override
	public void visitListEnd() {
		delegate.visitListEnd();
		pathTracking.popContext();
		valueVisited();
	}

	@Override
	public void visitMapStart() {
		delegate.visitMapStart();
		pathTracking.pushMapContext();
	}

	@Override
	public void visitMapEntryKey(String key) {
		delegate.visitMapEntryKey(key);
		pathTracking.pushPathPart(key);
	}

	@Override
	public void visitMapEnd() {
		delegate.visitMapEnd();
		pathTracking.popContext();
		valueVisited();
	}

	@Override
	public void visitComment(String comment) {
		delegate.visitComment(comment);
	}
}
