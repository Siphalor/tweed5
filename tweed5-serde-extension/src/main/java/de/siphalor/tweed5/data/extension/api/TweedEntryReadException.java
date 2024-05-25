package de.siphalor.tweed5.data.extension.api;

public class TweedEntryReadException extends Exception {
	public TweedEntryReadException() {
	}

	public TweedEntryReadException(String message) {
		super(message);
	}

	public TweedEntryReadException(String message, Throwable cause) {
		super(message, cause);
	}

	public TweedEntryReadException(Throwable cause) {
		super(cause);
	}
}
