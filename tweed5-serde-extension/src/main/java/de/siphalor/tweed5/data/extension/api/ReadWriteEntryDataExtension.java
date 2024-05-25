package de.siphalor.tweed5.data.extension.api;

public interface ReadWriteEntryDataExtension {
	TweedEntryReader<?, ?> entryReaderChain();
	TweedEntryWriter<?, ?> entryWriterChain();
}
