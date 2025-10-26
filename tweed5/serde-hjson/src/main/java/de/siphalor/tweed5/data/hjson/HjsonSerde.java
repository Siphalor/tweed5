package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedSerde;

import java.io.*;

public class HjsonSerde implements TweedSerde {
	@Override
	public TweedDataReader createReader(InputStream inputStream) {
		return new HjsonReader(new HjsonLexer(new InputStreamReader(inputStream)));
	}

	@Override
	public TweedDataVisitor createWriter(OutputStream outputStream) throws IOException {
		return new HjsonWriter(new OutputStreamWriter(outputStream), new HjsonWriter.Options());
	}

	@Override
	public String getPreferredFileExtension() {
		return "";
	}
}
