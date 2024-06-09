package de.siphalor.tweed5.defaultextensions.comment.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.AComment;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import lombok.Getter;
import lombok.Value;
import org.jetbrains.annotations.Nullable;

@AutoService(CommentExtension.class)
public class CommentExtensionImpl implements ReadWriteRelatedExtension, CommentExtension {
	@Getter
	private RegisteredExtensionData<EntryExtensionsData, InternalCommentEntryData> internalEntryDataExtension;
	private DefaultMiddlewareContainer<CommentProducer> middlewareContainer;

	@Override
	public String getId() {
		return "comment";
	}

	@Override
	public void setup(TweedExtensionSetupContext context) {
		internalEntryDataExtension = context.registerEntryExtensionData(InternalCommentEntryData.class);
		context.registerEntryExtensionData(AComment.class);

		middlewareContainer = new DefaultMiddlewareContainer<>();

		for (TweedExtension extension : context.configContainer().extensions()) {
			if (extension instanceof CommentModifyingExtension) {
				middlewareContainer.register(((CommentModifyingExtension) extension).commentMiddleware());
			}
		}

		middlewareContainer.seal();
	}

	@Override
	public @Nullable Middleware<TweedEntryWriter<?, ?>> entryWriterMiddleware() {
		return TweedEntryWriterCommentMiddleware.INSTANCE;
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		EntryExtensionsData entryExtensionsData = configEntry.extensionsData();
		String baseComment;
		if (entryExtensionsData.isPatchworkPartSet(AComment.class)) {
			baseComment = ((AComment) entryExtensionsData).value();
		} else {
			baseComment = "";
		}

		CommentProducer middleware = middlewareContainer.process(entry -> baseComment);
		internalEntryDataExtension.set(entryExtensionsData, new InternalCommentEntryDataImpl(middleware));
	}

	@Override
	@Nullable
	public String getFullComment(ConfigEntry<?> configEntry) {
		String comment = ((InternalCommentEntryData) configEntry.extensionsData()).commentProducer().createComment(configEntry);
		return comment.isEmpty() ? null : comment;
	}

	@Value
	private static class InternalCommentEntryDataImpl implements InternalCommentEntryData {
		CommentProducer commentProducer;
	}
}
