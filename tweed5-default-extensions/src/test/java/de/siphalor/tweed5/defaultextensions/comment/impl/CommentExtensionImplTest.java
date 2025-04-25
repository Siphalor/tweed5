package de.siphalor.tweed5.defaultextensions.comment.impl;

import de.siphalor.tweed5.core.api.extension.EntryExtensionsData;
import de.siphalor.tweed5.core.api.extension.RegisteredExtensionData;
import de.siphalor.tweed5.core.api.extension.TweedExtension;
import de.siphalor.tweed5.core.api.middleware.Middleware;
import de.siphalor.tweed5.core.impl.DefaultConfigContainer;
import de.siphalor.tweed5.core.impl.entry.SimpleConfigEntryImpl;
import de.siphalor.tweed5.core.impl.entry.StaticMapCompoundConfigEntryImpl;
import de.siphalor.tweed5.data.extension.api.EntryReaderWriterDefinition;
import de.siphalor.tweed5.data.extension.api.ReadWriteExtension;
import de.siphalor.tweed5.data.extension.api.TweedEntryReader;
import de.siphalor.tweed5.data.extension.api.TweedEntryWriter;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriter;
import de.siphalor.tweed5.data.extension.api.readwrite.TweedEntryReaderWriters;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.defaultextensions.comment.api.EntryComment;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jspecify.annotations.NullUnmarked;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@NullUnmarked
class CommentExtensionImplTest {

	private DefaultConfigContainer<Map<String, Object>> configContainer;
	private StaticMapCompoundConfigEntryImpl<Map<String, Object>> rootEntry;
	private SimpleConfigEntryImpl<Integer> intEntry;
	private SimpleConfigEntryImpl<String> stringEntry;
	private SimpleConfigEntryImpl<Long> noCommentEntry;

	@SafeVarargs
	final void setupContainer(Class<? extends TweedExtension>... extraExtensions) {
		configContainer = new DefaultConfigContainer<>();

		configContainer.registerExtension(CommentExtension.DEFAULT);
		configContainer.registerExtensions(extraExtensions);
		configContainer.finishExtensionSetup();

		//noinspection unchecked
		rootEntry = new StaticMapCompoundConfigEntryImpl<>(((Class<Map<String, Object>>)(Class<?>) Map.class), LinkedHashMap::new);

		intEntry = new SimpleConfigEntryImpl<>(Integer.class);
		rootEntry.addSubEntry("int", intEntry);
		stringEntry = new SimpleConfigEntryImpl<>(String.class);
		rootEntry.addSubEntry("string", stringEntry);
		noCommentEntry = new SimpleConfigEntryImpl<>(Long.class);
		rootEntry.addSubEntry("noComment", noCommentEntry);

		configContainer.attachAndSealTree(rootEntry);

		//noinspection unchecked
		RegisteredExtensionData<EntryExtensionsData, EntryComment> commentData = (RegisteredExtensionData<EntryExtensionsData, EntryComment>) configContainer.entryDataExtensions().get(EntryComment.class);

		commentData.set(rootEntry.extensionsData(), new CommentImpl("This is the root value.\nIt is the topmost value in the tree."));
		commentData.set(intEntry.extensionsData(), new CommentImpl("It is an integer"));
		commentData.set(stringEntry.extensionsData(), new CommentImpl("It is a string"));
	}

	@Test
	void simpleComments() {
		setupContainer();
		configContainer.initialize();

		CommentExtension commentExtension = configContainer.extension(CommentExtension.class).orElseThrow();
		assertEquals("It is an integer", commentExtension.getFullComment(intEntry));
		assertEquals("It is a string", commentExtension.getFullComment(stringEntry));
		assertNull(commentExtension.getFullComment(noCommentEntry));
	}

	@Test
	void commentProvidingExtension() {
		setupContainer(TestCommentModifyingExtension.class);
		configContainer.initialize();

		CommentExtension commentExtension = configContainer.extension(CommentExtension.class).orElseThrow();
		assertEquals("The comment is:\nIt is an integer\nEND", commentExtension.getFullComment(intEntry));
		assertEquals("The comment is:\nIt is a string\nEND", commentExtension.getFullComment(stringEntry));
		assertEquals("The comment is:\n\nEND", commentExtension.getFullComment(noCommentEntry));
	}

	@Test
	void simpleCommentsInHjson() {
		setupContainer(ReadWriteExtension.DEFAULT);
		setupReadWriteTypes();
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
				\t// It is an integer
				\tint: 123
				\t// It is a string
				\tstring: Hello World
				\tnoComment: 567
				}
				""", output.toString());
	}

	private void setupReadWriteTypes() {
		//noinspection unchecked
		var readerWriterData = (RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition>) configContainer.entryDataExtensions().get(EntryReaderWriterDefinition.class);

		readerWriterData.set(rootEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.compoundReaderWriter()));
		readerWriterData.set(intEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.intReaderWriter()));
		readerWriterData.set(stringEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.stringReaderWriter()));
		readerWriterData.set(noCommentEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.longReaderWriter()));
	}

	@Value
	private static class CommentImpl implements EntryComment {
		String comment;
	}

	@NoArgsConstructor
	public static class TestCommentModifyingExtension implements TweedExtension, CommentModifyingExtension {
		@Override
		public String getId() {
			return "test-extension";
		}

		@Override
		public Middleware<CommentProducer> commentMiddleware() {
			return new Middleware<CommentProducer>() {
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

	@RequiredArgsConstructor
	private static class TrivialEntryReaderWriterDefinition implements EntryReaderWriterDefinition {
		private final TweedEntryReaderWriter<?, ?> readerWriter;

		@Override
		public TweedEntryReader<?, ?> reader() {
			return readerWriter;
		}

		@Override
		public TweedEntryWriter<?, ?> writer() {
			return readerWriter;
		}
	}
}
