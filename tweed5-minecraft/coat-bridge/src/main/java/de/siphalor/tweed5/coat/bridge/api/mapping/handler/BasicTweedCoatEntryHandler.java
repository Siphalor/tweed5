package de.siphalor.tweed5.coat.bridge.api.mapping.handler;

import de.siphalor.coat.handler.ConfigEntryHandler;
import de.siphalor.coat.handler.Message;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssueLevel;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationResult;
import lombok.extern.apachecommons.CommonsLog;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static de.siphalor.tweed5.coat.bridge.api.TweedCoatMappingUtils.literalComponent;

@CommonsLog
public class BasicTweedCoatEntryHandler<T extends @Nullable Object> implements ConfigEntryHandler<T> {
	protected final ConfigEntry<T> configEntry;
	protected final T defaultValue;
	protected final Consumer<T> parentSaveHandler;
	protected final ValidationExtension validationExtension;

	public BasicTweedCoatEntryHandler(ConfigEntry<T> configEntry, T defaultValue, Consumer<T> parentSaveHandler) {
		this.configEntry = configEntry;
		this.defaultValue = defaultValue;
		this.parentSaveHandler = parentSaveHandler;
		this.validationExtension = configEntry.container().extension(ValidationExtension.class)
				.orElseThrow(() -> new IllegalStateException("No validation extension registered"));
	}

	@Override
	public T getDefault() {
		return defaultValue;
	}

	@Override
	public Collection<Message> getMessages(T value) {
		ValidationIssues issues = validationExtension.validate(configEntry, value);
		return issues.issuesByPath().values().stream()
				.flatMap(entryIssues -> entryIssues.issues().stream())
				.map(issue -> new Message(
						mapLevel(issue.level()),
						literalComponent(issue.message())
				))
				.collect(Collectors.toList());
	}

	private static Message.Level mapLevel(ValidationIssueLevel level) {
		switch (level) {
			case INFO:
				return Message.Level.INFO;
			case WARN:
				return Message.Level.WARNING;
			case ERROR:
				return Message.Level.ERROR;
			default:
				throw new IllegalStateException("Unknown validation issue level " + level);
		}
	}

	@Override
	public void save(T value) {
		parentSaveHandler.accept(processSaveValue(value));
	}

	@Override
	public Component asText(T value) {
		return literalComponent(Objects.toString(value));
	}

	protected T processSaveValue(T value) {
		ValidationResult<T> validationResult = validationExtension.validateValueFlat(configEntry, value);
		if (validationResult.hasError()) {
			log.warn(
					"Failed to save value " + value + " because of issues: " + validationResult.issues()
							+ "; using default: " + defaultValue + " instead"
			);
			return defaultValue;
		}
		return validationResult.value();
	}
}
