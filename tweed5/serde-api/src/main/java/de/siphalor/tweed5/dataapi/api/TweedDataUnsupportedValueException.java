package de.siphalor.tweed5.dataapi.api;

import lombok.Getter;

@Getter
public class TweedDataUnsupportedValueException extends Exception {
	private final Object value;

	public TweedDataUnsupportedValueException(Object value) {
		super("Unsupported value " + value + " of type " + value.getClass().getName());
		this.value = value;
	}
}
