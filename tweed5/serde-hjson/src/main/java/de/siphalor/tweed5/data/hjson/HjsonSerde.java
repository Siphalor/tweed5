package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.dataapi.api.TweedSerde;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class HjsonSerde implements TweedSerde {
	private final HjsonWriter.Options writerOptions;

	@Override
	public TweedDataReader createReader(InputStream inputStream) {
		return new HjsonReader(new HjsonLexer(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))));
	}

	@Override
	public TweedDataWriter createWriter(OutputStream outputStream) {
		return new HjsonWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)), writerOptions);
	}

	@Override
	public String getPreferredFileExtension() {
		return ".hjson";
	}
}
