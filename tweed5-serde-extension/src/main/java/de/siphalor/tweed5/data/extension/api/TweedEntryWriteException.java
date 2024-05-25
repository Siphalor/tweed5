package de.siphalor.tweed5.data.extension.api;

public class TweedEntryWriteException extends Exception {
	public TweedEntryWriteException() {
	}

	public TweedEntryWriteException(String message) {
		super(message);
	}

	public TweedEntryWriteException(String message, Throwable cause) {
		super(message, cause);
	}

	public TweedEntryWriteException(Throwable cause) {
		super(cause);
	}
}
