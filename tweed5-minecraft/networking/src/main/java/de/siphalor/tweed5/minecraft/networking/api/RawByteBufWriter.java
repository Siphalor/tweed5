package de.siphalor.tweed5.minecraft.networking.api;

import de.siphalor.tweed5.serde_api.api.TweedDataWriter;
import de.siphalor.tweed5.serde_api.api.decoration.TweedDataDecoration;
import de.siphalor.tweed5.minecraft.networking.impl.ByteBufSerdeConstants;
import io.netty.buffer.ByteBuf;
import lombok.RequiredArgsConstructor;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
public class RawByteBufWriter implements TweedDataWriter {
	protected final ByteBuf buf;

	@Override
	public void visitNull() {
		buf.writeByte(ByteBufSerdeConstants.NULL_VALUE);
	}

	@Override
	public void visitBoolean(boolean value) {
		if (value) {
			buf.writeByte(ByteBufSerdeConstants.TRUE_VALUE);
		} else {
			buf.writeByte(ByteBufSerdeConstants.FALSE_VALUE);
		}
	}

	@Override
	public void visitByte(byte value) {
		buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT8);
		buf.writeByte(Byte.toUnsignedInt(value));
	}

	@Override
	public void visitShort(short value) {
		buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT16);
		buf.writeShort(Short.toUnsignedInt(value));
	}

	@Override
	public void visitInt(int value) {
		buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT32);
		buf.writeInt(value);
	}

	@Override
	public void visitLong(long value) {
		buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT64);
		buf.writeLong(value);
	}

	@Override
	public void visitFloat(float value) {
		buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_FLOAT);
		buf.writeFloat(value);
	}

	@Override
	public void visitDouble(double value) {
		buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_DOUBLE);
		buf.writeDouble(value);
	}

	@Override
	public void visitString(String value) {
		writeStringBytes(value.getBytes(StandardCharsets.UTF_8));
	}

	protected void writeStringBytes(byte[] bytes) {
		buf.writeByte(ByteBufSerdeConstants.COMPLEX_VARIANT_STRING);
		buf.writeInt(bytes.length);
		buf.writeBytes(bytes);
	}

	@Override
	public void visitListStart() {
		buf.writeByte(ByteBufSerdeConstants.COMPLEX_VARIANT_LIST);
	}

	@Override
	public void visitListEnd() {
		buf.writeByte(ByteBufSerdeConstants.COMPLEX_VARIANT_END);
	}

	@Override
	public void visitMapStart() {
		buf.writeByte(ByteBufSerdeConstants.COMPLEX_VARIANT_MAP);
	}

	@Override
	public void visitMapEntryKey(String key) {
		visitString(key);
	}

	@Override
	public void visitMapEnd() {
		buf.writeByte(ByteBufSerdeConstants.COMPLEX_VARIANT_END);
	}

	@Override
	public void visitDecoration(TweedDataDecoration decoration) {
		// ignored
	}

	@Override
	public void close() {
	}
}
