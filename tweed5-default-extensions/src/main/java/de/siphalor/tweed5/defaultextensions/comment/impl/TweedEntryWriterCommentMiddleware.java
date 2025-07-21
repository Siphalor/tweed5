package de.siphalor.tweed5.defaultextensions.comment.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.dataapi.api.DelegatingTweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

@RequiredArgsConstructor
class TweedEntryWriterCommentMiddleware implements Middleware<TweedEntryWriter<?, ?>> {
	private final CommentExtensionImpl commentExtension;

	@Override
	public String id() {
		return "comment-writer";
	}

	@Override
	public TweedEntryWriter<?, ?> process(TweedEntryWriter<?, ?> inner) {
		PatchworkPartAccess<Boolean> writerInstalledAccess = commentExtension.writerInstalledReadWriteContextAccess();
		assert writerInstalledAccess != null;

		//noinspection unchecked
		TweedEntryWriter<Object, ConfigEntry<Object>> innerCasted = (TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>) inner;
		return (TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>) (writer, value, entry, context) -> {
			if (!Boolean.TRUE.equals(context.extensionsData().get(writerInstalledAccess))) {
				context.extensionsData().set(writerInstalledAccess, Boolean.TRUE);

				writer = new MapEntryKeyDeferringWriter(writer);
			}

			String comment = commentExtension.getFullComment(entry);
			if (comment != null) {
				writer.visitDecoration(new PiercingCommentDecoration(() -> comment));
			}

			innerCasted.write(writer, value, entry, context);
		};
	}

	private static class MapEntryKeyDeferringWriter extends DelegatingTweedDataVisitor {
		private final Deque<TweedDataDecoration> decorationQueue = new ArrayDeque<>();
		private @Nullable String mapEntryKey;

		protected MapEntryKeyDeferringWriter(TweedDataVisitor delegate) {
			super(delegate);
		}

		@Override
		public void visitMapEntryKey(String key) {
			if (mapEntryKey != null) {
				throw new IllegalStateException("Map entry key already visited");
			} else {
				mapEntryKey = key;
			}
		}

		@Override
		public void visitDecoration(TweedDataDecoration decoration) {
			if (decoration instanceof PiercingCommentDecoration) {
				super.visitDecoration(((PiercingCommentDecoration) decoration).commentDecoration());
				return;
			}
			if (mapEntryKey != null) {
				decorationQueue.addLast(decoration);
			} else {
				super.visitDecoration(decoration);
			}
		}

		@Override
		protected void beforeValueWrite() {
			if (mapEntryKey != null) {
				super.visitMapEntryKey(mapEntryKey);
				mapEntryKey = null;
				TweedDataDecoration decoration;
				while ((decoration = decorationQueue.pollFirst()) != null) {
					super.visitDecoration(decoration);
				}
			}
			super.beforeValueWrite();
		}
	}

	@Value
	private static class PiercingCommentDecoration implements TweedDataDecoration {
		TweedDataCommentDecoration commentDecoration;
	}
}
