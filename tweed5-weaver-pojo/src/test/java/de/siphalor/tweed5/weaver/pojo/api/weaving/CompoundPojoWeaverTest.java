package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.namingformat.api.NamingFormat;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.entry.WeavableCompoundConfigEntry;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.compound.CompoundWeavingConfig;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isCompoundEntryForClassWith;
import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isSimpleEntryForClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SuppressWarnings("unused")
class CompoundPojoWeaverTest {

	@Test
	void weave() {
		CompoundPojoWeaver compoundWeaver = new CompoundPojoWeaver();
		compoundWeaver.setup(new TweedPojoWeaver.SetupContext() {
			@Override
			public <E> RegisteredExtensionData<WeavingContext.ExtensionsData, E> registerWeavingContextExtensionData(Class<E> dataClass) {
				return (patchwork, extension) -> ((ExtensionsDataMock) patchwork).weavingConfig = (CompoundWeavingConfig) extension;
			}
		});

		WeavingContext weavingContext = WeavingContext.builder()
				.extensionsData(new ExtensionsDataMock(null))
				.weavingFunction(new TweedPojoWeavingFunction.NonNull() {
					@Override
					public @NotNull <T> ConfigEntry<T> weaveEntry(Class<T> valueClass, WeavingContext context) {
						ConfigEntry<T> entry = compoundWeaver.weaveEntry(valueClass, context);
						if (entry != null) {
							return entry;
						} else {
							//noinspection unchecked
							ConfigEntry<T> configEntry = mock((Class<SimpleConfigEntry<T>>) (Class<?>) SimpleConfigEntry.class);
							when(configEntry.valueClass()).thenReturn(valueClass);
							return configEntry;
						}
					}
				})
				.build();

		ConfigEntry<Compound> resultEntry = compoundWeaver.weaveEntry(Compound.class, weavingContext);

		assertThat(resultEntry).satisfies(isCompoundEntryForClassWith(Compound.class, compoundEntry -> assertThat(compoundEntry.subEntries())
				.hasEntrySatisfying("an-integer", isSimpleEntryForClass(int.class))
				.hasEntrySatisfying("inner-compound", isCompoundEntryForClassWith(InnerCompound1.class, innerCompound1 -> assertThat(innerCompound1.subEntries())
						.hasEntrySatisfying("a-parameter", isSimpleEntryForClass(String.class))
						.hasEntrySatisfying("inner-compound", isCompoundEntryForClassWith(InnerCompound2.class, innerCompound2 -> assertThat(innerCompound2.subEntries())
								.hasEntrySatisfying("inner_value", isSimpleEntryForClass(InnerValue.class))
								.hasSize(1)
						))
						.hasSize(2)
				))
				.hasSize(2)
		));
	}

	@CompoundWeaving(namingFormat = "kebab_case")
	public static class Compound {
		public int anInteger;
		public InnerCompound1 innerCompound;
	}

	@CompoundWeaving
	public static class InnerCompound1 {
		public String aParameter;
		public InnerCompound2 innerCompound;
	}

	@CompoundWeaving(namingFormat = "snake_case")
	public static class InnerCompound2 {
		public InnerValue innerValue;
	}

	public static class InnerValue {
		public Integer value;
	}

	@AllArgsConstructor
	private static class ExtensionsDataMock implements WeavingContext.ExtensionsData, CompoundWeavingConfig {
		private CompoundWeavingConfig weavingConfig;

		@Override
		public boolean isPatchworkPartDefined(Class<?> patchworkInterface) {
			return patchworkInterface == CompoundWeavingConfig.class;
		}

		@Override
		public boolean isPatchworkPartSet(Class<?> patchworkInterface) {
			return weavingConfig != null;
		}

		@Override
		public WeavingContext.ExtensionsData copy() {
			return new ExtensionsDataMock(weavingConfig);
		}

		@Override
		public NamingFormat compoundSourceNamingFormat() {
			return weavingConfig.compoundSourceNamingFormat();
		}

		@Override
		public NamingFormat compoundTargetNamingFormat() {
			return weavingConfig.compoundTargetNamingFormat();
		}

		@Override
		public @Nullable Class<? extends WeavableCompoundConfigEntry> compoundEntryClass() {
			return weavingConfig.compoundEntryClass();
		}
	}
}