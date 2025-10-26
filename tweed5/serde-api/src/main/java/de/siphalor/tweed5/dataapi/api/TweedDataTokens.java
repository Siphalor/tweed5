package de.siphalor.tweed5.dataapi.api;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class TweedDataTokens {
	private static final TweedDataToken NULL = new TweedDataToken() {
		@Override
		public boolean isNull() {
			return true;
		}

		@Override
		public String toString() {
			return "NULL";
		}
	};

	private static final TweedDataToken LIST_START = new TweedDataToken() {
		@Override
		public boolean isListStart() {
			return true;
		}

		@Override
		public String toString() {
			return "LIST_START";
		}
	};

	private static final TweedDataToken LIST_END = new TweedDataToken() {
		@Override
		public boolean isListEnd() {
			return true;
		}

		@Override
		public String toString() {
			return "LIST_END";
		}
	};

	private static final TweedDataToken MAP_START = new TweedDataToken() {
		@Override
		public boolean isMapStart() {
			return true;
		}

		@Override
		public String toString() {
			return "MAP_START";
		}
	};

	private static final TweedDataToken MAP_END = new TweedDataToken() {
		@Override
		public boolean isMapEnd() {
			return true;
		}

		@Override
		public String toString() {
			return "MAP_END";
		}
	};

	public static TweedDataToken getNull() {
		return NULL;
	}

	public static TweedDataToken getListStart() {
		return LIST_START;
	}

	public static TweedDataToken asListValue(TweedDataToken delegate) {
		return new DelegatingToken(delegate) {
			@Override
			public boolean isListValue() {
				return true;
			}

			@Override
			public String toString() {
				return "LIST_VALUE[" + delegate.toString() + "]";
			}
		};
	}

	public static TweedDataToken getListEnd() {
		return LIST_END;
	}

	public static TweedDataToken getMapStart() {
		return MAP_START;
	}

	public static TweedDataToken asMapEntryKey(TweedDataToken delegate) {
		return new DelegatingToken(delegate) {
			@Override
			public boolean isMapEntryKey() {
				return true;
			}

			@Override
			public String toString() {
				return "MAP_ENTRY_KEY[" + delegate.toString() + "]";
			}
		};
	}

	public static TweedDataToken asMapEntryValue(TweedDataToken delegate) {
		return new DelegatingToken(delegate) {
			@Override
			public boolean isMapEntryValue() {
				return true;
			}

			@Override
			public String toString() {
				return "MAP_ENTRY_VALUE[" + delegate.toString() + "]";
			}
		};
	}

	public static TweedDataToken getMapEnd() {
		return MAP_END;
	}

	@RequiredArgsConstructor
	private static class DelegatingToken implements TweedDataToken {
		private final TweedDataToken delegate;

		@Override
		public boolean isNull() {
			return delegate.isNull();
		}

		@Override
		public boolean canReadAsBoolean() {
			return delegate.canReadAsBoolean();
		}

		@Override
		public boolean readAsBoolean() throws TweedDataReadException {
			return delegate.readAsBoolean();
		}

		@Override
		public boolean canReadAsByte() {
			return delegate.canReadAsByte();
		}

		@Override
		public byte readAsByte() throws TweedDataReadException {
			return delegate.readAsByte();
		}

		@Override
		public boolean canReadAsShort() {
			return delegate.canReadAsShort();
		}

		@Override
		public short readAsShort() throws TweedDataReadException {
			return delegate.readAsShort();
		}

		@Override
		public boolean canReadAsInt() {
			return delegate.canReadAsInt();
		}

		@Override
		public int readAsInt() throws TweedDataReadException {
			return delegate.readAsInt();
		}

		@Override
		public boolean canReadAsLong() {
			return delegate.canReadAsLong();
		}

		@Override
		public long readAsLong() throws TweedDataReadException {
			return delegate.readAsLong();
		}

		@Override
		public boolean canReadAsFloat() {
			return delegate.canReadAsFloat();
		}

		@Override
		public float readAsFloat() throws TweedDataReadException {
			return delegate.readAsFloat();
		}

		@Override
		public boolean canReadAsDouble() {
			return delegate.canReadAsDouble();
		}

		@Override
		public double readAsDouble() throws TweedDataReadException {
			return delegate.readAsDouble();
		}

		@Override
		public boolean canReadAsString() {
			return delegate.canReadAsString();
		}

		@Override
		public String readAsString() throws TweedDataReadException {
			return delegate.readAsString();
		}

		@Override
		public boolean isListStart() {
			return delegate.isListStart();
		}

		@Override
		public boolean isListValue() {
			return delegate.isListValue();
		}

		@Override
		public boolean isListEnd() {
			return delegate.isListEnd();
		}

		@Override
		public boolean isMapStart() {
			return delegate.isMapStart();
		}

		@Override
		public boolean isMapEntryKey() {
			return delegate.isMapEntryKey();
		}

		@Override
		public boolean isMapEntryValue() {
			return delegate.isMapEntryValue();
		}

		@Override
		public boolean isMapEnd() {
			return delegate.isMapEnd();
		}

		@Override
		public String toString() {
			return delegate.toString() + "(delegated)";
		}
	}
}
