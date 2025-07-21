package de.siphalor.tweed5.dataapi.api;

import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DelegatingTweedDataVisitor implements TweedDataVisitor {
	protected final TweedDataVisitor delegate;

	@Override
	public void visitNull() {
		beforeValueWrite();
		delegate.visitNull();
	}

	@Override
	public void visitBoolean(boolean value) {
		beforeValueWrite();
		delegate.visitBoolean(value);
	}

	@Override
	public void visitByte(byte value) {
		beforeValueWrite();
		delegate.visitByte(value);
	}

	@Override
	public void visitShort(short value) {
		beforeValueWrite();
		delegate.visitShort(value);
	}

	@Override
	public void visitInt(int value) {
		beforeValueWrite();
		delegate.visitInt(value);
	}

	@Override
	public void visitLong(long value) {
		beforeValueWrite();
		delegate.visitLong(value);
	}

	@Override
	public void visitFloat(float value) {
		beforeValueWrite();
		delegate.visitFloat(value);
	}

	@Override
	public void visitDouble(double value) {
		beforeValueWrite();
		delegate.visitDouble(value);
	}

	@Override
	public void visitString(String value) {
		beforeValueWrite();
		delegate.visitString(value);
	}

	@Override
	public void visitEmptyList() {
		beforeValueWrite();
		delegate.visitEmptyList();
	}

	@Override
	public void visitListStart() {
		beforeValueWrite();
		delegate.visitListStart();
	}

	@Override
	public void visitListEnd() {
		delegate.visitListEnd();
	}

	@Override
	public void visitEmptyMap() {
		beforeValueWrite();
		delegate.visitEmptyMap();
	}

	@Override
	public void visitMapStart() {
		beforeValueWrite();
		delegate.visitMapStart();
	}

	@Override
	public void visitMapEntryKey(String key) {
		delegate.visitMapEntryKey(key);
	}

	@Override
	public void visitMapEnd() {
		delegate.visitMapEnd();
	}

	@Override
	public void visitValue(@Nullable Object value) throws TweedDataUnsupportedValueException {
		delegate.visitValue(value);
	}

	protected void beforeValueWrite() {}

	@Override
	public void visitDecoration(TweedDataDecoration decoration) {
		delegate.visitDecoration(decoration);
	}
}
