package de.siphalor.tweed5.dataapi.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TweedSerde {
	TweedDataReader createReader(InputStream inputStream);
	TweedDataVisitor createWriter(OutputStream outputStream) throws IOException;
	String getPreferredFileExtension();
}
