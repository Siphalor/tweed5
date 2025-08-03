package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PathTrackingDataReader implements TweedDataReader {
	private final TweedDataReader delegate;
	private final PathTracking pathTracking;

	@Override
	public TweedDataToken peekToken() throws TweedDataReadException {
		return delegate.peekToken();
	}

	@Override
	public TweedDataToken readToken() throws TweedDataReadException {
		TweedDataToken token = delegate.readToken();
		if (token.isListStart()) {
			pathTracking.pushListContext();
		} else if (token.isListValue()) {
			pathTracking.incrementListIndex();
		} else if (token.isListEnd()) {
			pathTracking.popContext();
		} else if (token.isMapStart()) {
			pathTracking.pushMapContext();
			pathTracking.pushPathPart("$");
		} else if (token.isMapEntryKey()) {
			pathTracking.popPathPart();
			pathTracking.pushPathPart(token.readAsString());
		} else if (token.isMapEnd()) {
			pathTracking.popPathPart();
			pathTracking.popContext();
		}
		return token;
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}
}
