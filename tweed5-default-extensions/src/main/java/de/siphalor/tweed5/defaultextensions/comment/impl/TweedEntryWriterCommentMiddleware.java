package de.siphalor.tweed5.defaultextensions.comment.impl;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class TweedEntryWriterCommentMiddleware implements Middleware<TweedEntryWriter<?, ?>> {
	public static final TweedEntryWriterCommentMiddleware INSTANCE = new TweedEntryWriterCommentMiddleware();

	@Override
	public String id() {
		return "comment-writer";
	}

	@Override
	public TweedEntryWriter<?, ?> process(TweedEntryWriter<?, ?> inner) {
		//noinspection unchecked
		TweedEntryWriter<Object, ConfigEntry<Object>> innerCasted = (TweedEntryWriter<Object, ConfigEntry<Object>>) inner;
		return (TweedEntryWriter<Object, ConfigEntry<Object>>) (writer, value, entry, context) -> {
			if (writer instanceof CompoundDataVisitor) {
				// Comment is already written in front of the key by the CompoundDataWriter,
				// so we don't have to write it here.
				// We also want to unwrap the original writer,
				// so that the special comment writing is limited to compounds.
				writer = ((CompoundDataVisitor) writer).delegate;
			} else {
				String comment = getEntryComment(entry);
				if (comment != null) {
					writer.visitComment(comment);
				}
			}

			if (entry instanceof CompoundConfigEntry) {
				innerCasted.write(
						new CompoundDataVisitor(writer, ((CompoundConfigEntry<?>) entry)),
						value,
						entry,
						context
				);
			} else {
				innerCasted.write(writer, value, entry, context);
			}
		};
	}

	@RequiredArgsConstructor
	private static class CompoundDataVisitor implements TweedDataVisitor {
		private final TweedDataVisitor delegate;
		private final CompoundConfigEntry<?> compoundConfigEntry;

		@Override
		public void visitNull() {
			delegate.visitNull();
		}

		@Override
		public void visitBoolean(boolean value) {
			delegate.visitBoolean(value);
		}

		@Override
		public void visitByte(byte value) {
			delegate.visitByte(value);
		}

		@Override
		public void visitShort(short value) {
			delegate.visitShort(value);
		}

		@Override
		public void visitInt(int value) {
			delegate.visitInt(value);
		}

		@Override
		public void visitLong(long value) {
			delegate.visitLong(value);
		}

		@Override
		public void visitFloat(float value) {
			delegate.visitFloat(value);
		}

		@Override
		public void visitDouble(double value) {
			delegate.visitDouble(value);
		}

		@Override
		public void visitString(@NotNull String value) {
			delegate.visitString(value);
		}

		@Override
		public void visitListStart() {
			delegate.visitListStart();
		}

		@Override
		public void visitListEnd() {
			delegate.visitListEnd();
		}

		@Override
		public void visitMapStart() {
			delegate.visitMapStart();
		}

		@Override
		public void visitMapEntryKey(String key) {
			ConfigEntry<?> subEntry = compoundConfigEntry.subEntries().get(key);
			String subEntryComment = getEntryComment(subEntry);
			if (subEntryComment != null) {
				delegate.visitComment(subEntryComment);
			}
			delegate.visitMapEntryKey(key);
		}

		@Override
		public void visitMapEnd() {
			delegate.visitMapEnd();
		}

		@Override
		public void visitComment(String comment) {
			delegate.visitComment(comment);
		}
	}

	private static @Nullable String getEntryComment(ConfigEntry<?> entry) {
		if (!entry.extensionsData().isPatchworkPartSet(InternalCommentEntryData.class)) {
			return null;
		}
		String comment = ((InternalCommentEntryData) entry.extensionsData()).commentProducer().createComment(entry).trim();
		return comment.isEmpty() ? null : comment;
	}
}
