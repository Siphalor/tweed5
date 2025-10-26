package de.siphalor.tweed5.dataapi.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface TweedSerde {
	TweedDataReader createReader(InputStream inputStream);
	TweedDataWriter createWriter(OutputStream outputStream) throws IOException;

	/**
	 * Yields the file extension that should normally be used for this serde.
	 * @return the file extension, typically with a leading dot.
	 */
	String getPreferredFileExtension();
}
