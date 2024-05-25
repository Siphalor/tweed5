package de.siphalor.tweed5.data.hjson;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum HjsonCommentType {
	HASH(false),
	SLASHES(false),
	BLOCK(true),
	;

	private final boolean block;
}
