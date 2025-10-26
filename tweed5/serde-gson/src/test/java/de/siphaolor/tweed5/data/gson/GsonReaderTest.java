package de.siphaolor.tweed5.data.gson;

import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.testutils.serde.json.JsonReaderTest;

import java.io.StringReader;

class GsonReaderTest implements JsonReaderTest {
	@Override
	public TweedDataReader createJsonReader(String text) {
		JsonReader jsonReader = new GsonBuilder().create().newJsonReader(new StringReader(text));
		return new GsonReader(jsonReader);
	}
}
