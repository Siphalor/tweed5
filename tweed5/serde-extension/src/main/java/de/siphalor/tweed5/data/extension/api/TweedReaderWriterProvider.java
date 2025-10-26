package de.siphalor.tweed5.data.extension.api;

import lombok.RequiredArgsConstructor;

/**
 * An interface that allows to register {@link TweedEntryReader}s and {@link TweedEntryWriter}s.
 * Implementing classes should be Java services, e.g. using {@link com.google.auto.service.AutoService}.
 */
public interface TweedReaderWriterProvider {
	void provideReaderWriters(ProviderContext context);

	/**
	 * The context where reader and writer factories may be registered.<br />
	 * The reader and writer ids must be globally unique. It is therefore recommended to scope custom reader and writer ids in your own namespace (e.g. "de.siphalor.custom.blub")
	 * Ids may consist of alphanumeric characters and dots.
	 */
	interface ProviderContext {
		void registerReaderFactory(String id, ReaderWriterFactory<TweedEntryReader<?, ?>> readerFactory);
		void registerWriterFactory(String id, ReaderWriterFactory<TweedEntryWriter<?, ?>> writerFactory);
	}

	/**
	 * A factory that creates a new reader or writer using delegate readers/writers as its arguments.
	 * @param <T>
	 */
	@FunctionalInterface
	interface ReaderWriterFactory<T> {
		T create(T... delegateReaderWriters);
	}

	@RequiredArgsConstructor
	final class StaticReaderWriterFactory<T> implements ReaderWriterFactory<T> {
		private final T readerWriter;

		@SafeVarargs
		@Override
		public final T create(T... delegateReaderWriters) {
			if (delegateReaderWriters.length != 0) {
				throw new IllegalArgumentException("Reader writer factory must not be passed any delegates as arguments");
			}
			return readerWriter;
		}
	}
}
