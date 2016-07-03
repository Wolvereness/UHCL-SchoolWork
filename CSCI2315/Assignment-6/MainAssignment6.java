/*
 * Copyright (C) 2016  Wesley Wolfe
 * Works provided with supplemented terms, outlined in accompanying
 * documentation, or found at https://github.com/Wolvereness/UHCL-ScholWork
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.function.*;

import static java.lang.System.out;

public class MainAssignment6 {
	public static void main(final String...args) throws Throwable {
		try {
			if (args.length != 0) {
				for (String arg : args) {
					for (int i = 1; i <= 6; i++) {
						final Scanner in = new Scanner(new StringReader(i + " " + new String(Files.readAllBytes(new File(arg).toPath()), "utf8")));
						try (final PrintStream out = new PrintStream(arg + "." + i + ".out.txt")) {
							main(in, out, true);
						}
					}
				}
			} else {
				main(new Scanner(System.in), out, false);
			}
		} catch (InputMismatchException | NegativeArraySizeException | ArrayIndexOutOfBoundsException e) {
			out.println("Please give proper input instead.");
			//e.printStackTrace();
		}
	}

	static void main(final Scanner in, final PrintStream out, final boolean raw) {
		final Consumer<BigInteger[]> method;
		out.print(
				raw ? "" :
				"1. Heapsort\n" +
				"2. Mergesort\n" +
				"3. Insertion Sort\n" +
				"4. Shell sort\n" +
				"5. Quick sort\n" +
				"6. Bubble sort\n" +
				"Please enter number for the sorting method to use: "
				);
		method = MainAssignment6.<Consumer<BigInteger[]>>make(
				Sort::heapSort,
				Sort::mergeSort,
				Sort::insertionSort,
				Sort::shellSort,
				Sort::quickSort,
				Sort::bubbleSort
				)[in.nextInt() - 1];
		out.print(raw ? "" : "Enter the number of integers: ");
		final int size = in.nextInt();
		final BigInteger[] integers = new BigInteger[size];
		out.print(raw ? "" : "Enter the " + integers.length + " integers: ");
		for (int i = size - 1; i >= 0; i--) {
			integers[i] = in.nextBigInteger();
		}
		method.accept(integers);
		int duplicates = 0;
		for (int i = 1; i < size; i++) {
			if (integers[i - 1 - duplicates].equals(integers[i])) {
				duplicates++;
			}
			Sort.swap(integers, i, i - duplicates);
		}
		out.print(raw ? "" : "The resulting array is: ");
		for (int i = 0; i < size - duplicates; i++) {
			out.print(integers[i]);
			out.print(raw ? "\n" : " ");
		}
		out.println();
		out.print(raw ? "" : "The numbers ");
		for (int i = size - duplicates; i < size; i++) {
			out.print(integers[i]);
			out.print(raw ? "\n" : " ");
		}
		out.println(raw ? "" : "were duplicated in the input.");
	}

	static <T> T[] make(T...t) {
		return t;
	}
}

class Sort {
	@FunctionalInterface interface BiIntConsumer {
		void accept(int v1, int v2);
	}

	/**
	 * This is an excessive list, as our max lg(n) is 31...
	 */
	private static final int[] PRATT_INCREMENTS = new int[] { 1, 2, 3, 4, 6, 8, 9, 12, 16, 18, 24, 27, 32, 36, 48, 54, 64, 72, 81, 96, 108, 128, 144, 162, 192, 216, 243, 256, 288, 324, 384, 432, 486, 512, 576, 648, 729, 768, 864, 972, 1024, 1152, 1296, 1458, 1536, 1728, 1944, 2048, 2187, 2304, 2592, 2916, 3072, 3456, 3888, 4096, 4374, 4608, 5184, 5832, 6144, 6561, 6912, 7776, 8192, 8748, 9216, 10368, 11664, 12288, 13122, 13824, 15552, 16384, 17496, 18432, 19683, 20736, 23328, 24576, 26244, 27648, 31104, 32768, 34992, 36864, 39366, 41472, 46656, 49152, 52488, 55296, 59049, 62208, 65536, 69984, 73728, 78732, 82944, 93312, 98304, 104976, 110592, 118098, 124416, 131072, 139968, 147456, 157464, 165888, 177147, 186624, 196608, 209952, 221184, 236196, 248832, 262144, 279936, 294912, 314928, 331776, 354294, 373248, 393216, 419904, 442368, 472392, 497664, 524288, 531441, 559872, 589824, 629856, 663552, 708588, 746496, 786432, 839808, 884736, 944784, 995328, 1048576, 1062882, 1119744, 1179648, 1259712, 1327104, 1417176, 1492992, 1572864, 1594323, 1679616, 1769472, 1889568, 1990656, 2097152, 2125764, 2239488, 2359296, 2519424, 2654208, 2834352, 2985984, 3145728, 3188646, 3359232, 3538944, 3779136, 3981312, 4194304, 4251528, 4478976, 4718592, 4782969, 5038848, 5308416, 5668704, 5971968, 6291456, 6377292, 6718464, 7077888, 7558272, 7962624, 8388608, 8503056, 8957952, 9437184, 9565938, 10077696, 10616832, 11337408, 11943936, 12582912, 12754584, 13436928, 14155776, 14348907, 15116544, 15925248, 16777216, 17006112, 17915904, 18874368, 19131876, 20155392, 21233664, 22674816, 23887872, 25165824, 25509168, 26873856, 28311552, 28697814, 30233088, 31850496, 33554432, 34012224, 35831808, 37748736, 38263752, 40310784, 42467328, 43046721, 45349632, 47775744, 50331648, 51018336, 53747712, 56623104, 57395628, 60466176, 63700992, 67108864, 68024448, 71663616, 75497472, 76527504, 80621568, 84934656, 86093442, 90699264, 95551488, 100663296, 102036672, 107495424, 113246208, 114791256, 120932352, 127401984, 129140163, 134217728, 136048896, 143327232, 150994944, 153055008, 161243136, 169869312, 172186884, 181398528, 191102976, 201326592, 204073344, 214990848, 226492416, 229582512, 241864704, 254803968, 258280326, 268435456, 272097792, 286654464, 301989888, 306110016, 322486272, 339738624, 344373768, 362797056, 382205952, 387420489, 402653184, 408146688, 429981696, 452984832, 459165024, 483729408, 509607936, 516560652, 536870912, 544195584, 573308928, 603979776, 612220032, 644972544, 679477248, 688747536, 725594112, 764411904, 774840978, 805306368, 816293376, 859963392, 905969664, 918330048, 967458816, 1019215872, 1033121304, 1073741824, 1088391168, 1146617856, 1162261467, 1207959552, 1224440064, 1289945088, 1358954496, 1377495072, 1451188224, 1528823808, 1549681956, 1610612736, 1632586752, 1719926784, 1811939328, 1836660096, 1934917632, 2038431744, 2066242608 };
	private static final int[] LG_TABLE = new int[] {0, 9, 1, 10, 13, 21, 2, 29, 11, 14, 16, 18, 22, 25, 3, 30, 8, 12, 20, 28, 15, 17, 24, 7, 19, 27, 23, 6, 26, 5, 4, 31};

	private Sort() { throw new AssertionError(); }

	/*
	Author: Sean Eron Anderson
	Source: https://graphics.stanford.edu/~seander/bithacks.html#IntegerLogDeBruijn
	 */
	private static int fastLg(int value) {
		value |= value >> 1;
		value |= value >> 2;
		value |= value >> 4;
		value |= value >> 8;
		value |= value >> 16;
		return LG_TABLE[(value * 0x07C4ACDD) >>> 27];
	}

	static void heapSort(final BigInteger[] a) {
		// Adapted from previous assignment
		// https://github.com/Wolvereness/UHCL-SchoolWork/blob/master/CSCI3352/Assignment-1/Heapsort.java
		if (a.length < 2)
			return;
		final BiIntConsumer swap = (i1, i2) -> {
			final BigInteger t = a[i1];
			a[i1] = a[i2];
			a[i2] = t;
		};
		final IntBinaryOperator compare = (i1, i2) -> a[i1].compareTo(a[i2]);
		heapify(fixUp(compare, swap), a.length);
		heapDequeue(swap, fixDown(compare, swap), a.length);
	}

	private static void heapDequeue(final BiIntConsumer swap, final IntBinaryOperator fixDown, final int length) {
		for (int i = length - 1; i > 0; i--) {
			// Position our maximal element after the heap
			// Take last element and position at root for fixing
			swap.accept(0, i);
			final int lastInHeap = i - 1;
			int pos = 0;
			// Continue fixing downward until no motion
			// A node with no leaves cannot move
			while (pos != (pos = fixDown.applyAsInt(pos, lastInHeap)));
		}
	}

	private static void heapify(final IntUnaryOperator fixUp, final int length) {
		for (int i = 1; i < length; i++) {
			int pos = i;
			// Continue fixing upward until no motion, or root
			while (pos != (pos = fixUp.applyAsInt(pos)) && pos != 0);
		}
	}

	/**
	 * 0 -> fault (or, -1?)
	 * 1, 2 -> 0
	 * 3, 4 -> 1
	 * 5, 6 -> 2
	 * 7, 8 -> 3
	 * 9, 10 -> 4
	 * 11, 12 -> 5
	 * 13, 14 -> 6
	 * etc.
	 */
	private static int parent(final int i) {
		return (i + 1) / 2 - 1;
	}

	/**
	 * 0 -> 1
	 * 1 -> 3
	 * 2 -> 5
	 * 3 -> 7
	 * 4 -> 9
	 * 5 -> 11
	 * 6 -> 13
	 * etc.
	 */
	private static int child(final int i) {
		return i * 2 + 1;
	}

	private static IntUnaryOperator fixUp(final IntBinaryOperator comparator, final BiIntConsumer swap) {
		return (i) -> {
			final int pi = parent(i);
			if (comparator.applyAsInt(pi, i) >= 0)
				return i;
			swap.accept(pi, i);
			return pi;
		};
	}

	private static IntBinaryOperator fixDown(final IntBinaryOperator comparator, final BiIntConsumer swap) {
		return (i, lastIndex) -> {
			final int c1 = child(i);
			if (c1 > lastIndex)
				// We have no leaves
				return i;
			if (c1 == lastIndex) {
				// Only 1 leaf
				if (comparator.applyAsInt(i, c1) >= 0)
					return i;
				swap.accept(c1, i);
				return c1;
			}

			final int c2 = c1 + 1;
			final int ci = comparator.applyAsInt(c1, c2) > 0 ? c1 : c2;

			if (comparator.applyAsInt(i, ci) >= 0)
				return i;
			swap.accept(ci, i);
			return ci;
		};
	}

	static void quickSort(final BigInteger[] a) {
		quickSort(a, 0, a.length);
	}

	private static void quickSort(final BigInteger[] a, final int s, final int e) {
		// Adapted from previous assignment
		// https://github.com/Wolvereness/UHCL-SchoolWork/blob/master/CSCI3352/Assignment-1/Quicksort.java
		final int size = e - s;
		if (size <= 1)
			return;

		int lSize = 0;
		int rSize = 0;
		final BigInteger pivot = a[s + size / 2];
		a[s + size / 2] = a[s];
		// a[s] = pivot; // This is implicit
		while ((lSize + rSize + 1) != size) {
			// The next left-index
			final int index = s + 1 + lSize;
			final BigInteger o = a[index];
			if (pivot.compareTo(o) < 0) {
				// Swap to right side
				// Leaves element from right side as next to check
				a[index] = a[e - 1 - rSize];
				a[e - 1 - rSize++] = o;
			} else {
				// Moves the index
				lSize++;
			}
		}
		// Set pivot to correct index.
		// This element will never move,
		// and is skipped for sub-lists being queued
		a[s] = a[s + lSize];
		a[s + lSize] = pivot;
		// Queue left
		quickSort(a, s, s + lSize);
		// Queue right
		quickSort(a, s + lSize + 1, e);
	}

	static void mergeSort(final BigInteger[] a) {
		mergeSort(a, new BigInteger[a.length / 2], 0, a.length);
	}

	private static void mergeSort(final BigInteger[] a, final BigInteger[] b, final int s, final int e) {
		if (e - s <= 1)
			return;
		final int cutOff = (e - s) / 2;
		mergeSort(a, b, s, s + cutOff);
		mergeSort(a, b, s + cutOff, e);
		System.arraycopy(a, s, b, 0, cutOff);
		int i1 = 0, i2 = s + cutOff;
		BigInteger v1 = b[i1], v2 = a[i2];
		for (int i = s; i < e; i++) {
			switch (Integer.signum(v1.compareTo(v2))) {
				case -1:
				case 0:
					a[i] = v1;
					if (++i1 == cutOff)
						return;
					v1 = b[i1];
					break;
				case 1:
					a[i] = v2;
					if (++i2 == e) {
						System.arraycopy(b, i1, a, i + 1, cutOff - i1);
						return;
					}
					v2 = a[i2];
					break;
				default:
					throw new AssertionError();
			}
		}
	}

	static void insertionSort(final BigInteger[] a) {
		insertionSort(a, 0, 1);
	}

	private static void insertionSort(final BigInteger[] a, final int start, final int increment) {
		final int m = a.length;
		int sorted = start, i;
		BigInteger v, t;
		while(m > (sorted += increment)) {
			v = a[i = sorted];
			while ((i -= increment) >= start && (t = a[i]).compareTo(v) > 0) {
				a[i + increment] = t;
			}
			a[i + increment] = v;
		}
	}

	static void shellSort(final BigInteger[] a) {
		/*
		O(n * (lg n) * (lg n))

		Increments (2^p 3^q) are complements of

		V. Pratt
		"Shellsort and Sorting Networks"
		February, 1972

		As explained by

		Satterfield, Wade James
		"An investigation of Shellsort"
		September, 1989
		 */
		final int lg = fastLg(a.length);
		final int CONSTANT = 1;
		if (lg == 0)
			return;
		int increment = 0;
		while (PRATT_INCREMENTS[increment++] >= lg * CONSTANT);
		while (increment-- > 0) {
			final int amount = PRATT_INCREMENTS[increment];
			for (int start = 0; start < amount; start++) {
				insertionSort(a, start, amount);
			}
		}
	}

	// a[index[i]] = a[index[i + 1]]
	static void swap(final BigInteger[] a, final int...indexes) {
		if (indexes.length <= 1)
			return;
		BigInteger v, t;
		int
				indexIndex = indexes.length,
				i = indexes[--indexIndex];
		v = a[i];
		do {
			i = indexes[--indexIndex];
			t = a[i];
			a[i] = v;
			v = t;
		} while (indexIndex > 0);
		a[indexes[indexes.length - 1]] = v;
	}

	static void bubbleSort(final BigInteger[] a) {
		boolean swaps = true;
		while (!(swaps ^= true)) {
			for (int i = a.length - 1; i > 0; i--) {
				if (a[i].compareTo(a[i-1]) < 0) {
					swaps |= true;
					final BigInteger t = a[i];
					a[i] = a[i-1];
					a[i-1] = t;
				}
			}
		}
	}
}
