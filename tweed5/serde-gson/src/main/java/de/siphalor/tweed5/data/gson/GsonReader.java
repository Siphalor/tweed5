package de.siphalor.tweed5.data.gson;

import com.google.gson.stream.JsonReader;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.dataapi.api.TweedDataTokens;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class GsonReader implements TweedDataReader {
	private final JsonReader reader;

	private final Deque<Context> contextStack = new ArrayDeque<>();

	private @Nullable TweedDataToken peekedToken;

	public GsonReader(JsonReader reader) {
		this.reader = reader;
		contextStack.push(Context.VALUE);
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
			switch (reader.peek()) {
				case BEGIN_ARRAY: {
					reader.beginArray();
					TweedDataToken token = wrapToken(TweedDataTokens.getListStart());
					contextStack.push(Context.LIST);
					return token;
				}
				case END_ARRAY: {
					reader.endArray();
					popContext(Context.LIST);
					TweedDataToken token = wrapToken(TweedDataTokens.getListEnd());
					afterValueRead();
					return token;
				}
				case BEGIN_OBJECT: {
					reader.beginObject();
					TweedDataToken token = wrapToken(TweedDataTokens.getMapStart());
					contextStack.push(Context.MAP);
					return token;
				}
				case END_OBJECT: {
					reader.endObject();
					popContext(Context.MAP);
					TweedDataToken token = wrapToken(TweedDataTokens.getMapEnd());
					afterValueRead();
					return token;
				}
				case NAME:
					contextStack.push(Context.MAP_ENTRY_VALUE);
					return TweedDataTokens.asMapEntryKey(createStringToken(reader.nextName()));
				case NULL: {
					reader.nextNull();
					TweedDataToken token = wrapToken(TweedDataTokens.getNull());
					afterValueRead();
					return token;
				}
				case BOOLEAN: {
					boolean value = reader.nextBoolean();
					TweedDataToken token = wrapToken(new TweedDataToken() {
						@Override
						public boolean canReadAsBoolean() {
							return true;
						}

						@Override
						public boolean readAsBoolean() throws TweedDataReadException {
							return value;
						}
					});
					afterValueRead();
					return token;
				}
				case NUMBER: {
					Long longValue = tryReadLong();
					TweedDataToken token;
					if (longValue != null) {
						token = wrapToken(new TweedDataToken() {
							@Override
							public boolean canReadAsByte() {
								return longValue >= Byte.MIN_VALUE && longValue <= Byte.MAX_VALUE;
							}

							@Override
							public byte readAsByte() {
								return longValue.byteValue();
							}

							@Override
							public boolean canReadAsShort() {
								return longValue >= Short.MIN_VALUE && longValue <= Short.MAX_VALUE;
							}

							@Override
							public short readAsShort() {
								return longValue.shortValue();
							}

							@Override
							public boolean canReadAsInt() {
								return longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE;
							}

							@Override
							public int readAsInt() {
								return longValue.intValue();
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
								return longValue.floatValue();
							}

							@Override
							public boolean canReadAsDouble() {
								return true;
							}

							@Override
							public double readAsDouble() {
								return longValue.doubleValue();
							}
						});
					} else {
						double doubleValue = reader.nextDouble();
						token = wrapToken(new TweedDataToken() {
							@Override
							public boolean canReadAsFloat() {
								return true;
							}

							@Override
							public float readAsFloat() {
								return (float) doubleValue;
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
					}
					afterValueRead();
					return token;
				}
				case STRING: {
					TweedDataToken token = wrapToken(createStringToken(reader.nextString()));
					afterValueRead();
					return token;
				}
				default:
					throw TweedDataReadException.builder()
							.message("Encountered unexpected " + peekedToken + " token at " + reader.getPath())
							.build();
			}
		} catch (IOException e) {
			throw TweedDataReadException.builder()
					.message("Error reading data using gson at " + reader.getPath())
					.cause(e)
					.build();
		}
	}

	private @Nullable Long tryReadLong() throws IOException {
		try {
			return reader.nextLong();
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private TweedDataToken wrapToken(TweedDataToken token) throws TweedDataReadException {
		switch (peekContext()) {
			case MAP_ENTRY_VALUE:
				return TweedDataTokens.asMapEntryValue(token);
			case LIST:
				return TweedDataTokens.asListValue(token);
			default:
				return token;
		}
	}

	private void afterValueRead() throws TweedDataReadException {
		Context context = peekContext();
		switch (context) {
			case MAP_ENTRY_VALUE:
			case VALUE:
				popContext(context);
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

	@Override
	public void close() throws Exception {
		reader.close();
	}

	private enum Context {
		VALUE,
		LIST,
		MAP,
		MAP_ENTRY_VALUE,
	}
}
