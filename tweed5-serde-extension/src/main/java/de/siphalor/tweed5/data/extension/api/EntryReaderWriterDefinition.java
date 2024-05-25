package de.siphalor.tweed5.data.extension.api;

public interface EntryReaderWriterDefinition {
	TweedEntryReader<?, ?> reader();
	TweedEntryWriter<?, ?> writer();
}
