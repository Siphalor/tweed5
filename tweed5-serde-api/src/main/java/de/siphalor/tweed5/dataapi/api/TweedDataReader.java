package de.siphalor.tweed5.dataapi.api;

public interface TweedDataReader extends AutoCloseable {
	TweedDataToken peekToken() throws TweedDataReadException;
	TweedDataToken readToken() throws TweedDataReadException;
}
