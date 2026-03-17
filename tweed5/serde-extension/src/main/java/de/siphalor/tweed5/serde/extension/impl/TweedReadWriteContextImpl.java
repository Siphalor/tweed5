package de.siphalor.tweed5.serde.extension.impl;

import de.siphalor.tweed5.serde.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.serde.extension.api.TweedReadContext;
import de.siphalor.tweed5.serde.extension.api.TweedWriteContext;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import lombok.Value;

@Value
public class TweedReadWriteContextImpl implements TweedReadContext, TweedWriteContext {
	ReadWriteExtension readWriteExtension;
	Patchwork extensionsData;
}
