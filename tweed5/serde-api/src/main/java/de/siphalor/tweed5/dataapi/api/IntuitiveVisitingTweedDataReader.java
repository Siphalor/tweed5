package de.siphalor.tweed5.dataapi.api;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class IntuitiveVisitingTweedDataReader {
	private final TweedDataVisitor visitor;

	public void readValue(TweedDataReader reader) throws TweedDataReadException {
		TweedDataToken token = reader.peekToken();
		if (token.isNull()) {
			reader.readToken();
			visitor.visitNull();
		} else if (token.isListStart()) {
			readList(reader);
		} else if (token.isMapStart()) {
			readMap(reader);
		} else if (token.canReadAsByte()) {
			visitor.visitByte(reader.readToken().readAsByte());
		} else if (token.canReadAsShort()) {
			visitor.visitShort(reader.readToken().readAsShort());
		} else if (token.canReadAsInt()) {
			visitor.visitInt(reader.readToken().readAsInt());
		} else if (token.canReadAsLong()) {
			visitor.visitLong(reader.readToken().readAsLong());
		} else if (token.canReadAsFloat()) {
			visitor.visitFloat(reader.readToken().readAsFloat());
		} else if (token.canReadAsDouble()) {
			visitor.visitDouble(reader.readToken().readAsDouble());
		} else if (token.canReadAsBoolean()) {
			visitor.visitBoolean(reader.readToken().readAsBoolean());
		} else if (token.canReadAsString()) {
			visitor.visitString(reader.readToken().readAsString());
		}
	}

	public void readList(TweedDataReader reader) throws TweedDataReadException {
		TweedDataToken token = reader.readToken();
		if (!token.isListStart()) {
			throw TweedDataReadException.builder().message("Expected list but got " + token).build();
		}

		visitor.visitListStart();
		while (true) {
			token = reader.peekToken();
			if (token.isListEnd()) {
				visitor.visitListEnd();
				reader.readToken();
				break;
			} else {
				readValue(reader);
			}
		}
	}

	public void readMap(TweedDataReader reader) throws TweedDataReadException {
		TweedDataToken token = reader.readToken();
		if (!token.isMapStart()) {
			throw TweedDataReadException.builder().message("Expected map but got " + token).build();
		}

		visitor.visitMapStart();
		while (true) {
			token = reader.peekToken();
			if (token.isMapEnd()) {
				reader.readToken();
				visitor.visitMapEnd();
				break;
			} else if (token.isMapEntryKey()) {
				visitor.visitMapEntryKey(reader.readToken().readAsString());
				readValue(reader);
			} else {
				throw TweedDataReadException.builder().message("Expected map end or entry key but got " + token).build();
			}
		}
	}
}
