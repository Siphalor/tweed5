package de.siphalor.tweed5.defaultextensions.validation.api.result;

import de.siphalor.tweed5.core.api.entry.ConfigEntry;
import lombok.Value;

import java.util.Collection;
import java.util.Map;

/**
 * Extension data for {@link de.siphalor.tweed5.data.extension.api.extension.ReadWriteContextExtensionsData}
 * that collects all validation issues.
 */
public interface ValidationIssues {
	Map<String, EntryIssues> issuesByPath();

	@Value
	class EntryIssues {
		ConfigEntry<?> entry;
		Collection<ValidationIssue> issues;
	}
}
