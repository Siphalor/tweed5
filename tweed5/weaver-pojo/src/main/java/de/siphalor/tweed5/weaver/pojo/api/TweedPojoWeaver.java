package de.siphalor.tweed5.weaver.pojo.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverImpl;
import org.jetbrains.annotations.Contract;

public interface TweedPojoWeaver<T> {
	static <T> TweedPojoWeaver<T> forClass(Class<T> pojoClass) {
		return TweedPojoWeaverImpl.implForClass(pojoClass);
	}

	Class<T> pojoClass();
	ConfigContainer<T> configContainer();

	@Contract("_ -> this")
	default TweedPojoWeaver<T> withWeavingExtensions(Class<? extends TweedPojoWeavingExtension>... weavingExtensions) {
		for (Class<? extends TweedPojoWeavingExtension> weavingExtension : weavingExtensions) {
			withWeavingExtension(weavingExtension);
		}
		return this;
	}

	@Contract("_ -> this")
	TweedPojoWeaver<T> withWeavingExtension(Class<? extends TweedPojoWeavingExtension> weavingExtension);

	@Contract("_ -> this")
	default TweedPojoWeaver<T> withExtensions(Class<? extends TweedExtension>... extensions) {
		for (Class<? extends TweedExtension> extension : extensions) {
			withExtension(extension);
		}
		return this;
	}

	@Contract("_ -> this")
	TweedPojoWeaver<T> withExtension(Class<? extends TweedExtension> extension);

	@Contract("_ -> this")
	TweedPojoWeaver<T> withConfigContainer(ConfigContainer<T> container);

	ConfigContainer<T> weave();
}
