package de.siphalor.tweed5.defaultextensions.comment.api;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.defaultextensions.comment.impl.CommentExtensionImpl;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface CommentExtension extends TweedExtension {
	Class<? extends CommentExtension> DEFAULT = CommentExtensionImpl.class;
	String EXTENSION_ID = "comment";

	static <C extends ConfigEntry<?>> Consumer<C> baseComment(String baseComment) {
		return entry -> {
			CommentExtension extension = entry.container().extension(CommentExtension.class)
					.orElseThrow(() -> new IllegalStateException("No comment extension registered"));
			extension.setBaseComment(entry, baseComment);
		};
	}

	void setBaseComment(ConfigEntry<?> configEntry, String baseComment);

	void recomputeFullComments();

	@Nullable String getFullComment(ConfigEntry<?> configEntry);
}
