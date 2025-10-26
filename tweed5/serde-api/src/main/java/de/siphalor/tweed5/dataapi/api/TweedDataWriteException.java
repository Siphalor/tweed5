package de.siphalor.tweed5.dataapi.api;

public class TweedDataWriteException extends RuntimeException {
	public TweedDataWriteException() {
	}

	public TweedDataWriteException(String message) {
		super(message);
	}

	public TweedDataWriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public TweedDataWriteException(Throwable cause) {
		super(cause);
	}
}
