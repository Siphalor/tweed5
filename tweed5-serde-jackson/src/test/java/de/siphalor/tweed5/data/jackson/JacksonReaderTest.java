package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.StreamReadFeature;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.testutils.serde.json.JsonReaderTest;
import lombok.SneakyThrows;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

class JacksonReaderTest implements JsonReaderTest {
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
