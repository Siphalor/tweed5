package de.siphalor.tweed5.defaultextensions.comment.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;

@FunctionalInterface
public interface CommentProducer {
	String createComment(ConfigEntry<?> entry);
}
