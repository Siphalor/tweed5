package de.siphalor.tweed5.weaver.pojoext.validation.api;

import de.siphalor.tweed5.core.api.container.ConfigContainer;
import de.siphalor.tweed5.defaultextensions.validation.api.ValidationExtension;
import de.siphalor.tweed5.defaultextensions.validation.api.result.ValidationIssues;
import de.siphalor.tweed5.weaver.pojo.api.TweedPojoWeaver;
import de.siphalor.tweed5.weaver.pojo.api.annotation.*;
import de.siphalor.tweed5.weaver.pojo.impl.weaving.TweedPojoWeaverImpl;
import de.siphalor.tweed5.weaver.pojoext.validation.api.validators.WeavableNumberRangeValidator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ValidatorsPojoWeavingProcessorTest {

	@ParameterizedTest
	@CsvSource({"-1,true", "0,false", "50,false", "99,false", "100,true", "101,true"})
	void test(int value, boolean issuesExpected) {
		ConfigContainer<Config> configContainer = TweedPojoWeaver.forClass(Config.class).weave();
		configContainer.initialize();

		var validationExtension = configContainer.extension(ValidationExtension.class).orElseThrow();

		ValidationIssues issues = validationExtension.validate(configContainer.rootEntry(), new Config(value));
		if (issuesExpected) {
			assertThat(issues.issuesByPath()).as("Issues should be present").isNotEmpty();
		} else {
			assertThat(issues.issuesByPath()).as("No issues should be present").isEmpty();
		}
	}

	@PojoWeaving
	@TweedExtension(ValidationExtension.class)
	@DefaultWeavingExtensions
	@PojoWeavingExtension(ValidatorsPojoWeavingProcessor.class)
	@CompoundWeaving
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Config {
		@Validator(
				value = WeavableNumberRangeValidator.class,
				config = "0=..100"
		)
		private int value;
	}
}
