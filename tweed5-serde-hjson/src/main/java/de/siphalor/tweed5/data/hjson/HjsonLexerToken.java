package de.siphalor.tweed5.data.hjson;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
@Value
public class HjsonLexerToken {
	Type type;
	HjsonReadPosition begin;
	HjsonReadPosition end;
	@EqualsAndHashCode.Exclude
	@Nullable
	CharSequence content;

	@EqualsAndHashCode.Include
	@Nullable
	public String contentString() {
		return content == null ? null : content.toString();
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(type.toString());
		if (content != null) {
			stringBuilder.append(" (\"");
			stringBuilder.append(content);
			stringBuilder.append("\")");
		}

		stringBuilder.append(" at ");
		stringBuilder.append(begin);

		if (!begin.equals(end)) {
			stringBuilder.append(" to ");
			stringBuilder.append(end);
		}
		return stringBuilder.toString();
	}

	enum Type {
		EOF,
		BRACKET_OPEN,
		BRACKET_CLOSE,
		BRACE_OPEN,
		BRACE_CLOSE,
		COMMA,
		COLON,
		LINE_FEED,

		NULL,
		TRUE,
		FALSE,
		NUMBER,
		QUOTELESS_STRING,
		JSON_STRING,
		MULTILINE_STRING,
	}
}
