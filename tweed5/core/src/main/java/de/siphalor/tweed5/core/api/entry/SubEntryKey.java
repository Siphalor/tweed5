package de.siphalor.tweed5.core.api.entry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.Nullable;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SubEntryKey {
	String entry;
	@Nullable String value;
	@Nullable String data;

	/**
	 * Models the key of a transparent entry. Transparent entries are sub entries that only exist in the entry tree
	 * but are not actually present in the data.
	 * @param entryKey the key of the entry in the entry tree
	 * @apiNote The resulting key is not addressable.
	 */
	public static SubEntryKey transparent(String entryKey) {
		return new SubEntryKey(entryKey, null, null);
	}

	/**
	 * Models the key of a transparent entry with a data key for use with {@link AddressableStructuredConfigEntry}s.
	 * @param entryKey the key of the entry in the entry tree
	 * @param dataKey the "address" of the data in the {@link AddressableStructuredConfigEntry}
	 */
	public static SubEntryKey transparentAddressable(String entryKey, String dataKey) {
		return new SubEntryKey(entryKey, null, dataKey);
	}

	/**
	 * A "normal" sub entry key.
	 * @param entryKey the key of the entry in the entry tree
	 * @param valueKey a potentially differing key for the user-facing data tree
	 */
	public static SubEntryKey structured(String entryKey, String valueKey) {
		return new SubEntryKey(entryKey, valueKey, null);
	}

	/**
	 * A sub entry key for {@link AddressableStructuredConfigEntry}s.
	 * @param entryKey the key of the entry in the entry tree
	 * @param valueKey a potentially differing key for the user-facing data tree
	 * @param dataKey the "address" of the data in the {@link AddressableStructuredConfigEntry}
	 * @apiNote {@code valueKey} and {@code dataKey} are usually a 1:1 mapping.
	 */
	public static SubEntryKey addressable(String entryKey, String valueKey, String dataKey) {
		return new SubEntryKey(entryKey, valueKey, dataKey);
	}
}
