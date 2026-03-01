package de.siphalor.tweed5.minecraft.networking.impl;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class ByteBufSerdeConstants {
	public static final int TYPE_MASK = 0b1111_0000;
	public static final int VALUE_MASK = 0b0000_1111;

	public static final int CONST_TYPE = 0;
	public static final int NULL_VALUE = 0;
	public static final int FALSE_VALUE = 1;
	public static final int TRUE_VALUE = 0b10;
	public static final int EMPTY_STRING_VALUE = 0b11;

	public static final int VARNUM_TYPE = 0b0001_0000;
	public static final int VARNUM_VARIANT_INT8 = VARNUM_TYPE;
	public static final int VARNUM_VARIANT_INT16 = VARNUM_TYPE | 0b0001;
	public static final int VARNUM_VARIANT_INT32 = VARNUM_TYPE | 0b0010;
	public static final int VARNUM_VARIANT_INT64 = VARNUM_TYPE | 0b0011;
	public static final int VARNUM_VARIANT_FLOAT = VARNUM_TYPE | 0b1000;
	public static final int VARNUM_VARIANT_DOUBLE = VARNUM_TYPE | 0b1001;

	public static final int COMPLEX_TYPE = 0b0010_0000;
	public static final int COMPLEX_VARIANT_STRING = COMPLEX_TYPE;
	public static final int COMPLEX_VARIANT_LIST = COMPLEX_TYPE | 0b0001;
	public static final int COMPLEX_VARIANT_MAP = COMPLEX_TYPE | 0b0010;
	public static final int COMPLEX_VARIANT_END = COMPLEX_TYPE | 0b1111;

	// 0b01xx_xxxx is reserved for future use

	public static final int SPECIAL_EMBED_TYPE_MASK = 0b1100_0000;
	public static final int SPECIAL_EMBED_VALUE_MASK = 0b0011_1111;
	public static final int UINT6_TYPE = 0b1000_0000;
	public static final int SMALL_STRING_TYPE = 0b1100_0000;
}
