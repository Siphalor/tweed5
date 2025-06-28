package de.siphalor.tweed5.dataapi.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

@Getter
public class TweedDataReadException extends Exception {
	@Nullable
	private final TweedDataReaderRecoverMode recoverMode;

	protected TweedDataReadException(String message, Throwable cause, @Nullable TweedDataReaderRecoverMode recoverMode) {
		super(message, cause);
		this.recoverMode = recoverMode;
	}

	public boolean canRecover() {
		return recoverMode != null;
	}

	public static Builder builder() {
		return new Builder();
	}

	@Setter
	public static class Builder {
		private String message;
		private Throwable cause;
		@Setter(AccessLevel.NONE)
		private @Nullable TweedDataReaderRecoverMode recoverMode;

		public TweedDataReadException build() {
			return new TweedDataReadException(message, cause, recoverMode);
		}

		public Builder recoverable(TweedDataReaderRecoverMode recoverMode) {
			this.recoverMode = recoverMode;
			return this;
		}
	}
}
