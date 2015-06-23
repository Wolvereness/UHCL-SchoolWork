/*
 * Copyright (C) 2015  Wesley Wolfe
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class Heapsort {
	static final Logger log = Logger.getLogger(Heapsort.class.getName());
	static final String HELP = "<HELP>\n\n"
		+ "-h -H : display this help\n"
		+ "-d -D :\n"
		+ "-f -F :"
			+ " parse following files as decimals aka floating point\n"
		+ "-n -N :\n"
		+ "-i -I :\n"
		+ "-l -L :"
			+ " parse following files as natural numbers, aka integer or longs\n"
		+ "-s -S :"
			+ " parse following files as plaintext, using Java's default comparator\n"
		+ "-r -R :"
			+ " toggles reversing how the following files are sorted\n"
		+ "       "
			+ " by default, the smallest possible elements will be first, and\n"
		+ "       "
			+ " strings starting with A before strings starting with Z, and\n"
		+ "       "
			+ " strings truncated before strings with more characters\n"
		+ "-  -- :"
			+ " treat all following tokens as literal files\n"
		+ "       "
			+ " Note, this is always pointless, as files require extensions!\n"
		+ "\n"
		+ "Output files will always be named with `.out' prepending the file extension, like\n"
		+ "in>   file.txt\n"
		+ "out>  file.out.txt\n"
		+ "\n"
		+ "Example execution\n"
		+ "params>   -s file.log     -n num1.txt     num2.data     -d num3.txt\n"
		+ "out files>   file.out.log    num1.out.txt num2.out.data    num3.out.txt\n"
		+ "sorted as>   strings         integers     integers         decimals\n"
		+ "\n"
		+ "Parsing files as plaintext is default behavior."
		+ "";

	/**
	 * Sort list in-place using two-step heapify-dequeue.
	 *
	 * @param list
	 */
	static <T> void heapsort(final List<T> l, final Comparator<? super T> c) {
		if (l.size() < 2)
			return;
		heapify(l, c);
		for (int i = l.size() - 1; i > 0; i--) {
			// Position our maximal element after the heap
			// Take last element and position at root for fixing
			swap(0, i, l);
			final int lastInHeap = i - 1;
			int pos = 0;
			// Continue fixing downward until no motion
			// A node with no leaves cannot move
			while (pos != (pos = fixDown(pos, lastInHeap, l, c)));
		}
	}

	/**
	 * Turn arbitrary list into a maximum heap.
	 * For minimal heap, use {@link Comparator#reversed()}.
	 *
	 * @param l
	 * @param c
	 */
	static <T> void heapify(final List<T> l, final Comparator<? super T> c) {
		if (l.size() < 2)
			return;
		// We start with an implicit heap of size 1
		// We then act as if each successive element has been added to list
		for (int i = 1; i < l.size(); i++) {
			int pos = i;
			// Continue fixing upward until no motion, or root
			while (pos != (pos = fixUp(pos, l, c)) && pos != 0);
		}
	}

	/**
	 * Checks the item at the specified index to see if it should move upward.
	 * Useful for insertions.
	 *
	 * @param i index to check
	 * @param l
	 * @param c
	 * @return
	 */
	static <T> int fixUp(final int i, final List<T> l, final Comparator<? super T> c) {
		final int p = parent(i);
		final T parent = l.get(p);
		if (c.compare(parent, l.get(i)) >= 0)
			return i;
		swap(p, i, l);
		return p;
	}

	/**
	 * Check the item at the specified index to see if it should move downward.
	 * Useful for deletions.
	 *
	 * @param i index to check
	 * @param lastIndex the last possible index for leaves
	 * @param l
	 * @param comp
	 * @return
	 */
	static <T> int fixDown(final int i, final int lastIndex, final List<T> l, final Comparator<? super T> comp) {
		final int c1 = child(i);
		if (c1 > lastIndex)
			// We have no leaves
			return i;
		if (c1 == lastIndex) {
			// Only 1 leaf
			if (comp.compare(l.get(i), l.get(c1)) >= 0)
				return i;
			swap(c1, i, l);
			return c1;
		}

		final int c2 = c1 + 1;

		final int c;
		final T child;
		if (comp.compare(l.get(c1), l.get(c2)) > 0) {
			child = l.get(c1);
			c = c1;
		} else {
			child = l.get(c2);
			c = c2;
		}

		if (comp.compare(l.get(i), child) >= 0)
			return i;
		swap(i, c, l);
		return c;
	}

	/**
	 * Calculate index for the parent of specified index
	 *
	 * @param i
	 * @return
	 */
	static int parent(final int i) {
		/*
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
		return (i + 1) / 2 - 1;
	}

	/**
	 * Calculate index for the first/left child of specified index
	 *
	 * @param i
	 * @return
	 */
	static int child(final int i) {
		/*
		 * 0 -> 1
		 * 1 -> 3
		 * 2 -> 5
		 * 3 -> 7
		 * 4 -> 9
		 * 5 -> 11
		 * 6 -> 13
		 * etc.
		 */
		return i * 2 + 1;
	}

	static <T> void swap(final int i, final int j, final List<T> l) {
		l.set(j, l.set(i, l.get(j)));
	}

	static Optional<List<Entry<String, String>>> stringPerform(final File fin) {
		return performTuple(fin, String::compareTo, Object::toString);
	}

	static Optional<List<Entry<BigInteger, String>>> integerPerform(final File fin) {
		return performTuple(fin, BigInteger::compareTo, BigInteger::new);
	}

	static Optional<List<Entry<BigDecimal, String>>> floatPerform(final File fin) {
		return performTuple(fin, BigDecimal::compareTo, BigDecimal::new);
	}

	/**
	 * Wrap tuples around parsed object and original data.
	 * Comparators only work on parsed object, but we write the original back out.
	 *
	 * @param fin
	 * @param comparator
	 * @param mapper
	 * @return
	 */
	static <T> Optional<List<Entry<T, String>>> performTuple(final File fin, final Comparator<? super T> comparator, final Function<? super String, ? extends T> mapper) {
		return perform(fin, Entry.comparingByKey(comparator), (s) -> tuple(mapper.apply(s), s));
	}

	static <L, R> Entry<L, R> tuple(final L l, final R r) {
		return new SimpleEntry<>(l, r);
	}

	/**
	 * Read file, transform lines, sort (heapsort), and return resulting data.
	 *
	 * @param fin file to read
	 * @param comparator sorting descriminator
	 * @param mapper transformer
	 * @return resulting data
	 */
	static <T> Optional<List<T>> perform(final File fin, final Comparator<? super T> comparator, final Function<? super String, ? extends T> mapper) {
		if (!(fin.exists() && fin.canRead())) {
			log.severe(String.format(
				"`%s' points to `%s', but cannot be read as a file",
				fin, fin.getAbsolutePath()
				));
			return empty();
		}
		final List<T> read;
		try (final BufferedReader in = new BufferedReader(new FileReader(fin))) {
			read = new ArrayList<> /* I need to confirm mutability */(in
				.lines()
				.map(mapper)
				.collect(Collectors.toList())
				);
		} catch (final NumberFormatException ex) {
			log.log(
				Level.SEVERE,
				String.format("%s contains invalid data", fin),
				ex
				);
			return empty();
		} catch (final UncheckedIOException | IOException ex) {
			log.log(
				Level.SEVERE,
				String.format("Could not read from %s", fin.getAbsolutePath()),
				ex
				);
			return empty();
		}
		heapsort(read, comparator);
		return of(read);
	}

	public static void main(String... args) {
		if (args.length == 0) {
			log.warning("No arguments specified");
			return;
		}
		boolean reverse = false;
		String token = null;
		Function<File, Optional<? extends List<? extends Entry<?, String>>>> performer;
		performer = Heapsort::stringPerform; // Default to strings
		for (final Iterator<String> it = Arrays.asList(args).iterator(); it.hasNext(); /* NOOP */) {
			final File fin;
			if (!("--".equals(token) || "-".equals(token))) {
				token = it.next();
				switch (token) {
				case "-s": case "-S":
					performer = Heapsort::stringPerform;
					continue;
				case "-i": case "-I":
				case "-n": case "-N":
				case "-l": case "-L":
					performer = Heapsort::integerPerform;
					continue;
				case "-f": case "-F":
				case "-d": case "-D":
					performer = Heapsort::floatPerform;
					continue;
				case "-r": case "-R":
					reverse = !reverse;
					continue;
				case "-": case "--":
					if (!it.hasNext()) {
						warnDangling(token);
					}
					continue;
				case "-h": case "-H":
					log.info(HELP);
					token = null;
					continue;
				default:
					fin = new File(token);
					token = null;
				}
			} else {
				fin = new File(it.next());
			}
			final Optional<File> fout = makeOut(fin);
			if (!fout.isPresent()) {
				log.severe(String.format(
					"`%s' is not a valid filename. Files must have a valid extension.",
					token
					));
				continue;
			}

			final Optional<? extends List<? extends Entry<?, String>>> sorted = performer.apply(fin);
			if (!sorted.isPresent())
				continue;

			try (final PrintWriter out = new PrintWriter(fout.get())) {
				final List<? extends Entry<?, String>> list = sorted.get();
				(reverse
					? IntStream.range(1 - list.size(), 1)
					: IntStream.range(0, list.size())
					)
					.map(Math::abs)
					.<Entry<?, String>>mapToObj(list::get)
					.map(Entry::getValue)
					.forEach(out::println);
			} catch (IOException ex) {
				log.log(
					Level.SEVERE,
					String.format("Could not write to %s", fout.get().getAbsolutePath()),
					ex
					);
			}
		}
		if (!("--".equals(token) || "-".equals(token) || token == null)) {
			warnDangling(token);
		}
	}

	static void warnDangling(String token) {
		log.warning(String.format(
			"Dangling modifier! `%s'",
			token
			));
	}

	private static Optional<File> makeOut(File fin) {
		final String inName = fin.getName();
		final int period = inName.lastIndexOf('.');
		if (period == -1)
			return empty();
		return of(new File(
			fin.getParentFile(),
			inName.substring(0, period) + ".out" + inName.substring(period)
			));
	}
}
