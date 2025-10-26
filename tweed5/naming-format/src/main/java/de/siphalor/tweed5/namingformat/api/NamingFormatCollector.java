package de.siphalor.tweed5.namingformat.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public class NamingFormatCollector {
	private final Map<String, NamingFormat> namingFormats = new HashMap<>();

	public void setupFormats() {
		ServiceLoader<NamingFormatProvider> serviceLoader = ServiceLoader.load(NamingFormatProvider.class);
		Context context = new Context();
		for (NamingFormatProvider provider : serviceLoader) {
			provider.provideNamingFormats(context);
		}
	}

	public Map<String, NamingFormat> namingFormats() {
		return Collections.unmodifiableMap(namingFormats);
	}

	private class Context implements NamingFormatProvider.ProvidingContext {
		@Override
		public void registerNamingFormat(String id, NamingFormat namingFormat) {
			namingFormats.put(id, namingFormat);
		}
	}
}
