package de.siphalor.tweed5.defaultextensions.comment.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import org.jetbrains.annotations.Nullable;

public interface CommentExtension extends TweedExtension {
	@Nullable String getFullComment(ConfigEntry<?> configEntry);
}
