package de.siphalor.tweed5.namingformat.api;

public interface NamingFormatProvider {
	void provideNamingFormats(ProvidingContext context);

	interface ProvidingContext {
		void registerNamingFormat(String id, NamingFormat namingFormat);
	}
}
