package de.siphalor.tweed5.dataapi.api;

import org.jetbrains.annotations.NotNull;

public interface TweedDataVisitor {
	void visitNull();
	void visitBoolean(boolean value);
	void visitByte(byte value);
	void visitShort(short value);
	void visitInt(int value);
	void visitLong(long value);
	void visitFloat(float value);
	void visitDouble(double value);
	void visitString(@NotNull String value);

	default void visitEmptyList() {
		visitListStart();
		visitListEnd();
	}
	void visitListStart();
	void visitListEnd();

	default void visitEmptyMap() {
		visitMapStart();
		visitMapEnd();
	}
	void visitMapStart();
	void visitMapEntryKey(String key);
	void visitMapEnd();

	void visitComment(String comment);
}
