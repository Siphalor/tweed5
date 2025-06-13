package de.siphalor.tweed5.data.extension.impl;

import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedReadContext;
import de.siphalor.tweed5.data.extension.api.TweedWriteContext;
import de.siphalor.tweed5.patchwork.api.Patchwork;
import lombok.Value;

@Value
public class TweedReadWriteContextImpl implements TweedReadContext, TweedWriteContext {
	ReadWriteExtension readWriteExtension;
	Patchwork extensionsData;
}
