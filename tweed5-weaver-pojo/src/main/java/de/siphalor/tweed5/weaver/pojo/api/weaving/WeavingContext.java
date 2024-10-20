package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import de.siphalor.tweed5.core.api.collection.TypedMultimap;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Value
public class WeavingContext implements TweedPojoWeavingFunction.NonNull {
	@Nullable
	WeavingContext parent;
	ExtensionsData extensionsData;
	@Getter(AccessLevel.NONE)
	TweedPojoWeavingFunction.NonNull weavingFunction;
	String[] path;
	TypedMultimap<Object> additionalData;

	public static Builder builder() {
		return new Builder(null, new String[0]);
	}

	public static Builder builder(String baseName) {
		return new Builder(null, new String[]{ baseName });
	}

	public Builder subContextBuilder(String subPathName) {
		String[] newPath = Arrays.copyOf(path, path.length + 1);
		newPath[path.length] = subPathName;
		return new Builder(this, newPath)
				.extensionsData(extensionsData)
				.weavingFunction(weavingFunction);
	}

	@Override
	public @NotNull <T> ConfigEntry<T> weaveEntry(Class<T> valueClass, WeavingContext context) {
		return weavingFunction.weaveEntry(valueClass, context);
	}

	public interface ExtensionsData extends Patchwork<ExtensionsData> {}

	@Accessors(fluent = true, chain = true)
	@Setter
	@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
	public static class Builder {
		@Nullable
		private final WeavingContext parent;
		private final String[] path;
		private ExtensionsData extensionsData;
		private TweedPojoWeavingFunction.NonNull weavingFunction;
		private TypedMultimap<Object> additionalData;

		public WeavingContext build() {
			return new WeavingContext(
					parent,
					extensionsData,
					weavingFunction,
					path,
					additionalData
			);
		}
	}
}
