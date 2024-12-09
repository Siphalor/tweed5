package de.siphalor.tweed5.weaver.pojoext.serde.impl;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.InstanceOfAssertFactories.type;

class SerdePojoReaderWriterSpecTest {

	@ParameterizedTest
	@CsvSource(ignoreLeadingAndTrailingWhitespace = false, value = {
			"  abc  ,abc",
			"  abc()  ,abc",
			"  abc.123  ,abc.123",
			"abc.123  (  )  ,abc.123",
			"123.abc,123.abc",
	})
	@SneakyThrows
	void parseSimpleIdentifier(String input, String identifier) {
		SerdePojoReaderWriterSpec spec = SerdePojoReaderWriterSpec.parse(input);
		assertThat(spec.identifier()).isEqualTo(identifier);
		assertThat(spec.arguments()).isEmpty();
	}

	@Test
	@SneakyThrows
	void parseNested() {
		SerdePojoReaderWriterSpec spec = SerdePojoReaderWriterSpec.parse("abc.def ( 12 ( def, ghi ( ) ), jkl ) ");
		assertThat(spec).isEqualTo(new SerdePojoReaderWriterSpec("abc.def", Arrays.asList(
				new SerdePojoReaderWriterSpec("12", Arrays.asList(
						new SerdePojoReaderWriterSpec("def", Collections.emptyList()),
						new SerdePojoReaderWriterSpec("ghi", Collections.emptyList())
				)),
				new SerdePojoReaderWriterSpec("jkl", Collections.emptyList())
		)));
	}

	@ParameterizedTest
	@CsvSource(ignoreLeadingAndTrailingWhitespace = false, nullValues = "EOF", delimiter = ';', value = {
			" abc def ;6;d",
			"abcäöüdef;4;ä",
			"abc.def(;8;EOF",
			"'';0;EOF",
			",;1;,",
			"abc(,);5;,",
			"abc..def;5;.",
	})
	@SneakyThrows
	void parseError(String input, int index, String codePoint) {
		assertThatThrownBy(() -> SerdePojoReaderWriterSpec.parse(input))
				.asInstanceOf(type(SerdePojoReaderWriterSpec.ParseException.class))
				.isInstanceOf(SerdePojoReaderWriterSpec.ParseException.class)
				.satisfies(
					exception -> assertThat(exception.index()).as("index of: " + exception.getMessage()).isEqualTo(index),
					exception -> assertThat(exception.codePoint()).as("code point of: " + exception.getMessage())
							.isEqualTo(codePoint == null ? -1 : codePoint.codePointAt(0))
				);
	}
}