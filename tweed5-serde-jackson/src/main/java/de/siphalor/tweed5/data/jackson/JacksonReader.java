package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.dataapi.api.TweedDataTokens;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class JacksonReader implements TweedDataReader {
	private final JsonParser parser;
	private final Deque<Context> contextStack = new ArrayDeque<>();

	private @Nullable TweedDataToken peekedToken;

	public JacksonReader(JsonParser parser) {
		this.parser = parser;
		this.contextStack.push(Context.VALUE);
	}

	@Override
	public TweedDataToken peekToken() throws TweedDataReadException {
		if (peekedToken == null) {
			peekedToken = nextToken();
		}
		return peekedToken;
	}

	@Override
	public TweedDataToken readToken() throws TweedDataReadException {
		if (peekedToken != null) {
			TweedDataToken token = peekedToken;
			peekedToken = null;
			return token;
		}
		return nextToken();
	}

	private TweedDataToken nextToken() throws TweedDataReadException {
		try {
			JsonToken jsonToken = parser.nextToken();
			switch (jsonToken) {
				case START_ARRAY: {
					TweedDataToken token = wrapToken(TweedDataTokens.getListStart());
					contextStack.push(Context.LIST);
					return token;
				}
				case END_ARRAY: {
					popContext(Context.LIST);
					TweedDataToken token = wrapToken(TweedDataTokens.getListEnd());
					afterValueRead();
					return token;
				}
				case START_OBJECT: {
					TweedDataToken token = wrapToken(TweedDataTokens.getMapStart());
					contextStack.push(Context.MAP);
					return token;
				}
				case END_OBJECT: {
					popContext(Context.MAP);
					TweedDataToken token = wrapToken(TweedDataTokens.getMapEnd());
					afterValueRead();
					return token;
				}
				case FIELD_NAME:
					contextStack.push(Context.MAP_ENTRY_VALUE);
					return TweedDataTokens.asMapEntryKey(createStringToken(parser.getText()));
				case VALUE_NULL: {
					TweedDataToken token = wrapToken(TweedDataTokens.getNull());
					afterValueRead();
					return token;
				}
				case VALUE_TRUE: {
					TweedDataToken token = wrapToken(createBooleanToken(true));
					afterValueRead();
					return token;
				}
				case VALUE_FALSE: {
					TweedDataToken token = wrapToken(createBooleanToken(false));
					afterValueRead();
					return token;
				}
				case VALUE_NUMBER_INT: {
					long longValue = parser.getLongValue();
					TweedDataToken token = wrapToken(new TweedDataToken() {
						@Override
						public boolean canReadAsByte() {
							return longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE;
						}

						@Override
						public byte readAsByte() {
							return (byte) longValue;
						}

						@Override
						public boolean canReadAsShort() {
							return longValue >= Short.MIN_VALUE && longValue <= Short.MAX_VALUE;
						}

						@Override
						public short readAsShort() {
							return (short) longValue;
						}

						@Override
						public boolean canReadAsInt() {
							return longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE;
						}

						@Override
						public int readAsInt() {
							return (int) longValue;
						}

						@Override
						public boolean canReadAsLong() {
							return true;
						}

						@Override
						public long readAsLong() {
							return longValue;
						}

						@Override
						public boolean canReadAsFloat() {
							return true;
						}

						@Override
						public float readAsFloat() {
							return (float) longValue;
						}

						@Override
						public boolean canReadAsDouble() {
							return true;
						}

						@Override
						public double readAsDouble() {
							return (double) longValue;
						}
					});
					afterValueRead();
					return token;
				}
				case VALUE_NUMBER_FLOAT: {
					float floatValue = parser.getFloatValue();
					double doubleValue = parser.getDoubleValue();
					TweedDataToken token = wrapToken(new TweedDataToken() {
						@Override
						public boolean canReadAsFloat() {
							return true;
						}

						@Override
						public float readAsFloat() {
							return floatValue;
						}

						@Override
						public boolean canReadAsDouble() {
							return true;
						}

						@Override
						public double readAsDouble() {
							return doubleValue;
						}
					});
					afterValueRead();
					return token;
				}
				case VALUE_STRING: {
					TweedDataToken token = wrapToken(createStringToken(parser.getText()));
					afterValueRead();
					return token;
				}
				case VALUE_EMBEDDED_OBJECT:
					throw TweedDataReadException.builder()
							.message("Encountered unsupported embedded object at " + parser.currentLocation())
							.build();
				case NOT_AVAILABLE:
					throw TweedDataReadException.builder()
							.message("Encountered unexpected NOT_AVAILABLE token at " + parser.currentLocation())
							.build();
				default:
					throw TweedDataReadException.builder()
							.message("Encountered unexpected token " + jsonToken + " at " + parser.currentLocation())
							.build();
			}
		} catch (IOException e) {
			throw TweedDataReadException.builder()
					.message("Error reading data using jackson at " + parser.currentLocation())
					.cause(e)
					.build();
		}
	}

	private TweedDataToken wrapToken(TweedDataToken token) throws TweedDataReadException {
		Context context = peekContext();
		switch (context) {
			case LIST:
				return TweedDataTokens.asListValue(token);
			case MAP_ENTRY_VALUE:
				return TweedDataTokens.asMapEntryValue(token);
			case VALUE:
				return token;
			default:
				throw TweedDataReadException.builder()
						.message("Encountered token " + token + " in unexpected context: " + context)
						.build();
		}
	}

	private void afterValueRead() throws TweedDataReadException {
		Context context = peekContext();
		switch (context) {
			case MAP_ENTRY_VALUE:
			case VALUE:
				contextStack.pop();
		}
	}

	private Context peekContext() throws TweedDataReadException {
		Context context = contextStack.peek();
		if (context == null) {
			throw TweedDataReadException.builder()
					.message("Tried to read context but currently not in any context")
					.build();
		}
		return context;
	}

	private void popContext(Context expectedContext) throws TweedDataReadException {
		Context context = contextStack.pop();
		if (context != expectedContext) {
			throw TweedDataReadException.builder()
					.message("Unexpected context " + context + " when popping " + expectedContext)
					.build();
		}
	}

	private TweedDataToken createStringToken(String value) {
		return new TweedDataToken() {
			@Override
			public boolean canReadAsString() {
				return true;
			}

			@Override
			public String readAsString() {
				return value;
			}
		};
	}

	private TweedDataToken createBooleanToken(boolean value) {
		return new TweedDataToken() {
			@Override
			public boolean canReadAsBoolean() {
				return true;
			}

			@Override
			public boolean readAsBoolean() {
				return value;
			}
		};
	}

	@Override
	public void close() throws Exception {
		parser.close();
	}

	private enum Context {
		VALUE,
		LIST,
		MAP,
		MAP_ENTRY_VALUE,
	}
}
