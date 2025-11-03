package de.siphalor.tweed5.coat.bridge.api.mapping.handler;

import de.siphalor.coat.handler.ConfigEntryHandler;
import de.siphalor.coat.handler.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

import static de.siphalor.tweed5.coat.bridge.api.TweedCoatMappingUtils.literalComponent;
import static de.siphalor.tweed5.coat.bridge.api.TweedCoatMappingUtils.translatableComponent;

@RequiredArgsConstructor
@CommonsLog
public class ConvertingTweedCoatEntryHandler<T extends @Nullable Object, C> implements ConfigEntryHandler<C> {
	private static final String CONVERSION_EXCEPTION_TEXT_KEY = "tweed5_coat_bridge.handler.conversion.exception";

	private final ConfigEntryHandler<T> inner;
	private final Function<T, C> toCoatMapper;
	private final Function<C, T> fromCoatMapper;

	@Override
	public C getDefault() {
		return toCoatMapper.apply(inner.getDefault());
	}

	@Override
	public Collection<Message> getMessages(C value) {
		try {
			T innerValue = fromCoatMapper.apply(value);
			return inner.getMessages(innerValue);
		} catch (Exception e) {
			return Collections.singletonList(new Message(
					Message.Level.ERROR,
					translatableComponent(CONVERSION_EXCEPTION_TEXT_KEY, e.getMessage())
			));
		}
	}

	@Override
	public void save(C value) {
		inner.save(convertSaveValue(value));
	}

	protected T convertSaveValue(C value) {
		try {
			return fromCoatMapper.apply(value);
		} catch (Exception e) {
			log.warn(
					"Failed to convert value "
							+ value
							+ " for saving, using default: "
							+ inner.getDefault(), e
			);
			return inner.getDefault();
		}
	}

	@Override
	public Component asText(C value) {
		return literalComponent(Objects.toString(value));
	}
}
