package de.siphalor.tweed5.data.gson;

import com.google.gson.GsonBuilder;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.testutils.serde.json.JsonWriterTest;
import lombok.SneakyThrows;

import java.io.StringWriter;

class GsonWriterTest implements JsonWriterTest {
	@Override
	@SneakyThrows
	public TweedDataWriter createPrettyJsonWriter(StringWriter stringWriter) {
		return new GsonWriter(new GsonBuilder().setPrettyPrinting().create().newJsonWriter(stringWriter));
	}
}
