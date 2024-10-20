package de.siphalor.tweed5.namingformat.impl;

import com.google.auto.service.AutoService;
import de.siphalor.tweed5.namingformat.api.NamingFormatProvider;

import static de.siphalor.tweed5.namingformat.api.NamingFormats.*;

@AutoService(NamingFormatProvider.class)
public class DefaultNamingFormatProvider implements NamingFormatProvider {
	@Override
	public void provideNamingFormats(ProvidingContext context) {
		context.registerNamingFormat("camel_case", camelCase());
		context.registerNamingFormat("pascal_case", pascalCase());
		context.registerNamingFormat("kebab_case", kebabCase());
		context.registerNamingFormat("upper_kebab_case", upperKebabCase());
		context.registerNamingFormat("snake_case", snakeCase());
		context.registerNamingFormat("upper_snake_case", upperKebabCase());
		context.registerNamingFormat("space_case", spaceCase());
		context.registerNamingFormat("upper_space_case", upperSpaceCase());
		context.registerNamingFormat("title_case", titleCase());
	}
}
