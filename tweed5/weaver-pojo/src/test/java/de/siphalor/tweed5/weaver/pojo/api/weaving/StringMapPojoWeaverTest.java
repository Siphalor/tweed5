package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.annotation.StringMapWeaving;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isSimpleEntryForClass;
import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isStringMapEntryForClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unused")
class StringMapPojoWeaverTest {

	private StringMapPojoWeaver stringMapWeaver;
	private PatchworkFactory weavingContextExtensionDataFactory;

	@BeforeEach
	void setup() {
		PatchworkFactory.Builder builder = PatchworkFactory.builder();
		stringMapWeaver = new StringMapPojoWeaver();
		stringMapWeaver.setup(builder::registerPart);
		weavingContextExtensionDataFactory = builder.build();
	}

	private WeavingContext.WeavingFn createWeavingFn(AtomicReference<WeavingContext.@Nullable WeavingFn> ref) {
		WeavingContext.WeavingFn fn = new WeavingContext.WeavingFn() {
			@Override
			public <T> ConfigEntry<T> weaveEntry(
					ActualType<T> valueType,
					Patchwork extensionsData,
					ProtoWeavingContext protoContext
			) {
				WeavingContext context = WeavingContext.builder()
						.configContainer(protoContext.configContainer())
						.annotations(valueType)
						.extensionsData(extensionsData.copy())
						.path(protoContext.path())
						.weavingFunction(Objects.requireNonNull(ref.get()))
						.build();
				return Objects.requireNonNullElseGet(
						stringMapWeaver.weaveEntry(valueType, context),
						() -> new SimpleConfigEntryImpl<>(context.configContainer(), valueType.declaredType())
				);
			}

			@Override
			public <T> ConfigEntry<T> weavePseudoEntry(
					WeavingContext parentContext,
					String pseudoEntryName,
					Patchwork extensionsData
			) {
				assert false;
				return null;
			}
		};
		ref.set(fn);
		return fn;
	}

	@Test
	void weaveStringToIntegerMap() {
		AtomicReference<WeavingContext.@Nullable WeavingFn> weavingFnRef = new AtomicReference<>();
		WeavingContext.WeavingFn weavingFn = createWeavingFn(weavingFnRef);

		WeavingContext weavingContext = WeavingContext.builder()
				.weavingFunction(weavingFn)
				.extensionsData(weavingContextExtensionDataFactory.create())
				.annotations(StringToIntegerMap.class)
				.path(new String[0])
				.configContainer(mock(ConfigContainer.class))
				.build();

		ConfigEntry<StringToIntegerMap> resultEntry = stringMapWeaver.weaveEntry(
				ActualType.ofClass(StringToIntegerMap.class),
				weavingContext
		);

		assertThat(resultEntry).satisfies(isStringMapEntryForClass(StringToIntegerMap.class, mapEntry ->
				assertThat(mapEntry.subEntries())
						.hasEntrySatisfying(":value", isSimpleEntryForClass(Integer.class))
						.hasSize(1)
		));
	}

	@Test
	void weaveStringToStringMap() {
		AtomicReference<WeavingContext.@Nullable WeavingFn> weavingFnRef = new AtomicReference<>();
		WeavingContext.WeavingFn weavingFn = createWeavingFn(weavingFnRef);

		WeavingContext weavingContext = WeavingContext.builder()
				.weavingFunction(weavingFn)
				.extensionsData(weavingContextExtensionDataFactory.create())
				.annotations(StringToStringLinkedMap.class)
				.path(new String[0])
				.configContainer(mock(ConfigContainer.class))
				.build();

		ConfigEntry<StringToStringLinkedMap> resultEntry = stringMapWeaver.weaveEntry(
				ActualType.ofClass(StringToStringLinkedMap.class),
				weavingContext
		);

		assertThat(resultEntry).satisfies(isStringMapEntryForClass(StringToStringLinkedMap.class, mapEntry ->
				assertThat(mapEntry.subEntries())
						.hasEntrySatisfying(":value", isSimpleEntryForClass(String.class))
						.hasSize(1)
		));
	}

	@Test
	void returnsNullWithoutAnnotation() {
		WeavingContext weavingContext = WeavingContext.builder()
				.weavingFunction(mock(WeavingContext.WeavingFn.class))
				.extensionsData(weavingContextExtensionDataFactory.create())
				.annotations(UnannotatedStringMap.class)
				.path(new String[0])
				.configContainer(mock(ConfigContainer.class))
				.build();

		ConfigEntry<UnannotatedStringMap> resultEntry = stringMapWeaver.weaveEntry(
				ActualType.ofClass(UnannotatedStringMap.class),
				weavingContext
		);

		assertThat(resultEntry).isNull();
	}

	@Test
	void returnsNullForNonStringKey() {
		WeavingContext weavingContext = WeavingContext.builder()
				.weavingFunction(mock(WeavingContext.WeavingFn.class))
				.extensionsData(weavingContextExtensionDataFactory.create())
				.annotations(IntegerKeyMap.class)
				.path(new String[0])
				.configContainer(mock(ConfigContainer.class))
				.build();

		ConfigEntry<IntegerKeyMap> resultEntry = stringMapWeaver.weaveEntry(
				ActualType.ofClass(IntegerKeyMap.class),
				weavingContext
		);

		assertThat(resultEntry).isNull();
	}

	@StringMapWeaving
	public static class StringToIntegerMap extends HashMap<String, Integer> {}

	@StringMapWeaving
	public static class StringToStringLinkedMap extends LinkedHashMap<String, String> {}

	public static class UnannotatedStringMap extends HashMap<String, Integer> {}

	@StringMapWeaving
	public static class IntegerKeyMap extends HashMap<Integer, String> {}
}
