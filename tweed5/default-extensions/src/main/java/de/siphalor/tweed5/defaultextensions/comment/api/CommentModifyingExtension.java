package de.siphalor.tweed5.defaultextensions.comment.api;

import de.siphalor.tweed5.core.api.middleware.Middleware;

public interface CommentModifyingExtension {
	Middleware<CommentProducer> commentMiddleware();
}
