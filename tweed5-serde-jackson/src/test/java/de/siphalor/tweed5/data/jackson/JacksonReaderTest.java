package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.testutils.serde.json.JsonReaderTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class JacksonReaderTest implements JsonReaderTest {
	@Disabled("Jackson does not support integer values with floating point syntax elements")
	@Override
	public void jsonByteFloaty(String input, byte expected) {
		JsonReaderTest.super.jsonByteFloaty(input, expected);
	}

	@Disabled("Jackson does not support integer values with floating point syntax elements")
	@Override
	public void jsonIntFloaty(String input, int expected) {
		JsonReaderTest.super.jsonIntFloaty(input, expected);
	}

	@Disabled("Jackson's double precision is abysmal in comparison with everyone else's even with fast double parsing turned off")
	@Override
	public void jsonDouble(String input, double expected) {
		JsonReaderTest.super.jsonDouble(input, expected);
	}

	@SneakyThrows
	@Override
	public TweedDataReader createJsonReader(String text) {
		var parser = JsonFactory.builder()
				.enable(StreamReadFeature.INCLUDE_SOURCE_IN_LOCATION)
				.build()
				.createParser(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));
		return new JacksonReader(parser);
	}
}
