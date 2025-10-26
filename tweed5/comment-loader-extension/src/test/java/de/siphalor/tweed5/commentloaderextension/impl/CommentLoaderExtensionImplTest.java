package de.siphalor.tweed5.commentloaderextension.impl;

import com.google.gson.GsonBuilder;
import de.siphalor.tweed5.commentloaderextension.api.CommentLoaderExtension;
import de.siphalor.tweed5.commentloaderextension.api.CommentPathProcessor;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.data.gson.GsonReader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.siphalor.tweed5.testutils.generic.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;

class CommentLoaderExtensionImplTest {

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	@SneakyThrows
	void test() {
		var configContainer = new DefaultConfigContainer<Map<String, Object>>();
		configContainer.registerExtension(CommentLoaderExtension.class);
		configContainer.finishExtensionSetup();

		var nestedIntEntry = new SimpleConfigEntryImpl<>(configContainer, Integer.class);
		var nestedEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>) (Class) Map.class,
				HashMap::newHashMap,
				sequencedMap(List.of(
						entry("int", nestedIntEntry)
				))
		);
		var intEntry = new SimpleConfigEntryImpl<>(configContainer, Integer.class);
		var rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				(Class<Map<String, Object>>)(Class) Map.class,
				HashMap::newHashMap,
				sequencedMap(List.of(
						entry("nested", nestedEntry),
						entry("int", intEntry)
				))
		);

		configContainer.attachTree(rootEntry);
		configContainer.initialize();

		CommentLoaderExtension extension = configContainer.extension(CommentLoaderExtension.class).orElseThrow();

		// language=json
		var text = """
				{
					"test": {
						"description": "Root comment"
					},
					"test.int.description": "What an int!",
					"test.nested": {
						"description": "Comment for nested entry",
					    "int.description": "A cool nested entry"
					}
				}
				""";
		try (var reader = new GsonReader(new GsonBuilder().create().newJsonReader(new StringReader(text)))) {
			extension.loadComments(
					reader,
					new CommentPathProcessor() {
						private static final String PREFIX_RAW = "test";
						private static final String PREFIX = PREFIX_RAW + ".";
						private static final String SUFFIX = ".description";

						@Override
						public MatchStatus matches(String path) {
							if (path.equals(PREFIX_RAW)) {
								return MatchStatus.MAYBE_DEEPER;
							} else if (path.startsWith(PREFIX)) {
								if (path.endsWith(SUFFIX)) {
									return MatchStatus.YES;
								} else {
									return MatchStatus.MAYBE_DEEPER;
								}
							} else {
								return MatchStatus.NO;
							}
						}

						@Override
						public String process(String path) {
							return path.substring(
									PREFIX.length(),
									Math.max(PREFIX.length(), path.length() - SUFFIX.length())
							);
						}
					}
			);
		}

		CommentExtension commentExtension = configContainer.extension(CommentExtension.class).orElseThrow();

		assertThat(commentExtension.getFullComment(rootEntry)).isEqualTo("Root comment");
		assertThat(commentExtension.getFullComment(nestedEntry)).isEqualTo("Comment for nested entry");
		assertThat(commentExtension.getFullComment(nestedIntEntry)).isEqualTo("A cool nested entry");
		assertThat(commentExtension.getFullComment(intEntry)).isEqualTo("What an int!");
	}
}
