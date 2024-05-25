package de.siphalor.tweed5.core.api.validation;

public class ConfigEntryValueValidationException extends Exception {
	public ConfigEntryValueValidationException() {
	}

	public ConfigEntryValueValidationException(String message) {
		super(message);
	}

	public ConfigEntryValueValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigEntryValueValidationException(Throwable cause) {
		super(cause);
	}
}
