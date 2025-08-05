package de.siphalor.tweed5.commentloaderextension.api;

import de.siphalor.tweed5.commentloaderextension.impl.CommentLoaderExtensionImpl;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;

public interface CommentLoaderExtension extends TweedExtension {
	Class<? extends CommentLoaderExtension> DEFAULT = CommentLoaderExtensionImpl.class;
	String EXTENSION_ID = "comment-loader";

	void loadComments(TweedDataReader reader, CommentPathProcessor pathProcessor);

	default String getId() {
		return EXTENSION_ID;
	}
}
