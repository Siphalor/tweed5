package de.siphaolor.tweed5.data.gson;

import com.google.gson.stream.JsonWriter;
import de.siphalor.tweed5.dataapi.api.TweedDataVisitor;
import de.siphalor.tweed5.dataapi.api.TweedDataWriteException;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataCommentDecoration;
import de.siphalor.tweed5.dataapi.api.decoration.TweedDataDecoration;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class GsonWriter implements TweedDataVisitor {
	private final JsonWriter writer;

	private final Deque<Context> contextStack = new ArrayDeque<>();

	private @Nullable String deferredFieldComment;

	public GsonWriter(JsonWriter writer) {
		this.writer = writer;
		this.contextStack.push(Context.VALUE);
	}

	@Override
	public void visitNull() {
		try {
			writer.nullValue();
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitBoolean(boolean value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitByte(byte value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitShort(short value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitInt(int value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitLong(long value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitFloat(float value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitDouble(double value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitString(String value) {
		try {
			writer.value(value);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitListStart() {
		try {
			writer.beginArray();
			contextStack.push(Context.LIST);
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitListEnd() {
		try {
			writer.endArray();
			popContext(Context.LIST);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitMapStart() {
		try {
			writer.beginObject();
			contextStack.push(Context.MAP);
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitMapEntryKey(String key) {
		try {
			if (deferredFieldComment != null) {
				writer.name(key + "__comment");
				writer.value(deferredFieldComment);
				deferredFieldComment = null;
			}
			writer.name(key);
			contextStack.push(Context.VALUE);
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitMapEnd() {
		try {
			writer.endObject();
			popContext(Context.MAP);
			afterValueWritten();
		} catch (IOException e) {
			throw createWriteExceptionFromIoException(e);
		}
	}

	@Override
	public void visitDecoration(TweedDataDecoration decoration) {
		if (decoration instanceof TweedDataCommentDecoration) {
			if (deferredFieldComment == null) {
				deferredFieldComment = ((TweedDataCommentDecoration) decoration).comment();
			} else {
				deferredFieldComment += "\n" + ((TweedDataCommentDecoration) decoration).comment();
			}
		}
	}

	private void afterValueWritten() {
		if (peekContext() == Context.VALUE) {
			contextStack.pop();
		}
	}

	private void popContext(Context expectedContext) {
		Context context = contextStack.pop();
		if (context != expectedContext) {
			throw new TweedDataWriteException("Unexpected context " + context + " when popping " + expectedContext);
		}
	}

	private Context peekContext() {
		Context context = contextStack.peek();
		if (context == null) {
			throw new TweedDataWriteException("Tried to read context but currently not in any context");
		}
		return context;
	}

	private TweedDataWriteException createWriteExceptionFromIoException(IOException e) {
		return new TweedDataWriteException("Error writing data using Gson", e);
	}

	private enum Context {
		VALUE,
		LIST,
		MAP,
	}
}
