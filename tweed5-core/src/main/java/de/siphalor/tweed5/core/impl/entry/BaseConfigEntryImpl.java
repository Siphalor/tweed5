package de.siphalor.tweed5.core.impl.entry;

import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntryVisitor;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.DefaultMiddlewareContainer;
import de.siphalor.tweed5.core.api.middleware.MiddlewareContainer;
import de.siphalor.tweed5.core.api.validation.ConfigEntryValidationExtension;
import de.siphalor.tweed5.core.api.validation.ConfigEntryValidationMiddleware;
import de.siphalor.tweed5.core.api.validation.ConfigEntryValueValidationException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

@RequiredArgsConstructor
@Getter
abstract class BaseConfigEntryImpl<T> implements ConfigEntry<T> {
	private static final ConfigEntryValidationMiddleware ROOT_VALIDATION = new ConfigEntryValidationMiddleware() {
		@Override
		public <U> void validate(ConfigEntry<U> configEntry, U value) {}
	};

	@NotNull
	private final Class<T> valueClass;
	private ConfigContainer<?> container;
	private EntryExtensionsData extensionsData;
	private boolean sealed;
	private ConfigEntryValidationMiddleware validationMiddleware;

	@Override
	public void seal(ConfigContainer<?> container) {
		requireUnsealed();

		this.container = container;
		this.extensionsData = container.createExtensionsData();

		MiddlewareContainer<ConfigEntryValidationMiddleware> validationMiddlewareContainer = new DefaultMiddlewareContainer<>();

		for (TweedExtension extension : container().extensions()) {
			if (extension instanceof ConfigEntryValidationExtension) {
				validationMiddlewareContainer.register(((ConfigEntryValidationExtension) extension).validationMiddleware(this));
			}
		}
		validationMiddlewareContainer.seal();

		validationMiddleware = validationMiddlewareContainer.process(ROOT_VALIDATION);

		sealed = true;
	}

	protected void requireUnsealed() {
		if (sealed) {
			throw new IllegalStateException("Config entry is already sealed!");
		}
	}

	@Override
	public void validate(T value) throws ConfigEntryValueValidationException {
		if (value == null) {
			if (valueClass.isPrimitive()) {
				throw new ConfigEntryValueValidationException("Value must not be null");
			}
		} else if (!valueClass.isAssignableFrom(value.getClass())) {
			throw new ConfigEntryValueValidationException("Value must be of type " + valueClass.getName());
		}

		validationMiddleware.validate(this, value);
	}

	@Override
	public void visitInOrder(ConfigEntryVisitor visitor) {
		visitor.visitEntry(this);
	}
}
