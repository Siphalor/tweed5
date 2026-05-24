package de.siphalor.tweed5.serde.extension.api.read.result;


import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import de.siphalor.tweed5.serde.extension.api.path.EntryPath;
import lombok.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class TweedReadIssue {
	@Getter
	private final Exception exception;
	private final TweedReadContext readContext;
	@Getter
	private final EntryPath entryPath;
	@Getter
	private final EntryPath valuePath;

	public static TweedReadIssue error(String message, TweedReadContext readContext) {
		return error(new Exception(message), readContext);
	}

	public static TweedReadIssue error(Exception exception, TweedReadContext readContext) {
		return new TweedReadIssue(
				exception,
				readContext,
				readContext.currentEntryPath(),
				readContext.currentValuePath()
		);
	}

	@Override
	public String toString() {
		return "TweedReadIssue(" + exception + ")";
	}
}
