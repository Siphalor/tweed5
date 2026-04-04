package de.siphalor.tweed5.serde.extension.api.read.result;


import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import lombok.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class TweedReadIssue {
	@Getter
	private final Exception exception;
	private final TweedReadContext readContext;

	public static TweedReadIssue error(String message, TweedReadContext readContext) {
		return new TweedReadIssue(new Exception(message), readContext);
	}

	public static TweedReadIssue error(Exception exception, TweedReadContext readContext) {
		return new TweedReadIssue(exception, readContext);
	}

	@Override
	public String toString() {
		return "TweedReadIssue(" + exception + ")";
	}
}
