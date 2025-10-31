package de.siphalor.tweed5.coat.bridge.api.mapping;

import de.siphalor.coat.handler.ConfigEntryHandler;
import de.siphalor.coat.input.ConfigInput;
import de.siphalor.coat.screen.ConfigContentWidget;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The result of a {@link TweedCoatMapper}.
 * <br />
 * There are three types of results:
 * <ol>
 *     <li>The mapper isn't applicable to the entry.</li>
 *     <li>The mapper is applicable and provides methods for creating
 *     {@link ConfigInput} and {@link ConfigEntryHandler} instances for the config entry.</li>
 *     FIXME
 * </ol>
 *
 *
 * @param <T> the actual (Tweed) type of the entry
 * @param <U> the UI (coat) type of the entry
 */
public interface TweedCoatEntryMappingResult<T, U> {
	TweedCoatEntryMappingResult<?, ?> NOT_APPLICABLE = new TweedCoatEntryMappingResult<Object, Object>() {
		@Override
		public boolean isApplicable() {
			return false;
		}

		@Override
		public @Nullable ConfigInput<Object> createInput(TweedCoatEntryCreationContext<Object> context) {
			return null;
		}

		@Override
		public @Nullable ConfigEntryHandler<Object> createHandler(TweedCoatEntryCreationContext<Object> context) {
			return null;
		}

		@Override
		public @Nullable ConfigContentWidget createContentWidget(TweedCoatEntryCreationContext<Object> context) {
			return null;
		}

	};

	static <T, U> TweedCoatEntryMappingResult<T, U> notApplicable() {
		//noinspection unchecked
		return (TweedCoatEntryMappingResult<T, U>) NOT_APPLICABLE;
	}

	boolean isApplicable();

	@Nullable ConfigInput<U> createInput(TweedCoatEntryCreationContext<T> context);

	@Nullable ConfigEntryHandler<U> createHandler(TweedCoatEntryCreationContext<T> context);

	@Nullable ConfigContentWidget createContentWidget(TweedCoatEntryCreationContext<T> context);
}
