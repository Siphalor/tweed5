package de.siphalor.tweed5.serde_api.api;

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
