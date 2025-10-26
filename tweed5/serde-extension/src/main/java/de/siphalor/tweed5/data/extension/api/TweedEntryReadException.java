package de.siphalor.tweed5.data.extension.api;

import lombok.Getter;

@Getter
public class TweedEntryReadException extends Exception {
	private final TweedReadContext context;

	public TweedEntryReadException(String message, TweedReadContext context) {
		super(message);
		this.context = context;
	}

	public TweedEntryReadException(String message, Throwable cause, TweedReadContext context) {
		super(message, cause);
		this.context = context;
	}

	public TweedEntryReadException(String message, TweedEntryReadException cause) {
		super(message, cause);
		this.context = cause.context;
	}

	public TweedEntryReadException(Throwable cause, TweedReadContext context) {
		super(cause);
		this.context = context;
	}
}
