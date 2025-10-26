package de.siphalor.tweed5.core.impl.sort;

import de.siphalor.tweed5.core.api.sort.AcyclicGraphSorter;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class AcyclicGraphSorterTest {

	@Test
	void trivialSort() {
		AcyclicGraphSorter sorter = new AcyclicGraphSorter(2);
		sorter.addEdge(0, 1);
		assertArrayEquals(new int[]{ 0, 1 }, assertDoesNotThrow(sorter::sort));
	}

	@Test
	void sort1() {
		AcyclicGraphSorter sorter = new AcyclicGraphSorter(4);
		sorter.addEdge(2, 1);
		sorter.addEdge(0, 2);

		assertArrayEquals(new int[]{ 0, 2, 3, 1 }, assertDoesNotThrow(sorter::sort));
	}

	@Test
	void sort2() {
		AcyclicGraphSorter sorter = new AcyclicGraphSorter(7);

		sorter.addEdge(3, 0);
		sorter.addEdge(3, 1);
		sorter.addEdge(3, 2);
		sorter.addEdge(4, 3);
		sorter.addEdge(5, 3);
		sorter.addEdge(6, 3);

		assertArrayEquals(new int[]{ 4, 5, 6, 3, 0, 1, 2 }, assertDoesNotThrow(sorter::sort));
	}

	@Test
	void sort3() {
		AcyclicGraphSorter sorter = new AcyclicGraphSorter(8);
		sorter.addEdge(0, 3);
		sorter.addEdge(1, 0);
		sorter.addEdge(1, 5);
		sorter.addEdge(2, 0);
		sorter.addEdge(4, 1);
		sorter.addEdge(4, 5);
		sorter.addEdge(5, 2);
		sorter.addEdge(6, 7);
		sorter.addEdge(7, 2);

		assertArrayEquals(new int[] { 4, 6, 7, 1, 5, 2, 0, 3 }, assertDoesNotThrow(sorter::sort));
	}

	@Test
	void sortErrorCycle() {
		AcyclicGraphSorter sorter = new AcyclicGraphSorter(8);
		sorter.addEdge(0, 6);
		sorter.addEdge(0, 1);
		sorter.addEdge(6, 1);

		sorter.addEdge(2, 3);
		sorter.addEdge(2, 4);
		sorter.addEdge(4, 5);
		sorter.addEdge(5, 2);

		AcyclicGraphSorter.GraphCycleException exception = assertThrows(AcyclicGraphSorter.GraphCycleException.class, sorter::sort);
		assertEquals(Arrays.asList(2, 4, 5), exception.cycleIndeces());
	}

	@Test
	void minimalCycle() {
		AcyclicGraphSorter sorter = new AcyclicGraphSorter(2);
		sorter.addEdge(0, 1);
		sorter.addEdge(1, 0);

		AcyclicGraphSorter.GraphCycleException exception = assertThrows(AcyclicGraphSorter.GraphCycleException.class, sorter::sort);
		assertEquals(Arrays.asList(0, 1), exception.cycleIndeces());
	}
}
