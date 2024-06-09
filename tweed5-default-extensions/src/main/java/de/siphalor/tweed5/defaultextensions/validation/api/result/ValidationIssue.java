package de.siphalor.tweed5.defaultextensions.validation.api.result;

import lombok.Value;

@Value
public class ValidationIssue {
	String message;
	ValidationIssueLevel level;
}
