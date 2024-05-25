package de.siphalor.tweed5.data.hjson;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedSerde;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HjsonSerde implements TweedSerde {
	@Override
	public TweedDataReader createReader(InputStream inputStream) {
		return null;
	}

	@Override
	public TweedDataVisitor createWriter(OutputStream outputStream) throws IOException {
		return null;
	}

	@Override
	public String getPreferredFileExtension() {
		return "";
	}
}
