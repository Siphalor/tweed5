package de.siphalor.tweed5.defaultextensions.comment.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.comment.impl.CommentExtensionImpl;
import org.jspecify.annotations.Nullable;

public interface CommentExtension extends TweedExtension {
	Class<? extends CommentExtension> DEFAULT = CommentExtensionImpl.class;

	@Nullable String getFullComment(ConfigEntry<?> configEntry);
}
