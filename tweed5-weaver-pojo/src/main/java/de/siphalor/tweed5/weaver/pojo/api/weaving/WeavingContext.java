package de.siphalor.tweed5.weaver.pojo.api.weaving;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

@Value
public class WeavingContext implements TweedPojoWeavingFunction.NonNull {
	@Nullable
	WeavingContext parent;
	@Getter(AccessLevel.NONE)
	@NotNull
	TweedPojoWeavingFunction.NonNull weavingFunction;
	@NotNull
	ConfigContainer<?> configContainer;
	@NotNull
	String[] path;
	@NotNull
	ExtensionsData extensionsData;
	@NotNull
	Annotations annotations;

	public static Builder builder(TweedPojoWeavingFunction.NonNull weavingFunction, ConfigContainer<?> configContainer) {
		return new Builder(null, weavingFunction, configContainer, new String[0]);
	}

	public static Builder builder(TweedPojoWeavingFunction.NonNull weavingFunction, ConfigContainer<?> configContainer, String baseName) {
		return new Builder(null, weavingFunction, configContainer, new String[]{ baseName });
	}

	public Builder subContextBuilder(String subPathName) {
		String[] newPath = Arrays.copyOf(path, path.length + 1);
		newPath[path.length] = subPathName;
		return new Builder(this, weavingFunction, configContainer, newPath).extensionsData(extensionsData);
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
		private final TweedPojoWeavingFunction.NonNull weavingFunction;
		private final ConfigContainer<?> configContainer;
		private final String[] path;
		private ExtensionsData extensionsData;
		private Annotations annotations;

		public WeavingContext build() {
			return new WeavingContext(
					parent,
					weavingFunction,
					configContainer,
					path,
					extensionsData,
					annotations
			);
		}
	}
}
