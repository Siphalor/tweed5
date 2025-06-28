package de.siphalor.tweed5.defaultextensions.pather.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataReader;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.val;
import lombok.var;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@AutoService(PatherExtension.class)
public class PatherExtensionImpl implements PatherExtension, TweedExtension, ReadWriteRelatedExtension {
	private static final String PATHER_ID = "pather";

	private final Middleware<TweedEntryReader<?, ?>> entryReaderMiddleware = createEntryReaderMiddleware();
	private final Middleware<TweedEntryWriter<?, ?>> entryWriterMiddleware = createEntryWriterMiddleware();

	private @Nullable PatchworkPartAccess<PathTracking> rwContextPathTrackingAccess;

	@Override
	public String getId() {
		return PATHER_ID;
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		rwContextPathTrackingAccess = context.registerReadWriteContextExtensionData(PathTracking.class);
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

	private Middleware<TweedEntryReader<?, ?>> createEntryReaderMiddleware() {
		return new Middleware<TweedEntryReader<?, ?>>() {
			@Override
			public String id() {
				return PATHER_ID;
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
				assert rwContextPathTrackingAccess != null;

				//noinspection unchecked
				val castedInner = (TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;

				return (TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) -> {
					PathTracking pathTracking = context.extensionsData().get(rwContextPathTrackingAccess);
					if (pathTracking != null) {
						return castedInner.read(reader, entry, context);
					}

					pathTracking = new PathTracking();
					context.extensionsData().set(rwContextPathTrackingAccess, pathTracking);
					try {
						return castedInner.read(new PathTrackingDataReader(reader, pathTracking), entry, context);
					} catch (TweedEntryReadException e) {
						var exceptionPathTracking = e.context().extensionsData().get(rwContextPathTrackingAccess);
						if (exceptionPathTracking != null) {
							throw new TweedEntryReadException(
									"Exception while reading entry at "
											+ String.join("/", exceptionPathTracking.currentPath())
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

	private Middleware<TweedEntryWriter<?, ?>> createEntryWriterMiddleware() {
		return new Middleware<TweedEntryWriter<?, ?>>() {
			@Override
			public String id() {
				return PATHER_ID;
			}

			@Override
			public TweedEntryWriter<?, ?> process(TweedEntryWriter<?, ?> inner) {
				assert rwContextPathTrackingAccess != null;

				//noinspection unchecked
				val castedInner = (TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>) inner;

				return (TweedDataVisitor writer, Object value, ConfigEntry<Object> entry, TweedWriteContext context) -> {
					PathTracking pathTracking = context.extensionsData().get(rwContextPathTrackingAccess);
					if (pathTracking != null) {
						castedInner.write(writer, value, entry, context);
						return;
					}

					pathTracking = new PathTracking();
					context.extensionsData().set(rwContextPathTrackingAccess, pathTracking);
					try {
						castedInner.write(new PathTrackingDataVisitor(writer, pathTracking), value, entry, context);
					} catch (TweedEntryWriteException e) {
						var exceptionPathTracking = e.context().extensionsData().get(rwContextPathTrackingAccess);
						if (exceptionPathTracking != null) {
							throw new TweedEntryWriteException(
									"Exception while writing entry at "
											+ String.join("/", exceptionPathTracking.currentPath())
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

	@Override
	public @Nullable Middleware<TweedEntryReader<?, ?>> entryReaderMiddleware() {
		return entryReaderMiddleware;
	}

	@Override
	public @Nullable Middleware<TweedEntryWriter<?, ?>> entryWriterMiddleware() {
		return entryWriterMiddleware;
	}
}
