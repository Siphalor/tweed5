package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import lombok.RequiredArgsConstructor;

import java.util.ArrayDeque;

@RequiredArgsConstructor
public class PathTrackingDataReader implements TweedDataReader {
	private final TweedDataReader delegate;
	private final PathTracking pathTracking;
	private final ArrayDeque<Context> contextStack = new ArrayDeque<>(50);
	private final ArrayDeque<Integer> listIndexStack = new ArrayDeque<>(50);

	@Override
	public TweedDataToken peekToken() throws TweedDataReadException {
		return delegate.peekToken();
	}

	@Override
	public TweedDataToken readToken() throws TweedDataReadException {
		TweedDataToken token = delegate.readToken();
		if (token.isListValue()) {
			if (contextStack.peek() == Context.LIST) {
				int index = listIndexStack.pop() + 1;
				if (index != 0) {
					pathTracking.popPathPart();
				}
				pathTracking.pushPathPart(Integer.toString(index));
				listIndexStack.push(index);
			}
		}

		if (token.isListStart()) {
			contextStack.push(Context.LIST);
			listIndexStack.push(-1);
		} else if (token.isListEnd()) {
			contextStack.pop();
			int lastIndex = listIndexStack.pop();
			if (lastIndex >= 0) {
				pathTracking.popPathPart();
			}
		} else if (token.isMapStart()) {
			contextStack.push(Context.MAP);
			pathTracking.pushPathPart("$");
		} else if (token.isMapEntryKey()) {
			pathTracking.popPathPart();
			pathTracking.pushPathPart(token.readAsString());
		} else if (token.isMapEnd()) {
			pathTracking.popPathPart();
			contextStack.pop();
		}
		return token;
	}

	@Override
	public void close() throws Exception {
		delegate.close();
	}

	private enum Context {
		LIST, MAP,
	}
}
