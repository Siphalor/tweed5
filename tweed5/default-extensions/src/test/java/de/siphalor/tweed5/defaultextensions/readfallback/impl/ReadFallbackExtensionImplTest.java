package de.siphalor.tweed5.defaultextensions.readfallback.impl;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.TweedWriteContext;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.dataapi.api.*;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.defaultextensions.readfallback.api.ReadFallbackExtension;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.read;
import static de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension.presetValue;
import static org.assertj.core.api.Assertions.assertThat;

@NullMarked
class ReadFallbackExtensionImplTest {
	@Test
	void test() {
		DefaultConfigContainer<Integer> configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(PresetsExtension.class);
		configContainer.registerExtension(ReadFallbackExtension.DEFAULT);
		configContainer.finishExtensionSetup();

		ConfigEntry<Integer> entry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(presetValue(PresetsExtension.DEFAULT_PRESET_NAME, -1))
				.apply(entryReaderWriter(new TweedEntryReaderWriter<>() {
					@Override
					public Integer read(
							TweedDataReader reader,
							ConfigEntry<Integer> entry,
							TweedReadContext context
					) throws TweedEntryReadException {
						int value;
						try {
							value = reader.readToken().readAsInt();
						} catch (TweedDataReadException e) {
							throw new IllegalStateException("Should not be called", e);
						}
						if (value % 2 == 0) {
							return value;
						} else {
							throw new TweedEntryReadException("Value is not even", context);
						}
					}

					@Override
					public void write(
							TweedDataVisitor writer,
							@Nullable Integer value,
							ConfigEntry<Integer> entry,
							TweedWriteContext context
					) throws TweedDataWriteException {
						throw new IllegalStateException("Should not be called");
					}
				}));

		configContainer.attachTree(entry);
		configContainer.initialize();

		assertThat(entry.call(read(new HjsonReader(new HjsonLexer(new StringReader("12")))))).isEqualTo(12);
		assertThat(entry.call(read(new HjsonReader(new HjsonLexer(new StringReader("13")))))).isEqualTo(-1);
	}
}
