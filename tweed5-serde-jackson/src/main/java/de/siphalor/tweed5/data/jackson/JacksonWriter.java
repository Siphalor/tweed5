package de.siphalor.tweed5.data.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.dataapi.api.TweedDataWriter;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class JacksonWriter implements TweedDataWriter {
	private final JsonGenerator generator;
	private final CommentWriteMode commentWriteMode;

	private final Deque<Context> contextStack = new ArrayDeque<>();
	private final List<String> deferredFieldComments = new ArrayList<>();

	public JacksonWriter(JsonGenerator generator, CommentWriteMode commentWriteMode) {
		this.generator = generator;
		this.commentWriteMode = commentWriteMode;
		this.contextStack.push(Context.VALUE);
	}

	@Override
	public void visitNull() {
		try {
			generator.writeNull();
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitBoolean(boolean value) {
		try {
			generator.writeBoolean(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitByte(byte value) {
		try {
			generator.writeNumber(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitShort(short value) {
		try {
			generator.writeNumber(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitInt(int value) {
		try {
			generator.writeNumber(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitLong(long value) {
		try {
			generator.writeNumber(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitFloat(float value) {
		try {
			generator.writeNumber(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitDouble(double value) {
		try {
			generator.writeNumber(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitString(String value) {
		try {
			generator.writeString(value);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitListStart() {
		try {
			generator.writeStartArray();
			contextStack.push(Context.LIST);
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitListEnd() {
		try {
			generator.writeEndArray();
			popContext(Context.LIST);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitMapStart() {
		try {
			generator.writeStartObject();
			contextStack.push(Context.MAP);
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitMapEntryKey(String key) {
		try {
			if (!deferredFieldComments.isEmpty()) {
				generator.writeFieldName(key + "__comment");
				if (deferredFieldComments.size() == 1) {
					generator.writeString(deferredFieldComments.get(0));
				} else {
					generator.writeStartArray();
					for (String deferredFieldComment : deferredFieldComments) {
						generator.writeString(deferredFieldComment);
					}
					generator.writeEndArray();
				}
				deferredFieldComments.clear();
			}
			generator.writeFieldName(key);
			contextStack.push(Context.VALUE);
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitMapEnd() {
		try {
			generator.writeEndObject();
			popContext(Context.MAP);
			afterValueVisited();
		} catch (IOException e) {
			throw createWriteExceptionForIOException(e);
		}
	}

	@Override
	public void visitDecoration(TweedDataDecoration decoration) {
		if (decoration instanceof TweedDataCommentDecoration) {
			switch (commentWriteMode) {
				case NONE:
					break;
				case MAP_ENTRIES:
					if (contextStack.peek() == Context.MAP) {
						appendDeferredComment(((TweedDataCommentDecoration) decoration).comment());
					}
					break;
				case DOUBLE_SLASHES:
					try {
						generator.writeRaw("// ");
						generator.writeRaw(((TweedDataCommentDecoration) decoration).comment());
						generator.writeRaw("\n");
					} catch (IOException e) {
						throw createWriteExceptionForIOException(e);
					}
			}
		}
	}

	private void appendDeferredComment(String comment) {
		int index = 0;
		while (true) {
			int next = comment.indexOf('\n', index);
			if (next == -1) {
				deferredFieldComments.add(comment.substring(index));
				break;
			}
			deferredFieldComments.add(comment.substring(index, next));
			index = next + 1;
		}
	}

	@Override
	public void close() throws Exception {
		generator.close();
	}

	private void afterValueVisited() {
		if (contextStack.peek() == Context.VALUE) {
			contextStack.pop();
		}
	}

	private void popContext(Context expectedContext) {
		Context context = contextStack.pop();
		if (context != expectedContext) {
			throw new IllegalStateException("Unexpected context " + context + " when popping " + expectedContext);
		}
	}

	private TweedDataWriteException createWriteExceptionForIOException(IOException e) {
		throw new TweedDataWriteException("Error writing data using jackson at " + generator.getOutputContext(), e);
	}

	public enum CommentWriteMode {
		NONE,
		MAP_ENTRIES,
		DOUBLE_SLASHES,
	}

	private enum Context {
		VALUE,
		LIST,
		MAP,
	}
}
