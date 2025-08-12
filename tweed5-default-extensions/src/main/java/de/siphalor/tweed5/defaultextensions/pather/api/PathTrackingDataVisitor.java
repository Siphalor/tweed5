package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.dataapi.api.TweedDataUnsupportedValueException;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;

@RequiredArgsConstructor
public class PathTrackingDataVisitor implements TweedDataVisitor {
	private final TweedDataVisitor delegate;
	private final PathTracking pathTracking;
	private final ArrayDeque<Context> contextStack = new ArrayDeque<>(50);
	private final ArrayDeque<Integer> listIndexStack = new ArrayDeque<>(50);

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
	public void visitString(String value) {
		delegate.visitString(value);
		valueVisited();
	}

	@Override
	public void visitValue(@Nullable Object value) throws TweedDataUnsupportedValueException {
		TweedDataVisitor.super.visitValue(value);
		valueVisited();
	}

	private void valueVisited() {
		Context context = contextStack.peek();
		if (context == Context.MAP_ENTRY) {
			contextStack.pop();
			pathTracking.popPathPart();
		} else if (context == Context.LIST) {
			pathTracking.popPathPart();
			int index = listIndexStack.pop();
			listIndexStack.push(index + 1);
			pathTracking.pushPathPart(Integer.toString(index));
		}
	}

	@Override
	public void visitListStart() {
		delegate.visitListStart();
		contextStack.push(Context.LIST);
		listIndexStack.push(0);
		pathTracking.pushPathPart("0");
	}

	@Override
	public void visitListEnd() {
		delegate.visitListEnd();
		contextStack.pop();
		listIndexStack.pop();
		pathTracking.popPathPart();
		valueVisited();
	}

	@Override
	public void visitMapStart() {
		delegate.visitMapStart();
	}

	@Override
	public void visitMapEntryKey(String key) {
		delegate.visitMapEntryKey(key);
		pathTracking.pushPathPart(key);
		contextStack.push(Context.MAP_ENTRY);
	}

	@Override
	public void visitMapEnd() {
		delegate.visitMapEnd();
		valueVisited();
	}

	@Override
	public void visitDecoration(TweedDataDecoration decoration) {
		delegate.visitDecoration(decoration);
	}

	private enum Context {
		LIST, MAP_ENTRY,
	}
}
