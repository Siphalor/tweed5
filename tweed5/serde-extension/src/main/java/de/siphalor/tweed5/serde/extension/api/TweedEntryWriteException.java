package de.siphalor.tweed5.serde.extension.api;

import lombok.Getter;

@Getter
public class TweedEntryWriteException extends Exception {
	private final TweedWriteContext context;

	public TweedEntryWriteException(String message, TweedWriteContext context) {
		super("At " + context.currentValuePath() + ": " + message);
		this.context = context;
	}

	public TweedEntryWriteException(String message, Throwable cause, TweedWriteContext context) {
		super("At " + context.currentValuePath() + ": " + message, cause);
		this.context = context;
	}

	public TweedEntryWriteException(String message, TweedEntryWriteException cause) {
		super("At " + cause.context.currentValuePath() + ": " + message, cause);
		this.context = cause.context;
	}

	public TweedEntryWriteException(Throwable cause, TweedWriteContext context) {
		super("At " + context.currentValuePath() + ": " + cause.getMessage(), cause);
		this.context = context;
	}
}
