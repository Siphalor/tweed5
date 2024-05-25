package de.siphalor.tweed5.dataapi.api;

public interface TweedDataReader {
	TweedDataToken peekToken() throws TweedDataReadException;
	TweedDataToken readToken() throws TweedDataReadException;
}
