package de.siphalor.tweed5.commentloaderextension.api;

public interface CommentPathProcessor {
	MatchStatus matches(String path);
	String process(String path);

	enum MatchStatus {
		YES,
		NO,
		MAYBE_DEEPER,
	}
}
