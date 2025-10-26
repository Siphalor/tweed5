package de.siphalor.tweed5.data.hjson;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HjsonWriterTest {
	private HjsonWriter writer;
	private StringWriter stringWriter;

	@BeforeEach
	void setUp() {
		stringWriter = new StringWriter();
	}

	@Test
	void complex() {
		setUpHjsonWriter(new HjsonWriter.Options());

		writer.visitMapStart();
		writer.visitMapEntryKey("test");
		writer.visitBoolean(false);
		writer.visitMapEntryKey("null");
		writer.visitNull();
		writer.visitMapEntryKey("a list");
		writer.visitListStart();
		writer.visitInt(12);
		writer.visitInt(34);
		writer.visitString("Testing\n  multiline\nstuff");
		writer.visitListEnd();
		writer.visitMapEnd();

		assertEquals(
				"{\n" +
						"\ttest: false\n" +
						"\tnull: null\n" +
						"\t\"a list\": [\n" +
						"\t\t12\n" +
						"\t\t34\n" +
						"\t\t'''\n" +
						"\t\tTesting\n" +
						"\t\t  multiline\n" +
						"\t\tstuff\n" +
						"\t\t'''\n" +
						"\t]\n" +
						"}\n",
				stringWriter.toString()
		);
	}

	void setUpHjsonWriter(HjsonWriter.Options options) {
		writer = new HjsonWriter(stringWriter, options);
	}
}