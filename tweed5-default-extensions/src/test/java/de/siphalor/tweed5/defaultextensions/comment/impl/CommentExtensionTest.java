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
import de.siphalor.tweed5.data.extension.impl.ReadWriteExtensionImpl;
import de.siphalor.tweed5.data.hjson.HjsonCommentType;
import de.siphalor.tweed5.data.hjson.HjsonWriter;
import de.siphalor.tweed5.defaultextensions.comment.api.AComment;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentProducer;
import de.siphalor.tweed5.defaultextensions.comment.api.CommentModifyingExtension;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class CommentExtensionTest {

	private DefaultConfigContainer<Map<String, Object>> configContainer;
	private CommentExtension commentExtension;
	private StaticMapCompoundConfigEntryImpl<Map<String, Object>> rootEntry;
	private SimpleConfigEntryImpl<Integer> intEntry;
	private SimpleConfigEntryImpl<String> stringEntry;
	private SimpleConfigEntryImpl<Long> noCommentEntry;

	void setupContainer(Collection<TweedExtension> extraExtensions) {
		configContainer = new DefaultConfigContainer<>();

		commentExtension = new CommentExtension();
		configContainer.registerExtension(commentExtension);
		extraExtensions.forEach(configContainer::registerExtension);
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
		RegisteredExtensionData<EntryExtensionsData, AComment> commentData = (RegisteredExtensionData<EntryExtensionsData, AComment>) configContainer.entryDataExtensions().get(AComment.class);

		commentData.set(rootEntry.extensionsData(), new ACommentImpl("This is the root value.\nIt is the topmost value in the tree."));
		commentData.set(intEntry.extensionsData(), new ACommentImpl("It is an integer"));
		commentData.set(stringEntry.extensionsData(), new ACommentImpl("It is a string"));
	}

	@Test
	void simpleComments() {
		setupContainer(Collections.emptyList());
		configContainer.initialize();

		assertEquals("It is an integer", commentExtension.getComment(intEntry));
		assertEquals("It is a string", commentExtension.getComment(stringEntry));
		assertNull(commentExtension.getComment(noCommentEntry));
	}

	@Test
	void commentProvidingExtension() {
		setupContainer(Collections.singletonList(new TestCommentModifyingExtension()));
		configContainer.initialize();

		assertEquals("The comment is:\nIt is an integer\nEND", commentExtension.getComment(intEntry));
		assertEquals("The comment is:\nIt is a string\nEND", commentExtension.getComment(stringEntry));
		assertEquals("The comment is:\n\nEND", commentExtension.getComment(noCommentEntry));
	}

	@Test
	void simpleCommentsInHjson() {
		ReadWriteExtension readWriteExtension = new ReadWriteExtensionImpl();
		setupContainer(Collections.singletonList(readWriteExtension));
		setupReadWriteTypes();
		configContainer.initialize();

		Map<String, Object> value = new HashMap<>();
		value.put("int", 123);
		value.put("string", "Hello World");
		value.put("noComment", 567L);

		StringWriter output = new StringWriter();
		assertDoesNotThrow(() -> readWriteExtension.write(
				new HjsonWriter(output, new HjsonWriter.Options().multilineCommentType(HjsonCommentType.SLASHES)),
				value,
				rootEntry,
				readWriteExtension.createReadWriteContextExtensionsData()
		));

		assertEquals("// This is the root value.\n// It is the topmost value in the tree.\n" +
				"{\n\t// It is an integer\n\tint: 123\n\t// It is a string\n" +
				"\tstring: Hello World\n\tnoComment: 567\n}\n", output.toString());
	}

	private void setupReadWriteTypes() {
		//noinspection unchecked
		RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition> readerWriterData = (RegisteredExtensionData<EntryExtensionsData, EntryReaderWriterDefinition>) configContainer.entryDataExtensions().get(EntryReaderWriterDefinition.class);

		readerWriterData.set(rootEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.compoundReaderWriter()));
		readerWriterData.set(intEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.intReaderWriter()));
		readerWriterData.set(stringEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.stringReaderWriter()));
		readerWriterData.set(noCommentEntry.extensionsData(), new TrivialEntryReaderWriterDefinition(TweedEntryReaderWriters.longReaderWriter()));
	}

	@Value
	private static class ACommentImpl implements AComment {
		String value;

		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}
	}

	private static class TestCommentModifyingExtension implements TweedExtension, CommentModifyingExtension {
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