package de.siphalor.tweed5.attributesextension.impl.serde.filter;

import de.siphalor.tweed5.attributesextension.api.AttributesExtension;
import de.siphalor.tweed5.attributesextension.api.AttributesRelatedExtension;
import de.siphalor.tweed5.attributesextension.api.serde.filter.AttributesReadWriteFilterExtension;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.container.ConfigContainerSetupPhase;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.extension.TweedExtensionSetupContext;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteExtensionSetupContext;
import de.siphalor.tweed5.data.extension.api.extension.ReadWriteRelatedExtension;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import de.siphalor.tweed5.data.extension.impl.TweedEntryReaderWriterImpls;
import de.siphalor.tweed5.dataapi.api.DelegatingTweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataUnsupportedValueException;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkPartAccess;
import de.siphalor.tweed5.utils.api.UniqueSymbol;
import lombok.*;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class AttributesReadWriteFilterExtensionImpl
		implements AttributesReadWriteFilterExtension, AttributesRelatedExtension, ReadWriteRelatedExtension {
	private static final String ID = "attributes-serde-filter";
	private static final Set<String> MIDDLEWARES_MUST_COME_BEFORE = new HashSet<>(Arrays.asList(
			Middleware.DEFAULT_START,
			"validation"
	));
	private static final Set<String> MIDDLEWARES_MUST_COME_AFTER = Collections.emptySet();
	private static final UniqueSymbol TWEED_DATA_NOTHING_VALUE = new UniqueSymbol("nothing (skip value)");

	private final ConfigContainer<?> configContainer;
	private @Nullable AttributesExtension attributesExtension;
	private final Set<String> filterableAttributes = new HashSet<>();
	private final PatchworkPartAccess<EntryCustomData> entryDataAccess;
	private @Nullable PatchworkPartAccess<ReadWriteContextCustomData> readWriteContextDataAccess;

	public AttributesReadWriteFilterExtensionImpl(ConfigContainer<?> configContainer, TweedExtensionSetupContext setupContext) {
		this.configContainer = configContainer;

		entryDataAccess = setupContext.registerEntryExtensionData(EntryCustomData.class);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public void setupReadWriteExtension(ReadWriteExtensionSetupContext context) {
		readWriteContextDataAccess = context.registerReadWriteContextExtensionData(ReadWriteContextCustomData.class);
		context.registerReaderMiddleware(new ReaderMiddleware());
		context.registerWriterMiddleware(new WriterMiddleware());
	}

	@Override
	public void markAttributeForFiltering(String key) {
		requireUninitialized();
		filterableAttributes.add(key);
	}

	@Override
	public void afterAttributesInitialized() {
		attributesExtension = configContainer.extension(AttributesExtension.class)
				.orElseThrow(() -> new IllegalStateException(
						"You must register a " + AttributesExtension.class.getSimpleName()
								+ " before initializing the " + AttributesReadWriteFilterExtension.class.getSimpleName()
				));

		configContainer.rootEntry().visitInOrder(new ConfigEntryVisitor() {
			private final Deque<Map<String, Set<String>>> attributesCollectors = new ArrayDeque<>();

			@Override
			public void visitEntry(ConfigEntry<?> entry) {
				Map<String, Set<String>> currentAttributesCollector = attributesCollectors.peekFirst();
				if (currentAttributesCollector != null) {
					for (String filterableAttribute : filterableAttributes) {
						List<String> values = attributesExtension.getAttributeValues(entry, filterableAttribute);
						if (!values.isEmpty()) {
							currentAttributesCollector.computeIfAbsent(filterableAttribute, k -> new HashSet<>()).addAll(values);
						}
					}
				}
			}

			@Override
			public boolean enterCollectionEntry(ConfigEntry<?> entry) {
				attributesCollectors.push(new HashMap<>());
				visitEntry(entry);
				return true;
			}

			@Override
			public void leaveCollectionEntry(ConfigEntry<?> entry) {
				leaveContainerEntry(entry);
			}

			@Override
			public boolean enterCompoundEntry(ConfigEntry<?> entry) {
				attributesCollectors.push(new HashMap<>());
				visitEntry(entry);
				return true;
			}

			@Override
			public void leaveCompoundEntry(ConfigEntry<?> entry) {
				leaveContainerEntry(entry);
			}

			private void leaveContainerEntry(ConfigEntry<?> entry) {
				Map<String, Set<String>> entryAttributesCollector = attributesCollectors.pop();
				entry.extensionsData().set(entryDataAccess, new EntryCustomData(entryAttributesCollector));

				Map<String, Set<String>> outerAttributesCollector = attributesCollectors.peekFirst();
				if (outerAttributesCollector != null) {
					entryAttributesCollector.forEach((key, value) ->
							outerAttributesCollector.computeIfAbsent(key, k -> new HashSet<>()).addAll(value)
					);
				}
			}
		});
	}

	@Override
	public void addFilter(Patchwork contextExtensionsData, String key, String value) {
		requireInitialized();

		var contextCustomData = getOrCreateReadWriteContextCustomData(contextExtensionsData);
		addFilterToRWContextData(key, value, contextCustomData);
	}

	private ReadWriteContextCustomData getOrCreateReadWriteContextCustomData(Patchwork patchwork) {
		assert readWriteContextDataAccess != null;

		ReadWriteContextCustomData readWriteContextCustomData = patchwork.get(readWriteContextDataAccess);
		if (readWriteContextCustomData == null) {
			readWriteContextCustomData = new ReadWriteContextCustomData();
			patchwork.set(readWriteContextDataAccess, readWriteContextCustomData);
		}

		return readWriteContextCustomData;
	}

	private void addFilterToRWContextData(String key, String value, ReadWriteContextCustomData contextCustomData) {
		if (filterableAttributes.contains(key)) {
			contextCustomData.attributeFilters().computeIfAbsent(key, k -> new HashSet<>()).add(value);
		} else {
			throw new IllegalArgumentException("The attribute " + key + " has not been marked for filtering");
		}
	}

	private void requireUninitialized() {
		if (configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.INITIALIZED) >= 0) {
			throw new IllegalStateException(
					"Attribute optimization is only editable until the config has been initialized"
			);
		}
	}

	private void requireInitialized() {
		if (configContainer.setupPhase().compareTo(ConfigContainerSetupPhase.INITIALIZED) < 0) {
			throw new IllegalStateException("Config container must already be initialized");
		}
	}

	private class ReaderMiddleware implements Middleware<TweedEntryReader<?, ?>> {
		@Override
		public String id() {
			return ID;
		}

		@Override
		public Set<String> mustComeBefore() {
			return MIDDLEWARES_MUST_COME_BEFORE;
		}

		@Override
		public Set<String> mustComeAfter() {
			return MIDDLEWARES_MUST_COME_AFTER;
		}

		@Override
		public TweedEntryReader<?, ?> process(TweedEntryReader<?, ?> inner) {
			assert readWriteContextDataAccess != null;
			//noinspection unchecked
			TweedEntryReader<Object, ConfigEntry<Object>> innerCasted
					= (TweedEntryReader<Object, @NonNull ConfigEntry<Object>>) inner;

			return new TweedEntryReader<@Nullable Object, ConfigEntry<Object>>() {
				@Override
				public @Nullable Object read(
						TweedDataReader reader,
						ConfigEntry<Object> entry,
						TweedReadContext context
				) throws TweedEntryReadException {
					ReadWriteContextCustomData contextData = context.extensionsData().get(readWriteContextDataAccess);
					if (contextData == null || doFiltersMatch(entry, contextData)) {
						return innerCasted.read(reader, entry, context);
					}
					TweedEntryReaderWriterImpls.NOOP_READER_WRITER.read(reader, entry, context);
					// TODO: this should result in a noop instead of a null value
					return null;
				}
			};
		}
	}

	private class WriterMiddleware implements Middleware<TweedEntryWriter<?, ?>> {
		@Override
		public String id() {
			return ID;
		}

		@Override
		public Set<String> mustComeBefore() {
			return MIDDLEWARES_MUST_COME_BEFORE;
		}

		@Override
		public Set<String> mustComeAfter() {
			return MIDDLEWARES_MUST_COME_AFTER;
		}

		@Override
		public TweedEntryWriter<?, ?> process(TweedEntryWriter<?, ?> inner) {
			assert readWriteContextDataAccess != null;
			//noinspection unchecked
			TweedEntryWriter<Object, ConfigEntry<Object>> innerCasted
					= (TweedEntryWriter<Object, @NonNull ConfigEntry<Object>>) inner;

			return (TweedEntryWriter<@Nullable Object, @NonNull ConfigEntry<@Nullable Object>>)
					(writer, value, entry, context) -> {
						ReadWriteContextCustomData contextData = context.extensionsData()
								.get(readWriteContextDataAccess);
						if (contextData == null || contextData.attributeFilters().isEmpty()) {
							innerCasted.write(writer, value, entry, context);
							return;
						}

						if (!contextData.writerInstalled()) {
							writer = new MapEntryKeyDeferringWriter(writer);
							contextData.writerInstalled(true);
						}

						if (doFiltersMatch(entry, contextData)) {
							innerCasted.write(writer, value, entry, context);
						} else {
							try {
								writer.visitValue(TWEED_DATA_NOTHING_VALUE);
							} catch (TweedDataUnsupportedValueException ignored) {}
						}
					};
		}
	}

	private boolean doFiltersMatch(ConfigEntry<?> entry, ReadWriteContextCustomData contextData) {
		assert attributesExtension != null;

		EntryCustomData entryCustomData = entry.extensionsData().get(entryDataAccess);
		if (entryCustomData == null) {
			for (Map.Entry<String, Set<String>> attributeFilter : contextData.attributeFilters().entrySet()) {
				List<String> values = attributesExtension.getAttributeValues(entry, attributeFilter.getKey());
				//noinspection SlowListContainsAll
				if (!values.containsAll(attributeFilter.getValue())) {
					return false;
				}
			}
			return true;
		}
		for (Map.Entry<String, Set<String>> attributeFilter : contextData.attributeFilters().entrySet()) {
			Set<String> values = entryCustomData.optimizedAttributes()
					.getOrDefault(attributeFilter.getKey(), Collections.emptySet());

			if (!values.containsAll(attributeFilter.getValue())) {
				return false;
			}
		}
		return true;
	}

	private static class MapEntryKeyDeferringWriter extends DelegatingTweedDataVisitor {
		private final Deque<Boolean> mapContext = new ArrayDeque<>();
		private final Deque<TweedDataDecoration> preDecorationQueue = new ArrayDeque<>();
		private final Deque<TweedDataDecoration> postDecorationQueue = new ArrayDeque<>();
		private @Nullable String mapEntryKey;

		protected MapEntryKeyDeferringWriter(TweedDataVisitor delegate) {
			super(delegate);
			mapContext.push(false);
		}

		@Override
		public void visitMapStart() {
			beforeValueWrite();
			mapContext.push(true);
			delegate.visitMapStart();
		}

		@Override
		public void visitMapEntryKey(String key) {
			if (mapEntryKey != null) {
				throw new IllegalStateException("The map entry key has already been visited");
			} else {
				mapEntryKey = key;
			}
		}

		@Override
		public void visitMapEnd() {
			if (mapEntryKey != null) {
				throw new IllegalArgumentException("Reached end of map while waiting for value for key " + mapEntryKey);
			}

			TweedDataDecoration decoration;
			while ((decoration = preDecorationQueue.pollFirst()) != null) {
				super.visitDecoration(decoration);
			}

			super.visitMapEnd();
			mapContext.pop();
		}

		@Override
		public void visitListStart() {
			beforeValueWrite();
			mapContext.push(false);
			delegate.visitListStart();
		}

		@Override
		public void visitListEnd() {
			super.visitListEnd();
			mapContext.pop();
		}

		@Override
		public void visitValue(@Nullable Object value) throws TweedDataUnsupportedValueException {
			if (value == TWEED_DATA_NOTHING_VALUE) {
				preDecorationQueue.clear();
				postDecorationQueue.clear();
				mapEntryKey = null;
				return;
			}
			super.visitValue(value);
		}

		@Override
		public void visitDecoration(TweedDataDecoration decoration) {
			if (Boolean.TRUE.equals(mapContext.peekFirst())) {
				if (mapEntryKey == null) {
					preDecorationQueue.addLast(decoration);
				} else {
					postDecorationQueue.addLast(decoration);
				}
			} else {
				super.visitDecoration(decoration);
			}
		}

		@Override
		protected void beforeValueWrite() {
			super.beforeValueWrite();

			if (mapEntryKey != null) {
				TweedDataDecoration decoration;
				while ((decoration = preDecorationQueue.pollFirst()) != null) {
					super.visitDecoration(decoration);
				}

				super.visitMapEntryKey(mapEntryKey);
				mapEntryKey = null;

				while ((decoration = postDecorationQueue.pollFirst()) != null) {
					super.visitDecoration(decoration);
				}
			}
		}
	}

	@Getter
	@RequiredArgsConstructor
	private static class EntryCustomData {
		private final Map<String, Set<String>> optimizedAttributes;
	}

	@Getter
	@Setter
	private static class ReadWriteContextCustomData {
		private final Map<String, Set<String>> attributeFilters = new HashMap<>();
		private boolean writerInstalled;
	}
}
