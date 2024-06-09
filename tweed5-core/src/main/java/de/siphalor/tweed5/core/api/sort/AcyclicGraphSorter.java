package de.siphalor.tweed5.core.api.sort;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.*;

public class AcyclicGraphSorter {
	private final int nodeCount;
	private final int wordCount;

	private final BitSet[] outgoingEdges;
	private final BitSet[] incomingEdges;

	public AcyclicGraphSorter(int nodeCount) {
		this.nodeCount = nodeCount;

		BigDecimal[] div = BigDecimal.valueOf(nodeCount)
				.divideAndRemainder(BigDecimal.valueOf(BitSet.WORD_SIZE));
		this.wordCount = div[0].intValue() + (BigDecimal.ZERO.equals(div[1]) ? 0 : 1);

		outgoingEdges = new BitSet[nodeCount];
		incomingEdges = new BitSet[nodeCount];

		for (int i = 0; i < nodeCount; i++) {
			outgoingEdges[i] = BitSet.empty(nodeCount, wordCount);
			incomingEdges[i] = BitSet.empty(nodeCount, wordCount);
		}
	}

	public void addEdge(int from, int to) {
		checkBounds(from);
		checkBounds(to);

		if (from == to) {
			throw new IllegalArgumentException("Edge from and to cannot be the same");
		}

		outgoingEdges[from].set(to);
		incomingEdges[to].set(from);
	}

	private void checkBounds(int index) {
		if (index < 0 || index >= nodeCount) {
			throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + nodeCount);
		}
	}

	public int[] sort() throws GraphCycleException {
		BitSet visited = BitSet.ready(nodeCount, wordCount);
		BitSet.Iterator visitedIter = visited.iterator();

		int lastVisited = -1;

		int[] sortedIndeces = new int[nodeCount];
		int nextSortedIndex = 0;

		while (nextSortedIndex < sortedIndeces.length) {
			if (!visitedIter.next()) {
				BitSet incomingEdge = incomingEdges[visitedIter.index()];
				if (incomingEdge.isEmptyAfterAndNot(visited)) {
					sortedIndeces[nextSortedIndex] = visitedIter.index();
					visitedIter.set();
					lastVisited = visitedIter.index();

					nextSortedIndex++;
				}
			} else if (visitedIter.index() == lastVisited) {
				break;
			}
			if (!visitedIter.hasNext()) {
				if (lastVisited == -1) {
					break;
				}
				visitedIter.restart();
			}
		}

		if (nextSortedIndex < sortedIndeces.length) {
			findCycleAndThrow(visited);
		}

		return sortedIndeces;
	}

	private void findCycleAndThrow(BitSet visited) throws GraphCycleException {
		Deque<Integer> stack = new LinkedList<>();

		BitSet.Iterator visitedIter = visited.iterator();
		while (visitedIter.next()) {
			if (!visitedIter.hasNext()) {
				throw new IllegalStateException("Unable to find unvisited node in cycle detection");
			}
		}

		stack.push(visitedIter.index());

		outer:
		//noinspection InfiniteLoopStatement
		while (true) {
			BitSet leftoverOutgoing = outgoingEdges[stack.getFirst()].andNot(visited);

			BitSet.Iterator outgoingIter = leftoverOutgoing.iterator();
			while (outgoingIter.hasNext()) {
				if (outgoingIter.next()) {
					if (stack.contains(outgoingIter.index())) {
						throw new GraphCycleException(stack.reversed());
					}
					stack.push(outgoingIter.index());
					continue outer;
				}
			}

			visited.set(stack.pop());
		}
	}

	@Getter
	@ToString
	public static class GraphCycleException extends Exception {
		private final Collection<Integer> cycleIndeces;

		public GraphCycleException(Collection<Integer> cycleIndeces) {
			super("Detected illegal cycle in directed graph");
			this.cycleIndeces = cycleIndeces;
		}
	}

	@AllArgsConstructor(access = AccessLevel.PRIVATE)
	private static class BitSet {
		private static final int WORD_SIZE = Long.SIZE;

		private final int bitCount;
		private final int wordCount;
		private long[] words;

		static BitSet ready(int bitCount, int wordCount) {
			return new BitSet(bitCount, wordCount, new long[wordCount]);
		}

		static BitSet empty(int bitCount, int wordCount) {
			return new BitSet(bitCount, wordCount, null);
		}

		private void set(int index) {
			cloneOnWrite();

			int wordIndex = index / WORD_SIZE;
			int innerIndex = index % WORD_SIZE;

			words[wordIndex] |= 1L << innerIndex;
		}

		private void cloneOnWrite() {
			if (words == null) {
				words = new long[wordCount];
			}
		}

		public boolean isEmpty() {
			if (words == null) {
				return true;
			}
			for (long word : words) {
				if (word != 0L) {
					return false;
				}
			}
			return true;
		}

		public BitSet andNot(BitSet mask) {
			if (words == null) {
				return BitSet.empty(bitCount, wordCount);
			}

			BitSet result = BitSet.ready(bitCount, wordCount);
			for (int i = 0; i < words.length; i++) {
				result.words[i] = words[i] & ~mask.words[i];
			}
			return result;
		}

		public boolean isEmptyAfterAndNot(BitSet mask) {
			if (words == null) {
				return true;
			}

			for (int i = 0; i < words.length; i++) {
				long maskWord = mask.words[i];
				long word = words[i];

				if ((word & ~maskWord) != 0) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof BitSet)) return false;
			BitSet bitSet = (BitSet) o;
			if (this.words == null || bitSet.words == null) {
				return this.isEmpty() && bitSet.isEmpty();
			}
			return Objects.deepEquals(words, bitSet.words);
		}

		@Override
		public int hashCode() {
			if (isEmpty()) {
				return 0;
			}
			return Arrays.hashCode(words);
		}

		@Override
		public String toString() {
			if (wordCount == 0) {
				return "";
			}

			StringBuilder sb = new StringBuilder(wordCount * 9);
			int leftBitCount = bitCount;
			if (words == null) {
				for (int i = 0; i < wordCount; i++) {
					sb.repeat("0", Math.min(WORD_SIZE, leftBitCount));
					sb.append(" ");
					leftBitCount -= WORD_SIZE;
				}
			} else {
				for (long word : words) {
					int wordEnd = Math.min(WORD_SIZE, leftBitCount);
					for (int j = 0; j < wordEnd; j++) {
						sb.append((word & 1) == 1 ? "1" : "0");
						word >>>= 1;
					}
					sb.append(" ");
					leftBitCount -= WORD_SIZE;
				}
			}
			return sb.substring(0, sb.length() - 1);
		}

		public Iterator iterator() {
			return new Iterator();
		}

		public class Iterator {
			private int wordIndex = 0;
			private int innerIndex = -1;
			@Getter
			private int index = -1;

			public void restart() {
				wordIndex = 0;
				innerIndex = -1;
				index = -1;
			}

			public boolean hasNext() {
				return index < bitCount - 1;
			}

			public boolean next() {
				innerIndex++;
				if (innerIndex == WORD_SIZE) {
					innerIndex = 0;
					wordIndex++;
				}
				index++;

				if (words == null) {
					return false;
				}
				return (words[wordIndex] & (1L << innerIndex)) != 0L;
			}

			public void set() {
				cloneOnWrite();
				words[wordIndex] |= (1L << innerIndex);
			}
		}
	}
}
