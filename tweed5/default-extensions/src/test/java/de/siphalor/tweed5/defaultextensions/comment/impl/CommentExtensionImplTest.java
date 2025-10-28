package de.siphalor.tweed5.defaultextensions.comment.impl;

import de.siphalor.tweed5.core.api.entry.CompoundConfigEntry;
import de.siphalor.tweed5.core.api.entry.NullableConfigEntry;
import de.siphalor.tweed5.core.api.entry.SimpleConfigEntry;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.NullableConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static de.siphalor.tweed5.data.extension.api.ReadWriteExtension.entryReaderWriter;
import static de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters.*;
import static de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension.baseComment;
import static de.siphalor.tweed5.testutils.generic.MapTestUtils.sequencedMap;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.*;

@NullUnmarked
class CommentExtensionImplTest {

	private DefaultConfigContainer<@NonNull Map<String, Object>> configContainer;
	private CompoundConfigEntry<Map<String, Object>> rootEntry;
	private NullableConfigEntry<Integer> intEntry;
	private SimpleConfigEntry<String> stringEntry;
	private SimpleConfigEntry<Long> noCommentEntry;

	@SafeVarargs
	final void setupContainer(Class<? extends TweedExtension>... extraExtensions) {
		configContainer = new DefaultConfigContainer<>();

		configContainer.registerExtension(CommentExtension.DEFAULT);
		configContainer.registerExtension(ReadWriteExtension.DEFAULT);
		configContainer.registerExtensions(extraExtensions);
		configContainer.finishExtensionSetup();

		intEntry = new NullableConfigEntryImpl<>(
				configContainer, Integer.class,
				new SimpleConfigEntryImpl<>(configContainer, Integer.class)
					.apply(entryReaderWriter(intReaderWriter()))
					.apply(baseComment("It is an integer")))
				.apply(entryReaderWriter(nullableReaderWriter()))
				.apply(baseComment("This is nullable"));
		stringEntry = new SimpleConfigEntryImpl<>(configContainer, String.class)
				.apply(entryReaderWriter(stringReaderWriter()))
				.apply(baseComment("It is a string"));
		noCommentEntry = new SimpleConfigEntryImpl<>(configContainer, Long.class)
				.apply(entryReaderWriter(longReaderWriter()));

		//noinspection unchecked
		rootEntry = new StaticMapCompoundConfigEntryImpl<>(
				configContainer,
				((Class<Map<String, Object>>) (Class<?>) Map.class),
				LinkedHashMap::new,
				sequencedMap(List.of(
						entry("int", intEntry),
						entry("string", stringEntry),
						entry("noComment", noCommentEntry)
				)))
				.apply(entryReaderWriter(compoundReaderWriter()))
				.apply(baseComment("This is the root value.\nIt is the topmost value in the tree."));

		configContainer.attachTree(rootEntry);
	}

	@Test
	void simpleComments() {
		setupContainer();
		configContainer.initialize();

		CommentExtension commentExtension = configContainer.extension(CommentExtension.class).orElseThrow();
		assertEquals("This is nullable", commentExtension.getFullComment(intEntry));
		assertEquals("It is a string", commentExtension.getFullComment(stringEntry));
		assertNull(commentExtension.getFullComment(noCommentEntry));
	}

	@Test
	void commentProvidingExtension() {
		setupContainer(TestCommentModifyingExtension.class);
		configContainer.initialize();

		CommentExtension commentExtension = configContainer.extension(CommentExtension.class).orElseThrow();
		assertEquals("The comment is:\nThis is nullable\nEND", commentExtension.getFullComment(intEntry));
		assertEquals("The comment is:\nIt is a string\nEND", commentExtension.getFullComment(stringEntry));
		assertEquals("The comment is:\n\nEND", commentExtension.getFullComment(noCommentEntry));
	}

	@Test
	void simpleCommentsInHjson() {
		setupContainer();
		configContainer.initialize();

		Map<String, Object> value = new HashMap<>();
		value.put("int", 123);
		value.put("string", "Hello World");
		value.put("noComment", 567L);

		ReadWriteExtension readWriteExtension = configContainer.extension(ReadWriteExtension.class).orElseThrow();
		StringWriter output = new StringWriter();
		assertDoesNotThrow(() -> readWriteExtension.write(
				new HjsonWriter(output, new HjsonWriter.Options().multilineCommentType(HjsonCommentType.SLASHES)),
				value,
				rootEntry,
				readWriteExtension.createReadWriteContextExtensionsData()
		));

		assertEquals(
				"""
				// This is the root value.
				// It is the topmost value in the tree.
				{
				\t// This is nullable
				\t// It is an integer
				\tint: 123
				\t// It is a string
				\tstring: Hello World
				\tnoComment: 567
				}
				""", output.toString());
	}

	@NoArgsConstructor
	public static class TestCommentModifyingExtension implements TweedExtension, CommentModifyingExtension {
		@Override
		public String getId() {
			return "test-extension";
		}

		@Override
		public Middleware<CommentProducer> commentMiddleware() {
			return new Middleware<>() {
				@Override
				public String id() {
					return "test";
				}

				@Override
				public CommentProducer process(CommentProducer inner) {
					return entry -> "The comment is:\n" + inner.createComment(entry) + "\nEND";
				}
			};
		}
	}
}
