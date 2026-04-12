package de.siphalor.tweed5.defaultextensions.pather.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.serde.extension.api.*;
import de.siphalor.tweed5.serde.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.serde.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.serde.extension.api.extension.ReaderMiddlewareContext;
import de.siphalor.tweed5.serde.extension.api.extension.WriterMiddlewareContext;
import de.siphalor.tweed5.serde_api.api.TweedDataReader;
import de.siphalor.tweed5.serde_api.api.TweedDataVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataReader;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PatherExtensionImpl implements PatherExtension, ReadWriteRelatedExtension {
	private @Nullable PatchworkPartAccess<PathTracking> rwContextPathTrackingAccess;

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		rwContextPathTrackingAccess = context.registerReadWriteContextExtensionData(PathTracking.class);
		context.registerReaderMiddleware(createEntryReaderMiddleware());
		context.registerWriterMiddleware(createEntryWriterMiddleware());
	}

	@Override
	public String getPath(TweedReadContext context) {
		assert rwContextPathTrackingAccess != null;

		PathTracking pathTracking = context.extensionsData().get(rwContextPathTrackingAccess);
		if (pathTracking == null) {
			throw new IllegalStateException("Path tracking is not active!");
		}
		return pathTracking.currentPath();
	}

	@Override
	public String getPath(TweedWriteContext context) {
		assert rwContextPathTrackingAccess != null;

		PathTracking pathTracking = context.extensionsData().get(rwContextPathTrackingAccess);
		if (pathTracking == null) {
			throw new IllegalStateException("Path tracking is not active!");
		}
		return pathTracking.currentPath();
	}

	private Middleware<TweedEntryReader<?, ?>, ReaderMiddlewareContext> createEntryReaderMiddleware() {
		return new Middleware<TweedEntryReader<?, ?>, ReaderMiddlewareContext>() {
			@Override
			public String id() {
				return EXTENSION_ID;
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner, ReaderMiddlewareContext context) {
				assert rwContextPathTrackingAccess != null;

				//noinspection unchecked
				val castedInner = (TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;

				return (TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext readContext) -> {
					PathTracking pathTracking = readContext.extensionsData().get(rwContextPathTrackingAccess);
					if (pathTracking != null) {
						return castedInner.read(reader, entry, readContext);
					}

					pathTracking = PathTracking.create();
					readContext.extensionsData().set(rwContextPathTrackingAccess, pathTracking);
					return castedInner.read(new PathTrackingDataReader(reader, pathTracking), entry, readContext);
				};
			}
		};
	}

	private Middleware<TweedEntryWriter<?, ?>, WriterMiddlewareContext> createEntryWriterMiddleware() {
		return new Middleware<TweedEntryWriter<?, ?>, WriterMiddlewareContext>() {
			@Override
			public String id() {
				return EXTENSION_ID;
			}

			@Override
			public TweedEntryWriter<?, ?> process(TweedEntryWriter<?, ?> inner, WriterMiddlewareContext context) {
				assert rwContextPathTrackingAccess != null;

				//noinspection unchecked
				val castedInner = (TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>) inner;

				return (TweedDataVisitor writer, Object value, ConfigEntry<Object> entry, TweedWriteContext writeContext) -> {
					PathTracking pathTracking = writeContext.extensionsData().get(rwContextPathTrackingAccess);
					if (pathTracking != null) {
						castedInner.write(writer, value, entry, writeContext);
						return;
					}

					pathTracking = PathTracking.create();
					writeContext.extensionsData().set(rwContextPathTrackingAccess, pathTracking);
					try {
						castedInner.write(new PathTrackingDataVisitor(writer, pathTracking), value, entry, writeContext);
					} catch (TweedEntryWriteException e) {
						PathTracking exceptionPathTracking =
								e.context().extensionsData().get(rwContextPathTrackingAccess);
						if (exceptionPathTracking != null) {
							throw new TweedEntryWriteException(
									"Exception while writing entry at "
											+ exceptionPathTracking.currentPath()
											+ ": " + e.getMessage(),
									e
							);
						} else {
							throw e;
						}
					}
				};
			}
		};
	}
}
