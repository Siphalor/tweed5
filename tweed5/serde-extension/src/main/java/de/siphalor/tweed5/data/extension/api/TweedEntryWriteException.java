package de.siphalor.tweed5.data.extension.api;

import lombok.Getter;

@Getter
public class TweedEntryWriteException extends Exception {
	private final TweedWriteContext context;

	public TweedEntryWriteException(String message, TweedWriteContext context) {
		super(message);
		this.context = context;
	}

	public TweedEntryWriteException(String message, Throwable cause, TweedWriteContext context) {
		super(message, cause);
		this.context = context;
	}

	public TweedEntryWriteException(String message, TweedEntryWriteException cause) {
		super(message, cause);
		this.context = cause.context;
	}

	public TweedEntryWriteException(Throwable cause, TweedWriteContext context) {
		super(cause);
		this.context = context;
	}
}
