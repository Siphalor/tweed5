package de.siphalor.tweed5.defaultextensions.patch.impl;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.serde.extension.api.TweedEntryReader;
import de.siphalor.tweed5.serde.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.serde.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.serde.extension.api.extension.ReaderMiddlewareContext;
import de.siphalor.tweed5.serde.extension.api.read.result.TweedReadResult;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchExtension;
import de.siphalor.tweed5.defaultextensions.patch.api.PatchInfo;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import lombok.Data;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PatchExtensionImpl implements PatchExtension, ReadWriteRelatedExtension {
	private @Nullable PatchworkPartAccess<ReadWriteContextCustomData> readWriteContextDataAccess;

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		readWriteContextDataAccess = context.registerReadWriteContextExtensionData(ReadWriteContextCustomData.class);
		context.registerReaderMiddleware(new ReaderMiddleware());
	}

	@Override
	public PatchInfo collectPatchInfo(Patchwork readWriteContextExtensionsData) {
		ReadWriteContextCustomData customData = getOrCreateCustomData(readWriteContextExtensionsData);
		PatchInfoImpl patchInfo = customData.patchInfo();
		if (patchInfo == null) {
			patchInfo = new PatchInfoImpl();
			customData.patchInfo(patchInfo);
		}
		return patchInfo;
	}

	private ReadWriteContextCustomData getOrCreateCustomData(Patchwork readWriteContextExtensionsData) {
		assert readWriteContextDataAccess != null;
		ReadWriteContextCustomData customData = readWriteContextExtensionsData.get(readWriteContextDataAccess);
		if (customData == null) {
			customData = new ReadWriteContextCustomData();
			readWriteContextExtensionsData.set(readWriteContextDataAccess, customData);
		}
		return customData;
	}

	@Override
	public <T extends @Nullable Object> T patch(ConfigEntry<T> entry, T targetValue, T patchValue, PatchInfo patchInfo) {
		if (!patchInfo.containsEntry(entry)) {
			return targetValue;
		} else if (patchValue == null) {
			return null;
		}

		if (entry instanceof CompoundConfigEntry) {
			CompoundConfigEntry<T> compoundEntry = (CompoundConfigEntry<T>) entry;

			T targetCompoundValue;
			if (targetValue != null) {
				targetCompoundValue = targetValue;
			} else {
				targetCompoundValue = compoundEntry.instantiateValue();
			}
			compoundEntry.subEntries().forEach((key, subEntry) -> {
				if (!patchInfo.containsEntry(subEntry)) {
					return;
				}
				compoundEntry.set(
						targetCompoundValue, key, patch(
								(ConfigEntry<Object>) subEntry,
								compoundEntry.get(targetCompoundValue, key),
								compoundEntry.get(patchValue, key),
								patchInfo
						)
				);
			});
			return targetCompoundValue;
		} else {
			return patchValue;
		}
	}

	private class ReaderMiddleware implements Middleware<TweedEntryReader<?, ?>, ReaderMiddlewareContext> {
		@Override
		public String id() {
			return "patch-info-collector";
		}

		@Override
		public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner, ReaderMiddlewareContext context) {
			assert readWriteContextDataAccess != null;

			//noinspection unchecked
			TweedEntryReader<Object, ConfigEntry<Object>> innerCasted =
					(TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;
			return (TweedEntryReader<Object, ConfigEntry<Object>>) (reader, entry, readContext) -> {
				TweedReadResult<Object> readResult = innerCasted.read(reader, entry, readContext);
				ReadWriteContextCustomData customData = readContext.extensionsData().get(readWriteContextDataAccess);
				if (customData != null && customData.patchInfo() != null) {
					customData.patchInfo().addEntry(entry);
				}
				return readResult;
			};
		}
	}

	@Data
	private static class ReadWriteContextCustomData {
		private @Nullable PatchInfoImpl patchInfo;
	}
}
