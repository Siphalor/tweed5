package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.patchwork.api.PatchworkFactory;
import de.siphalor.tweed5.typeutils.api.type.ActualType;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isCompoundEntryForClassWith;
import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isSimpleEntryForClass;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unused")
@NullUnmarked
class CompoundPojoWeaverTest {

	@Test
	void weave() {
		PatchworkFactory.Builder weavingContextExtensionDataFactoryBuilder = PatchworkFactory.builder();

		CompoundPojoWeaver compoundWeaver = new CompoundPojoWeaver();
		compoundWeaver.setup(weavingContextExtensionDataFactoryBuilder::registerPart);

		PatchworkFactory weavingContextExtensionDataFactory = weavingContextExtensionDataFactoryBuilder.build();

		WeavingContext weavingContext = WeavingContext.builder()
				.weavingFunction(new WeavingContext.WeavingFn() {
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
								.weavingFunction(protoContext.parent()::weaveEntry)
								.build();
						return Objects.requireNonNullElseGet(
								compoundWeaver.weaveEntry(valueType, context),
								() -> new SimpleConfigEntryImpl<>(context.configContainer(), valueType.declaredType())
						);
					}
				})
				.extensionsData(weavingContextExtensionDataFactory.create())
				.annotations(Compound.class)
				.path(new String[0])
				.configContainer(mock(ConfigContainer.class))
				.build();

		ConfigEntry<Compound> resultEntry = compoundWeaver.weaveEntry(ActualType.ofClass(Compound.class), weavingContext);

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
}
