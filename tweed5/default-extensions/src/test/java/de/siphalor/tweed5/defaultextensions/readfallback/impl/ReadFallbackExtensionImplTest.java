package de.siphalor.tweed5.defaultextensions.readfallback.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReadException;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.TweedWriteContext;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import de.siphalor.tweed5.data.hjson.HjsonLexer;
import de.siphalor.tweed5.data.hjson.HjsonReader;
import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.defaultextensions.pather.api.PatherExtension;
import de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension;
import de.siphalor.tweed5.defaultextensions.readfallback.api.ReadFallbackExtension;
import de.siphalor.tweed5.testutils.generic.log.LogCaptureMockitoExtension;
import de.siphalor.tweed5.testutils.generic.log.LogsCaptor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.read;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.compoundReaderWriter;
import static de.siphalor.tweed5.defaultextensions.presets.api.PresetsExtension.presetValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.STRING;

@ExtendWith(LogCaptureMockitoExtension.class)
@NullMarked
class ReadFallbackExtensionImplTest {
	@Test
	void test(LogsCaptor<ReadFallbackExtensionImpl> logsCaptor) {
		DefaultConfigContainer<Integer> configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(PresetsExtension.class);
		configContainer.registerExtension(ReadFallbackExtension.DEFAULT);
		configContainer.finishExtensionSetup();

		ConfigEntry<Integer> entry = new SimpleConfigEntryImpl<>(configContainer, Integer.class)
				.apply(presetValue(PresetsExtension.DEFAULT_PRESET_NAME, -1))
				.apply(entryReaderWriter(new EvenIntReader()));

		configContainer.attachTree(entry);
		configContainer.initialize();

		assertThat(entry.call(read(new HjsonReader(new HjsonLexer(new StringReader("12")))))).isEqualTo(12);
		assertThat(logsCaptor.getLogsForLevel(Level.ERROR)).isEmpty();
		logsCaptor.clear();

		assertThat(entry.call(read(new HjsonReader(new HjsonLexer(new StringReader("13")))))).isEqualTo(-1);
		assertThat(logsCaptor.getLogsForLevel(Level.ERROR)).hasSize(1);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	void nestedWithPather(LogsCaptor<ReadFallbackExtensionImpl> logsCaptor) {
		DefaultConfigContainer<Map<String, Object>> configContainer = new DefaultConfigContainer<>();
		configContainer.registerExtension(ReadWriteExtension.class);
		configContainer.registerExtension(PatherExtension.class);
		configContainer.registerExtension(PresetsExtension.class);
		configContainer.registerExtension(ReadFallbackExtension.DEFAULT);
		configContainer.finishExtensionSetup();

		CompoundConfigEntry<Map<String, Object>> root = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>) (Class) Map.class,
				HashMap::new,
				Collections.singletonMap(
						"first",
						new StaticMapCompoundConfigEntryImpl<>(
								configContainer,
								(Class<Map<String, Object>>) (Class) Map.class,
								HashMap::new,
								Collections.singletonMap(
										"second",
										new SimpleConfigEntryImpl<>(
												configContainer,
												Integer.class
										).apply(presetValue(PresetsExtension.DEFAULT_PRESET_NAME, -1))
												.apply(entryReaderWriter(new EvenIntReader()))
								)
						).apply(entryReaderWriter(TweedEntryReaderWriters.compoundReaderWriter()))
				)
		).apply(entryReaderWriter(compoundReaderWriter()));

		configContainer.attachTree(root);
		configContainer.initialize();

		assertThat(root.call(read(new HjsonReader(new HjsonLexer(new StringReader(
				"{first: {second: 12}}"
		))))))
				.extracting(map -> (Map<String, Object>) map.get("first"))
				.extracting(map -> (Integer) map.get("second"))
				.isEqualTo(12);
		assertThat(logsCaptor.getLogsForLevel(Level.ERROR)).isEmpty();
		logsCaptor.clear();

		assertThat(root.call(read(new HjsonReader(new HjsonLexer(new StringReader(
				"{first: {second: 13}}"
		))))))
				.extracting(map -> (Map<String, Object>) map.get("first"))
				.extracting(map -> (Integer) map.get("second"))
				.isEqualTo(-1);
		assertThat(logsCaptor.getLogsForLevel(Level.ERROR)).singleElement()
				.extracting(ILoggingEvent::getMessage)
				.asInstanceOf(STRING)
				.contains("first.second");
	}

	private static class EvenIntReader implements TweedEntryReaderWriter<Integer, ConfigEntry<Integer>> {
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
	}
}
