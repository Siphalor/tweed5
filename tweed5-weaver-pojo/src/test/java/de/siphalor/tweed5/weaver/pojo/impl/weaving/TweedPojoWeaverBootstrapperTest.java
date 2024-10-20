package de.siphalor.tweed5.weaver.pojo.impl.weaving;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.weaver.pojo.api.annotation.CompoundWeaving;
import de.siphalor.tweed5.weaver.pojo.api.annotation.PojoWeaving;
import lombok.Data;
import org.junit.jupiter.api.Test;

import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isCompoundEntryForClassWith;
import static de.siphalor.tweed5.weaver.pojo.test.ConfigEntryAssertions.isSimpleEntryForClass;
import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("unused")
class TweedPojoWeaverBootstrapperTest {
	@Test
	void defaultWeaving() {
		TweedPojoWeaverBootstrapper<DefaultWeaving> bootstrapper = TweedPojoWeaverBootstrapper.create(DefaultWeaving.class);
		ConfigContainer<DefaultWeaving> configContainer = bootstrapper.weave();

		assertThat(configContainer.rootEntry()).satisfies(isCompoundEntryForClassWith(DefaultWeaving.class, rootCompound ->
			assertThat(rootCompound.subEntries())
					.hasEntrySatisfying("primitiveInteger", isSimpleEntryForClass(int.class))
					.hasEntrySatisfying("boxedDouble", isSimpleEntryForClass(Double.class))
					.hasEntrySatisfying("value", isSimpleEntryForClass(InnerValue.class))
					.hasEntrySatisfying("compound", isCompoundEntryForClassWith(InnerCompound.class, innerCompound ->
							assertThat(innerCompound.subEntries())
								.hasEntrySatisfying("string", isSimpleEntryForClass(String.class))
								.hasSize(1)))
					.hasSize(4)
		));

		configContainer.initialize();

		assertThat(configContainer.extensions())
				.satisfiesOnlyOnce(extension -> assertThat(extension).isInstanceOf(DummyExtension.class))
				.hasSize(1);
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
	public static class DefaultWeaving {
		int primitiveInteger;
		Double boxedDouble;
		InnerValue value;

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
}