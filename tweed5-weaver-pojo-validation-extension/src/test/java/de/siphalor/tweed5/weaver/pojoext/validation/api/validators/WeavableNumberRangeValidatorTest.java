package de.siphalor.tweed5.weaver.pojoext.validation.api.validators;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

class WeavableNumberRangeValidatorTest {

	@ParameterizedTest
	@MethodSource("constructParams")
	void construct(Class<? extends Number> numberClass, String config, List<Number> works, List<Number> fails) {
		DefaultConfigContainer<Object> configContainer = new DefaultConfigContainer<>();
		configContainer.finishExtensionSetup();

		//noinspection unchecked
		var configEntry = new SimpleConfigEntryImpl<>(configContainer, (Class<Number>) numberClass);

		var validator = WeavableConfigEntryValidator.FACTORY.construct(WeavableNumberRangeValidator.class)
				.typedArg(ConfigEntry.class, configEntry)
				.namedArg("config", config)
				.finish();

		assertAll(Stream.concat(
				works.stream().map(
						number -> () -> assertThat(validator.validate(configEntry, number).issues())
								.as("Number %s should not have validation issues", number)
								.isEmpty()
				),
				fails.stream().map(
						number -> () -> assertThat(validator.validate(configEntry, number).issues())
								.as("Number %s should have validation issues", number)
								.isNotEmpty()
				)
		));
	}

	static Stream<Arguments> constructParams() {
		return Stream.of(
				argumentSet(
						"integer, no range",
						Integer.class,
						"..",
						List.of(Integer.MIN_VALUE, -1234, 0, 1234, Integer.MAX_VALUE),
						List.of()
				),
				argumentSet(
						"integer, greater than",
						Integer.class,
						"12..",
						List.of(13, 25, Integer.MAX_VALUE),
						List.of(Integer.MIN_VALUE, -1234, 0, 12)
				),
				argumentSet(
						"integer, greater than or equal",
						Integer.class,
						"12=..",
						List.of(12, 13, 24, Integer.MAX_VALUE),
						List.of(Integer.MIN_VALUE, -1234, 0, 11)
				),
				argumentSet(
						"integer, less than",
						Integer.class,
						"..-12",
						List.of(Integer.MIN_VALUE, -1234, -13),
						List.of(-12, 0, 12, 1234, Integer.MAX_VALUE)
				),
				argumentSet(
						"integer, less than or equal",
						Integer.class,
						"..=-12",
						List.of(Integer.MIN_VALUE, -1234, -13, -12),
						List.of(-11, 0, 12, 1234, Integer.MAX_VALUE)
				),
				argumentSet(
						"integer, range",
						Integer.class,
						"0=..100",
						List.of(0, 50, 99),
						List.of(Integer.MIN_VALUE, -1, 100, Integer.MAX_VALUE)
				),
				argumentSet(
						"double, range",
						Double.class,
						"1.5=..2.5",
						List.of(1.5, 2.0, 2.1, 2.49999),
						List.of(Integer.MIN_VALUE, 0, 1.49999, 2.5, 3.0, Integer.MAX_VALUE)
				)
		);
	}
}
