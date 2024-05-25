package de.siphalor.tweed5.dataapi.api;

public interface TweedDataToken {
	default boolean isNull() {
		return false;
	}

	default boolean canReadAsBoolean() {
		return false;
	}
	default boolean readAsBoolean() throws TweedDataReadException {
		throw createUnsupportedReadException("boolean");
	}
	default boolean canReadAsByte() {
		return false;
	}
	default byte readAsByte() throws TweedDataReadException {
		throw createUnsupportedReadException("byte");
	}
	default boolean canReadAsShort() {
		return false;
	}
	default short readAsShort() throws TweedDataReadException {
		throw createUnsupportedReadException("short");
	}
	default boolean canReadAsInt() {
		return false;
	}
	default int readAsInt() throws TweedDataReadException {
		throw createUnsupportedReadException("integer");
	}
	default boolean canReadAsLong() {
		return false;
	}
	default long readAsLong() throws TweedDataReadException {
		throw createUnsupportedReadException("long");
	}
	default boolean canReadAsFloat() {
		return false;
	}
	default float readAsFloat() throws TweedDataReadException {
		throw createUnsupportedReadException("float");
	}
	default boolean canReadAsDouble() {
		return false;
	}
	default double readAsDouble() throws TweedDataReadException {
		throw createUnsupportedReadException("double");
	}
	default boolean canReadAsString() {
		return false;
	}
	default String readAsString() throws TweedDataReadException {
		throw createUnsupportedReadException("string");
	}

	default boolean isListStart() {
		return false;
	}
	default boolean isListValue() {
		return false;
	}
	default boolean isListEnd() {
		return false;
	}
	default boolean isMapStart() {
		return false;
	}
	default boolean isMapEntryKey() {
		return false;
	}
	default boolean isMapEntryValue() {
		return false;
	}
	default boolean isMapEnd() {
		return false;
	}

	default TweedDataReadException createUnsupportedReadException(String requestedType) {
		return TweedDataReadException.builder().message("Token can not be read as " + requestedType + ": " + this).build();
	}
}
