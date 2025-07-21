package de.siphalor.tweed5.defaultextensions.comment.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.Data;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@AutoService(CommentExtension.class)
public class CommentExtensionImpl implements ReadWriteRelatedExtension, CommentExtension {
	private final ConfigContainer<?> configContainer;
	@Getter
	private final PatchworkPartAccess<CustomEntryData> customEntryDataAccess;
	private final DefaultMiddlewareContainer<CommentProducer> middlewareContainer;
	@Getter
	private @Nullable PatchworkPartAccess<Boolean> writerInstalledReadWriteContextAccess;

	public CommentExtensionImpl(ConfigContainer<?> configContainer, TweedExtensionSetupContext context) {
		this.configContainer = configContainer;
		this.customEntryDataAccess = context.registerEntryExtensionData(CustomEntryData.class);
		this.middlewareContainer = new DefaultMiddlewareContainer<>();
	}

	@Override
	public String getId() {
		return "comment";
	}

	@Override
	public void extensionsFinalized() {
		for (TweedExtension extension : configContainer.extensions()) {
			if (extension instanceof CommentModifyingExtension) {
				middlewareContainer.register(((CommentModifyingExtension) extension).commentMiddleware());
			}
		}
		middlewareContainer.seal();
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		writerInstalledReadWriteContextAccess = context.registerReadWriteContextExtensionData(Boolean.class);
		context.registerWriterMiddleware(new TweedEntryWriterCommentMiddleware(this));
	}

	@Override
	public void setBaseComment(ConfigEntry<?> configEntry, String baseComment) {
		if (configEntry.container() != configContainer) {
			throw new IllegalArgumentException("config entry doesn't belong to config container of this extension");
		} else if (configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.INITIALIZED) >= 0) {
			throw new IllegalStateException("config container must not be initialized");
		}

		getOrCreateCustomEntryData(configEntry).baseComment(baseComment);
	}

	@Override
	public void initEntry(ConfigEntry<?> configEntry) {
		CustomEntryData entryData = getOrCreateCustomEntryData(configEntry);
		entryData.commentProducer(middlewareContainer.process(entry -> entryData.baseComment()));
	}

	private CustomEntryData getOrCreateCustomEntryData(ConfigEntry<?> entry) {
		CustomEntryData customEntryData = entry.extensionsData().get(customEntryDataAccess);
		if (customEntryData == null) {
			customEntryData = new CustomEntryData();
			entry.extensionsData().set(customEntryDataAccess,  customEntryData);
		}
		return customEntryData;
	}

	@Override
	public @Nullable String getFullComment(ConfigEntry<?> configEntry) {
		CustomEntryData customEntryData = configEntry.extensionsData().get(customEntryDataAccess);
		if (customEntryData == null) {
			return null;
		}
		String comment = customEntryData.commentProducer().createComment(configEntry);
		return comment.isEmpty() ? null : comment;
	}


	@Data
	private static class CustomEntryData {
		private String baseComment = "";
		private CommentProducer commentProducer;
	}
}
