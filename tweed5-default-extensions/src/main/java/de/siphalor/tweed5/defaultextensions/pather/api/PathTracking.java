package de.siphalor.tweed5.defaultextensions.pather.api;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class PathTracking implements PatherData {
	private final StringBuilder pathBuilder = new StringBuilder(256);
	private final Deque<Context> contextStack = new ArrayDeque<>(50);
	private final Deque<String> pathParts = new ArrayDeque<>(50);
	private final Deque<Integer> listIndexes = new ArrayDeque<>(10);

	public @Nullable Context currentContext() {
		return contextStack.peek();
	}

	public void popContext() {
		if (contextStack.pop() == Context.LIST) {
			listIndexes.pop();
			popPathPart();
		}
	}

	public void pushMapContext() {
		contextStack.push(Context.MAP);
	}

	public void pushPathPart(String part) {
		pathParts.push(part);
		pathBuilder.append(".").append(part);
	}

	public void popPathPart() {
		if (!pathParts.isEmpty()) {
			String poppedPart = pathParts.pop();
			pathBuilder.setLength(pathBuilder.length() - poppedPart.length() - 1);
		}
	}

	public void pushListContext() {
		contextStack.push(Context.LIST);
		listIndexes.push(0);
		pushPathPart("0");
	}

	public int incrementListIndex() {
		int index = listIndexes.pop() + 1;
		listIndexes.push(index);
		popPathPart();
		pushPathPart(Integer.toString(index));
		return index;
	}

	@Override
	public String valuePath() {
		return pathBuilder.toString();
	}

	public enum Context {
		LIST, MAP,
	}
}
