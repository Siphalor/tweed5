package de.siphalor.tweed5.minecraft.networking.api;

import de.siphalor.tweed5.minecraft.networking.impl.ByteBufSerdeConstants;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class SlightlyCompressedByteBufWriter extends RawByteBufWriter {
	public SlightlyCompressedByteBufWriter(ByteBuf buf) {
		super(buf);
	}

	@Override
	public void visitByte(byte value) {
		int v = Byte.toUnsignedInt(value);
		if (v <= 0b11_1111) {
			buf.writeByte(ByteBufSerdeConstants.UINT6_TYPE + (v & 0b11_1111));
		} else {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT8);
			buf.writeByte(v);
		}
	}

	@Override
	public void visitShort(short value) {
		int v = Short.toUnsignedInt(value);
		if (v <= 0b11_1111) {
			writeUint6(v);
		} else if (v <= 0b0111_1111) {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT8);
			buf.writeByte(v & 0b0111_1111);
		} else if (v >= 0b1000_0000_0000_0000 && v <= 0b1000_0000_0111_1111) {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT8);
			buf.writeByte(v & 0b0111_1111 | 0b1000_0000);
		} else {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT16);
			buf.writeShort(v);
		}
	}

	@Override
	public void visitInt(int value) {
		if (value >= 0 && value <= 0b11_1111) {
			writeUint6(value);
		} else if (value >= Byte.MIN_VALUE && value <= Byte.MAX_VALUE) {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT8);
			if (value < 0) value |= 0b1000_0000;
			buf.writeByte(value);
		} else if (value >= Short.MIN_VALUE && value <= Short.MAX_VALUE) {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT16);
			if (value < 0) value |= 0b1000_0000_0000_0000;
			buf.writeShort(value);
		} else {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT32);
			buf.writeInt(value);
		}
	}

	@Override
	public void visitLong(long value) {
		if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
			visitInt((int) value);
		} else {
			buf.writeByte(ByteBufSerdeConstants.VARNUM_VARIANT_INT64);
			buf.writeLong(value);
		}
	}

	private void writeUint6(int value) {
		buf.writeByte(ByteBufSerdeConstants.UINT6_TYPE | (value & 0b11_1111));
	}

	@Override
	public void visitString(String value) {
		if (value.isEmpty()) {
			buf.writeByte(ByteBufSerdeConstants.EMPTY_STRING_VALUE);
			return;
		}
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		if (bytes.length <= 0b100_0000) {
			buf.writeByte(ByteBufSerdeConstants.SMALL_STRING_TYPE | (bytes.length - 1));
			buf.writeBytes(bytes);
		} else {
			writeStringBytes(bytes);
		}
	}
}
