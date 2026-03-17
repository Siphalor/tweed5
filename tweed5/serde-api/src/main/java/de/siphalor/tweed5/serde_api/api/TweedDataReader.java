package de.siphalor.tweed5.serde_api.api;

public interface TweedDataReader extends AutoCloseable {
	TweedDataToken peekToken() throws TweedDataReadException;
	TweedDataToken readToken() throws TweedDataReadException;
}
