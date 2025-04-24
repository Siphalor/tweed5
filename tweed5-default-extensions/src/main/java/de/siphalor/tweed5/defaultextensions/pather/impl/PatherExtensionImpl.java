package de.siphalor.tweed5.defaultextensions.pather.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.*;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTracking;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataReader;
import de.siphalor.tweed5.defaultextensions.pather.api.PathTrackingDataVisitor;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import lombok.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@AutoService(PatherExtension.class)
@NullUnmarked
public class PatherExtensionImpl implements PatherExtension, TweedExtension, ReadWriteRelatedExtension {
	private static final String PATHER_ID = "pather";

	private RegisteredExtensionData<ReadWriteContextExtensionsData, PathTracking> rwContextPathTrackingData;
	private Middleware<TweedEntryReader<?, ?>> entryReaderMiddleware;
	private Middleware<TweedEntryWriter<?, ?>> entryWriterMiddleware;

	@Override
	public String getId() {
		return PATHER_ID;
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		rwContextPathTrackingData = context.registerReadWriteContextExtensionData(PathTracking.class);

		entryReaderMiddleware = createEntryReaderMiddleware();
		entryWriterMiddleware = createEntryWriterMiddleware();
	}

	private @NonNull Middleware<TweedEntryReader<?, ?>> createEntryReaderMiddleware() {
		return new Middleware<TweedEntryReader<?, ?>>() {
			@Override
			public String id() {
				return PATHER_ID;
			}

			@Override
			public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
				//noinspection unchecked
				val castedInner = (TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;

				return (TweedDataReader reader, ConfigEntry<Object> entry, TweedReadContext context) -> {
					if (context.extensionsData().isPatchworkPartSet(PathTracking.class)) {
						return castedInner.read(reader, entry, context);
					}

					PathTracking pathTracking = new PathTracking();
					rwContextPathTrackingData.set(context.extensionsData(), pathTracking);
					return castedInner.read(new PathTrackingDataReader(reader, pathTracking), entry, context);
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
				//noinspection unchecked
				val castedInner = (TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>) inner;

				return (TweedDataVisitor writer, Object value, ConfigEntry<Object> entry, TweedWriteContext context) -> {
					if (context.extensionsData().isPatchworkPartSet(PathTracking.class)) {
						castedInner.write(writer, value, entry, context);
						return;
					}

					PathTracking pathTracking = new PathTracking();
					rwContextPathTrackingData.set(context.extensionsData(), pathTracking);
					castedInner.write(new PathTrackingDataVisitor(writer, pathTracking), value, entry, context);
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
