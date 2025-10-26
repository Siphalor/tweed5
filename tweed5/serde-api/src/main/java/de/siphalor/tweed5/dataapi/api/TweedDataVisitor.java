package de.siphalor.tweed5.dataapi.api;

import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import org.jspecify.annotations.Nullable;

public interface TweedDataVisitor {
	void visitNull();
	void visitBoolean(boolean value);
	void visitByte(byte value);
	void visitShort(short value);
	void visitInt(int value);
	void visitLong(long value);
	void visitFloat(float value);
	void visitDouble(double value);
	void visitString(String value);

	/**
	 * Visits an arbitrary value.
	 * <br />
	 * This method is allowed to throw a {@link TweedDataUnsupportedValueException} for any value,
	 * so call sites should <b>always</b> provide a fallback based on the primitive visitor methods.
	 * @param value the value to visit. May be {@code null}.
	 * @throws TweedDataUnsupportedValueException if the value is not supported by this visitor.
	 * The visitor should then proceed to write the value with the primitive visitor methods.
	 * @apiNote Please use the specific visitor methods if possible.
	 * This method is mainly provided for extensibility beyond the standard data types.
	 * This could, for example, be used to allow native support for {@link java.math.BigDecimal}
	 * or {@link java.util.UUID} values.
	 */
	default void visitValue(@Nullable Object value) throws TweedDataUnsupportedValueException {
		if (value == null) {
			visitNull();
		} else if (value instanceof Boolean) {
			visitBoolean((Boolean) value);
		} else if (value instanceof Byte) {
			visitByte((Byte) value);
		} else if (value instanceof Short) {
			visitShort((Short) value);
		} else if (value instanceof Integer) {
			visitInt((Integer) value);
		} else if (value instanceof Long) {
			visitLong((Long) value);
		} else if (value instanceof Float) {
			visitFloat((Float) value);
		} else if (value instanceof Double) {
			visitDouble((Double) value);
		} else if (value instanceof String) {
			visitString((String) value);
		} else {
			throw new TweedDataUnsupportedValueException(value);
		}
	}

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

	/**
	 * Visits a decoration. The implementation <b>may</b> choose to ignore the decoration.
	 */
	void visitDecoration(TweedDataDecoration decoration);
}
