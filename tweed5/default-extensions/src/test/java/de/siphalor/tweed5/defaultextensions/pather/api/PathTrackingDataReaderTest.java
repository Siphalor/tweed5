package de.siphalor.tweed5.defaultextensions.pather.api;

import de.siphalor.tweed5.dataapi.api.TweedDataReader;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.dataapi.api.TweedDataTokens;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PathTrackingDataReaderTest {
	@SneakyThrows
	@Test
	void test() {
		PathTracking pathTracking = PathTracking.create();
		TweedDataReader mockedDelegate = mock(TweedDataReader.class);

		when(mockedDelegate.readToken()).thenReturn(
				TweedDataTokens.getMapStart(),
				TweedDataTokens.asMapEntryKey(new StringToken("key")),
				TweedDataTokens.asMapEntryValue(new StringToken("value")),
				TweedDataTokens.asMapEntryKey(new StringToken("list")),
				TweedDataTokens.asMapEntryValue(TweedDataTokens.getListStart()),
				TweedDataTokens.asListValue(new StringToken("first")),
				TweedDataTokens.asListValue(TweedDataTokens.getMapStart()),
				TweedDataTokens.asListValue(TweedDataTokens.getMapEnd()),
				TweedDataTokens.getListEnd(),
				TweedDataTokens.getMapEnd()
		);

		var reader = new PathTrackingDataReader(mockedDelegate, pathTracking);

		assertThat(reader.readToken()).isEqualTo(TweedDataTokens.getMapStart());
		assertThat(pathTracking.currentPath()).isEqualTo(".$");
		assertThat(reader.readToken().readAsString()).isEqualTo("key");
		assertThat(pathTracking.currentPath()).isEqualTo(".key");
		assertThat(reader.readToken().readAsString()).isEqualTo("value");
		assertThat(reader.readToken().readAsString()).isEqualTo("list");
		assertThat(pathTracking.currentPath()).isEqualTo(".list");
		assertThat(reader.readToken().isListStart()).isTrue();
		assertThat(pathTracking.currentPath()).isEqualTo(".list");
		assertThat(reader.readToken().readAsString()).isEqualTo("first");
		assertThat(reader.readToken().isMapStart()).isTrue();
		assertThat(pathTracking.currentPath()).startsWith(".list.1");
		assertThat(reader.readToken().isMapEnd()).isTrue();
		assertThat(reader.readToken().isListEnd()).isTrue();
		assertThat(pathTracking.currentPath()).isEqualTo(".list");
		assertThat(reader.readToken()).isEqualTo(TweedDataTokens.getMapEnd());
		assertThat(pathTracking.currentPath()).isEqualTo("");
	}

	@RequiredArgsConstructor
	@EqualsAndHashCode
	static class StringToken implements TweedDataToken {
		private final String value;

		@Override
		public boolean canReadAsString() {
			return true;
		}

		@Override
		public String readAsString() {
			return value;
		}
	}
}
