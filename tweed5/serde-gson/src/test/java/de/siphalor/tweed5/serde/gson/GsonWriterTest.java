package de.siphalor.tweed5.serde.gson;

import com.google.gson.GsonBuilder;
import de.siphalor.tweed5.serde.gson.GsonWriter;
import de.siphalor.tweed5.serde_api.api.TweedDataWriter;
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
