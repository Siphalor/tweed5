package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import de.siphalor.tweed5.weaver.pojo.api.weaving.CollectionPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.CompoundPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TrivialPojoWeaver;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.*;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class TweedPojoWeaverBootstrapperTest {
	@Test
	void defaultWeaving() {
		TweedPojoWeaverBootstrapper<MainCompound> bootstrapper = TweedPojoWeaverBootstrapper.create(MainCompound.class);
		ConfigContainer<MainCompound> configContainer = bootstrapper.weave();

		assertThat(configContainer.rootEntry()).satisfies(isCompoundEntryForClassWith(MainCompound.class, rootCompound ->
				assertThat(rootCompound.subEntries())
						.hasEntrySatisfying("primitiveInteger", isSimpleEntryForClass(int.class))
						.hasEntrySatisfying("boxedDouble", isSimpleEntryForClass(Double.class))
						.hasEntrySatisfying("value", isSimpleEntryForClass(InnerValue.class))
						.hasEntrySatisfying("list", isSimpleEntryForClass(List.class))
						.hasEntrySatisfying("compound", isCompoundEntryForClassWith(InnerCompound.class, innerCompound ->
								assertThat(innerCompound.subEntries())
										.hasEntrySatisfying("string", isSimpleEntryForClass(String.class))
										.hasSize(1)))
						.hasSize(5)
		));

		configContainer.initialize();

		assertThat(configContainer.extensions())
				.satisfiesOnlyOnce(extension -> assertThat(extension).isInstanceOf(DummyExtension.class))
				.hasSize(1);
	}

	@Test
	void weavingWithList() {
		TweedPojoWeaverBootstrapper<CompoundWithList> bootstrapper = TweedPojoWeaverBootstrapper.create(CompoundWithList.class);
		ConfigContainer<CompoundWithList> configContainer = bootstrapper.weave();

		assertThat(configContainer.rootEntry()).satisfies(isCompoundEntryForClassWith(CompoundWithList.class, rootCompound ->
				assertThat(rootCompound.subEntries())
						.hasEntrySatisfying("strings", isCollectionEntryForClass(
								List.class,
								list -> assertThat(list.elementEntry()).satisfies(isSimpleEntryForClass(String.class))

						))
		));
	}

	@AutoService(DummyExtension.class)
	public static class DummyExtension implements TweedExtension {
		@Override
		public String getId() {
			return "dummy";
		}
	}

	@PojoWeaving(extensions = {DummyExtension.class})
	@CompoundWeaving(namingFormat = "camel_case")
	@Data
	public static class MainCompound {
		int primitiveInteger;
		Double boxedDouble;
		InnerValue value;
		List<Integer> list;

		InnerCompound compound;
	}

	@CompoundWeaving
	@Data
	public static class InnerCompound {
		String string;
	}

	@Data
	public static class InnerValue {
		int something;
		boolean somethingElse;
	}

	@PojoWeaving(weavers = {CompoundPojoWeaver.class, CollectionPojoWeaver.class, TrivialPojoWeaver.class})
	@CompoundWeaving(namingFormat = "camel_case")
	@Data
	public static class CompoundWithList {
		List<String> strings;
	}
}
