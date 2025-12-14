package de.siphalor.tweed5.weaver.pojo.api;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.weaver.pojo.api.weaving.TweedPojoWeavingExtension;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverImpl;
import org.jetbrains.annotations.Contract;

/**
 * Entry point for weaving a {@link de.siphalor.tweed5.core.api.container.ConfigContainer}
 * from a POJO class annotated for Tweed POJO weaving.
 * <p>
 * A {@code TweedPojoWeaver} configures the weaving process by
 * adding Tweed extensions and POJO weaving extensions and finally produces
 * a configured {@link ConfigContainer} tree via {@link #weave()}.
 * </p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * ConfigContainer<MyPojo> container = TweedPojoWeaver
 *         .forClass(MyPojo.class)
 *         .withExtensions(MyExtension.class)
 *         .withWeavingExtensions(MyPojoWeavingExt.class)
 *         .weave();
 * }</pre>
 *
 * @param <T> the root POJO type to weave into a configuration tree
 */
public interface TweedPojoWeaver<T> {
    /**
     * Creates a new weaver for the given POJO class.
     * The class must fulfill the requirements of the POJO weaver (e.g. carry
     * the appropriate annotations); otherwise an exception will be thrown when
     * setting up the weaver.
     *
     * @param pojoClass the POJO class that represents the configuration root
     * @param <T>       the POJO type
     * @return a new {@code TweedPojoWeaver} instance for the given class
     */
	static <T> TweedPojoWeaver<T> forClass(Class<T> pojoClass) {
		return TweedPojoWeaverImpl.implForClass(pojoClass);
	}

    /**
     * @return the POJO class this weaver is configured for
     */
	Class<T> pojoClass();

    /**
     * Returns the {@link ConfigContainer} used during weaving. If none has been
     * explicitly provided via {@link #withConfigContainer(ConfigContainer)}, the
     * container is lazily created from the POJO's weaving configuration when this
     * method is first called.
     *
     * @return the container that will host the woven configuration tree
     */
	ConfigContainer<T> configContainer();

    /**
     * Registers multiple POJO weaving extensions at once.
     *
     * @param weavingExtensions the weaving extension classes to add
     * @return {@code this} weaver for chaining
     */
	@Contract("_ -> this")
	default TweedPojoWeaver<T> withWeavingExtensions(Class<? extends TweedPojoWeavingExtension>... weavingExtensions) {
		for (Class<? extends TweedPojoWeavingExtension> weavingExtension : weavingExtensions) {
			withWeavingExtension(weavingExtension);
		}
		return this;
	}

    /**
     * Registers a single POJO weaving extension. Weaving extensions influence how
     * POJO members are turned into {@link de.siphalor.tweed5.core.api.entry.ConfigEntry}
     * instances during the weave.
     *
     * @param weavingExtension the weaving extension class to add
     * @return {@code this} weaver for chaining
     * @throws IllegalStateException if called after the configuration tree was attached
     */
	@Contract("_ -> this")
	TweedPojoWeaver<T> withWeavingExtension(Class<? extends TweedPojoWeavingExtension> weavingExtension);

    /**
     * Registers multiple Tweed core extensions at once.
     *
     * @param extensions the extension classes to register in the target container
     * @return {@code this} weaver for chaining
     */
	@Contract("_ -> this")
	default TweedPojoWeaver<T> withExtensions(Class<? extends TweedExtension>... extensions) {
		for (Class<? extends TweedExtension> extension : extensions) {
			withExtension(extension);
		}
		return this;
	}

    /**
     * Registers a single Tweed core extension in the target container.
     * If a container has already been created, the extension is registered immediately;
     * otherwise it will be applied when the container is created.
     *
     * @param extension the extension class to register
     * @return {@code this} weaver for chaining
     * @throws IllegalStateException if called after extensions have been finalized in the container
     */
	@Contract("_ -> this")
	TweedPojoWeaver<T> withExtension(Class<? extends TweedExtension> extension);

    /**
     * Provides a pre-created {@link ConfigContainer} instance to use for weaving
     * instead of creating one automatically.
     *
     * @param container the container to use
     * @return {@code this} weaver for chaining
     * @throws IllegalStateException if a container has already been assigned
     */
	@Contract("_ -> this")
	TweedPojoWeaver<T> withConfigContainer(ConfigContainer<T> container);

    /**
     * Performs the actual weaving and attaches the resulting configuration tree
     * to the target {@link ConfigContainer}. This method can be called only once
     * per weaver instance.
     *
     * @return the configured container with the woven configuration tree attached
     * @throws IllegalStateException if weaving has already been performed
     */
	ConfigContainer<T> weave();
}
