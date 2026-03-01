package de.siphalor.tweed5.minecraft.networking.api;

import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.dataapi.api.TweedDataTokens;
import de.siphalor.tweed5.minecraft.networking.impl.ByteBufSerdeConstants;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;

@RequiredArgsConstructor
public class ByteBufReader implements TweedDataReader {
	private final ByteBuf buf;
	private final Deque<Context> contextStack = new ArrayDeque<>();
	private @Nullable TweedDataToken peek;

	@Override
	public TweedDataToken peekToken() throws TweedDataReadException {
		if (peek != null) return peek;
		ensureReadable();
		return (peek = nextToken());
	}

	@Override
	public TweedDataToken readToken() throws TweedDataReadException {
		if (peek != null) {
			TweedDataToken token = peek;
			peek = null;
			return token;
		}
		ensureReadable();
		return nextToken();
	}

	private void ensureReadable() throws TweedDataReadException {
		if (!buf.isReadable()) {
			throw TweedDataReadException.builder().message("Reached end of buffer").build();
		}
	}

	private TweedDataToken nextToken() throws TweedDataReadException {
		int b = Byte.toUnsignedInt(buf.readByte());
		switch (b) {
			case ByteBufSerdeConstants.NULL_VALUE:
				return wrapTokenForContext(TweedDataTokens.getNull());
			case ByteBufSerdeConstants.FALSE_VALUE:
				return wrapTokenForContext(BooleanToken.FALSE);
			case ByteBufSerdeConstants.TRUE_VALUE:
				return wrapTokenForContext(BooleanToken.TRUE);
			case ByteBufSerdeConstants.EMPTY_STRING_VALUE:
				return wrapTokenForContext(new StringToken(""));
			case ByteBufSerdeConstants.VARNUM_VARIANT_INT8:
				return wrapTokenForContext(new ByteToken(buf.readByte()));
			case ByteBufSerdeConstants.VARNUM_VARIANT_INT16:
				return wrapTokenForContext(new IntToken(buf.readShort()));
			case ByteBufSerdeConstants.VARNUM_VARIANT_INT32:
				return wrapTokenForContext(new IntToken(buf.readInt()));
			case ByteBufSerdeConstants.VARNUM_VARIANT_INT64: {
				long value = buf.readLong();
				if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
					return wrapTokenForContext(new IntToken((int) value));
				} else {
					return wrapTokenForContext(new TweedDataToken() {
						@Override
						public boolean canReadAsLong() {
							return true;
						}

						@Override
						public long readAsLong() {
							return value;
						}
					});
				}
			}
			case ByteBufSerdeConstants.VARNUM_VARIANT_FLOAT:
				return wrapTokenForContext(new TweedDataToken() {
					private final float value = buf.readFloat();
					@Override
					public boolean canReadAsFloat() {
						return true;
					}

					@Override
					public float readAsFloat() {
						return value;
					}
				});
			case ByteBufSerdeConstants.VARNUM_VARIANT_DOUBLE:
				return wrapTokenForContext(new TweedDataToken() {
					private final double value = buf.readDouble();
					@Override
					public boolean canReadAsDouble() {
						return true;
					}

					@Override
					public double readAsDouble() {
						return value;
					}
				});
			case ByteBufSerdeConstants.COMPLEX_VARIANT_STRING: {
				int length = buf.readInt();
				ByteBuf byteBuf = buf.readBytes(length);
				return wrapTokenForContext(new StringToken(byteBuf.toString(StandardCharsets.UTF_8)));
			}
			case ByteBufSerdeConstants.COMPLEX_VARIANT_LIST: {
				TweedDataToken token = wrapTokenForContext(TweedDataTokens.getListStart(), false);
				contextStack.push(Context.LIST);
				return token;
			}
			case ByteBufSerdeConstants.COMPLEX_VARIANT_MAP: {
				TweedDataToken token = wrapTokenForContext(TweedDataTokens.getMapStart(), false);
				contextStack.push(Context.MAP);
				return token;
			}
			case ByteBufSerdeConstants.COMPLEX_VARIANT_END: {
				Context context = contextStack.pop();
				return wrapTokenForContext(
						context == Context.MAP
								? TweedDataTokens.getMapEnd()
								: TweedDataTokens.getListEnd()
				);
			}
			default:
				int specialEmbedType = b & ByteBufSerdeConstants.SPECIAL_EMBED_TYPE_MASK;
				if (specialEmbedType == ByteBufSerdeConstants.UINT6_TYPE) {
					return wrapTokenForContext(new ByteToken((byte) (b & ByteBufSerdeConstants.SPECIAL_EMBED_VALUE_MASK)));
				} else if (specialEmbedType == ByteBufSerdeConstants.SMALL_STRING_TYPE) {
					int length = (b & ByteBufSerdeConstants.SPECIAL_EMBED_VALUE_MASK) + 1;
					ByteBuf byteBuf = buf.readBytes(length);
					return wrapTokenForContext(new StringToken(byteBuf.toString(StandardCharsets.UTF_8)));
				}
				throw TweedDataReadException.builder()
						.message("Unknown type byte value " + Integer.toBinaryString(b))
						.build();
		}
	}

	private TweedDataToken wrapTokenForContext(TweedDataToken token) {
		return wrapTokenForContext(token, true);
	}

	private TweedDataToken wrapTokenForContext(TweedDataToken token, boolean isValueEnd) {
		Context context = contextStack.peek();
		if (context == null) {
			return token;
		} else if (context == Context.LIST) {
			return TweedDataTokens.asListValue(token);
		} else if (context == Context.MAP) {
			contextStack.push(Context.MAP_VALUE);
			return TweedDataTokens.asMapEntryKey(token);
		} else {
			if (isValueEnd) {
				contextStack.pop();
			}
			return TweedDataTokens.asMapEntryValue(token);
		}
	}

	@Override
	public void close() {

	}

	private enum Context {
		LIST, MAP, MAP_VALUE
	}

	@RequiredArgsConstructor
	private static class BooleanToken implements TweedDataToken {
		public static final BooleanToken FALSE = new BooleanToken(false);
		public static final BooleanToken TRUE = new BooleanToken(true);

		private final boolean value;

		@Override
		public boolean canReadAsBoolean() {
			return true;
		}

		@Override
		public boolean readAsBoolean() {
			return value;
		}
	}

	@RequiredArgsConstructor
	private static class ByteToken implements TweedDataToken {
		private final byte value;

		@Override
		public boolean canReadAsByte() {
			return true;
		}

		@Override
		public byte readAsByte() {
			return value;
		}

		@Override
		public boolean canReadAsShort() {
			return true;
		}

		@Override
		public short readAsShort() {
			return value;
		}

		@Override
		public boolean canReadAsInt() {
			return true;
		}

		@Override
		public int readAsInt() {
			return value;
		}

		@Override
		public boolean canReadAsLong() {
			return true;
		}

		@Override
		public long readAsLong() {
			return value;
		}
	}

	@RequiredArgsConstructor
	public static class IntToken implements TweedDataToken {
		private final int value;

		@Override
		public boolean canReadAsByte() {
			return value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE;
		}

		@Override
		public byte readAsByte() {
			return (byte) value;
		}

		@Override
		public boolean canReadAsShort() {
			return value >= Short.MIN_VALUE && value <= Short.MAX_VALUE;
		}

		@Override
		public short readAsShort() {
			return (short) value;
		}

		@Override
		public boolean canReadAsInt() {
			return true;
		}

		@Override
		public int readAsInt() {
			return value;
		}

		@Override
		public boolean canReadAsLong() {
			return true;
		}

		@Override
		public long readAsLong() {
			return value;
		}
	}

	@RequiredArgsConstructor
	public static class StringToken implements TweedDataToken {
		private final String value;

		@Override
		public boolean canReadAsString() {
			return true;
		}

		@Override
		public String readAsString() {
			return value;
		}
	}
}
