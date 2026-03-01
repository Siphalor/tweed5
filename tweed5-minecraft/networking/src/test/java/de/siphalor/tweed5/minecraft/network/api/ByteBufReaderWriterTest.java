package de.siphalor.tweed5.minecraft.network.api;

import de.siphalor.tweed5.dataapi.api.TweedDataReadException;
import de.siphalor.tweed5.dataapi.api.TweedDataToken;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.minecraft.networking.api.ByteBufReader;
import de.siphalor.tweed5.minecraft.networking.api.RawByteBufWriter;
import de.siphalor.tweed5.minecraft.networking.api.SlightlyCompressedByteBufWriter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;

public class ByteBufReaderWriterTest {
	@ParameterizedTest
	@MethodSource("testParams")
	@SneakyThrows
	void test(Function<ByteBuf, TweedDataWriter> writerConstructor) {
		ByteBuf buffer = Unpooled.buffer();

		try (TweedDataWriter writer = writerConstructor.apply(buffer)) {
			writer.visitMapStart();
			writer.visitMapEntryKey("first");
			writer.visitNull();
			writer.visitMapEntryKey("kind-of a_weird/key!");
			writer.visitListStart();
			writer.visitByte((byte) 12);
			writer.visitByte((byte) -12);
			writer.visitByte((byte) 123);
			writer.visitListEnd();
			writer.visitMapEntryKey("nums");
			writer.visitListStart();
			writer.visitShort((short) 1234);
			writer.visitInt(4321);
			writer.visitInt(Integer.MAX_VALUE);
			writer.visitLong(Long.MAX_VALUE);
			writer.visitFloat(1234.5678f);
			writer.visitDouble(1234.5678);
			writer.visitListEnd();
			writer.visitMapEntryKey("other");
			writer.visitString("Hello World!");
			writer.visitMapEnd();
		}

		System.out.println("Buffer size is: " + buffer.writerIndex());
		assertThat(buffer.readerIndex()).isZero();

		try (ByteBufReader reader = new ByteBufReader(buffer)) {
			assertThat(reader.readToken()).extracting(TweedDataToken::isMapStart).isEqualTo(true);
			assertNextMapKey(reader.readToken(), "first");
			assertThat(reader.readToken()).extracting(TweedDataToken::isNull).isEqualTo(true);
			assertNextMapKey(reader.readToken(), "kind-of a_weird/key!");
			assertThat(reader.readToken()).extracting(TweedDataToken::isListStart).isEqualTo(true);
			assertByteToken(reader.readToken(), (byte) 12);
			assertByteToken(reader.readToken(), (byte) -12);
			assertByteToken(reader.readToken(), (byte) 123);
			assertThat(reader.readToken()).extracting(TweedDataToken::isListEnd).isEqualTo(true);
			assertNextMapKey(reader.readToken(), "nums");
			assertThat(reader.readToken()).extracting(TweedDataToken::isListStart).isEqualTo(true);
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsByte()).isFalse(),
					token -> assertThat(token.canReadAsShort()).isTrue(),
					token -> assertThat(token.readAsShort()).isEqualTo((short) 1234),
					token -> assertThat(token.canReadAsInt()).isTrue(),
					token -> assertThat(token.readAsInt()).isEqualTo(1234),
					token -> assertThat(token.canReadAsLong()).isTrue(),
					token -> assertThat(token.readAsLong()).isEqualTo(1234L)
			);
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsByte()).isFalse(),
					token -> assertThat(token.canReadAsShort()).isTrue(),
					token -> assertThat(token.readAsShort()).isEqualTo((short) 4321),
					token -> assertThat(token.canReadAsInt()).isTrue(),
					token -> assertThat(token.readAsInt()).isEqualTo(4321),
					token -> assertThat(token.canReadAsLong()).isTrue(),
					token -> assertThat(token.readAsLong()).isEqualTo(4321L)
			);
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsByte()).isFalse(),
					token -> assertThat(token.canReadAsShort()).isFalse(),
					token -> assertThat(token.canReadAsInt()).isTrue(),
					token -> assertThat(token.readAsInt()).isEqualTo(Integer.MAX_VALUE),
					token -> assertThat(token.canReadAsLong()).isTrue(),
					token -> assertThat(token.readAsLong()).isEqualTo(Integer.MAX_VALUE)
			);
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsByte()).isFalse(),
					token -> assertThat(token.canReadAsShort()).isFalse(),
					token -> assertThat(token.canReadAsInt()).isFalse(),
					token -> assertThat(token.canReadAsLong()).isTrue(),
					token -> assertThat(token.readAsLong()).isEqualTo(Long.MAX_VALUE)
			);
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsFloat()).isTrue(),
					token -> assertThat(token.readAsFloat()).isEqualTo(1234.5678f)
			);
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsDouble()).isTrue(),
					token -> assertThat(token.readAsDouble()).isEqualTo(1234.5678)
			);
			assertThat(reader.readToken()).extracting(TweedDataToken::isListEnd).isEqualTo(true);
			assertNextMapKey(reader.readToken(), "other");
			assertThat(reader.readToken()).satisfies(
					token -> assertThat(token.canReadAsString()).isTrue(),
					token -> assertThat(token.readAsString()).isEqualTo("Hello World!"),
					token -> assertThat(token.isMapEntryValue()).isTrue()
			);
			assertThat(reader.readToken()).extracting(TweedDataToken::isMapEnd).isEqualTo(true);
			assertThatThrownBy(reader::readToken).isInstanceOf(TweedDataReadException.class);
		}

		buffer.release();
	}

	private void assertNextMapKey(TweedDataToken dataToken, String key) {
		assertThat(dataToken).satisfies(
				token -> assertThat(token.isMapEntryKey()).isTrue(),
				token -> assertThat(token.canReadAsString()).isTrue(),
				token -> assertThat(token.readAsString()).isEqualTo(key)
		);
	}

	private void assertByteToken(TweedDataToken dataToken, byte value) {
		assertThat(dataToken).satisfies(
				token -> assertThat(token.canReadAsByte()).isTrue(),
				token -> assertThat(token.readAsByte()).isEqualTo(value),
				token -> assertThat(token.canReadAsShort()).isTrue(),
				token -> assertThat(token.readAsShort()).isEqualTo(value),
				token -> assertThat(token.canReadAsInt()).isTrue(),
				token -> assertThat(token.readAsInt()).isEqualTo(value),
				token -> assertThat(token.canReadAsLong()).isTrue(),
				token -> assertThat(token.readAsLong()).isEqualTo(value)
		);
	}

	static Stream<Arguments> testParams() {
		return Stream.of(
				argumentSet(
						RawByteBufWriter.class.getSimpleName(),
						((Function<ByteBuf, ?>) RawByteBufWriter::new)
				),
				argumentSet(
						SlightlyCompressedByteBufWriter.class.getSimpleName(),
						((Function<ByteBuf, ?>) SlightlyCompressedByteBufWriter::new)
				)
		);
	}
}
