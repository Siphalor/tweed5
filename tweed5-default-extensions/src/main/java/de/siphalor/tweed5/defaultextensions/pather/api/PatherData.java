package de.siphalor.tweed5.defaultextensions.pather.api;

/**
 * Extension data for {@link de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData}
 * that provides the path to the value currently being read/written.
 */
public interface PatherData {
	String valuePath();
}
