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
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class Quicksort {
	static final Logger log = Logger.getLogger(Quicksort.class.getName());
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
	 * Quicksorts sub-list in-place, minimum first.
	 * Delegates to {@link #foursort(int, int, List, Comparator)} for lists < 4.
	 * Uses pivot selected from middle of list, then
	 * separates elements higher or lower as sub-lists, then
	 * performs same quicksort on each sub-list.
	 *
	 * @param l
	 * @param c
	 */
	static <T> void quicksort(final List<T> l, final Comparator<? super T> c) {
		final ListIterator<Entry<Integer, Integer>> stack = new ArrayList<Entry<Integer, Integer>>().listIterator();
		stack.add(tuple(0, l.size()));
		while (stack.hasPrevious()) {
			final Entry<Integer, Integer> entry = pop(stack);
			final int
				// inclusive
				start = entry.getKey(),
				// exclusive
				end = entry.getValue(),
				size = end - start;
			if (size <= 4) {
				// We really don't need to quicksort on small lists
				foursort(start, end, l, c);
				continue;
			}

			int lSize = 0;
			int rSize = 0;
			final T pivot = l.set(start + size / 2, l.get(start));
			l.set(start, pivot);
			while ((lSize + rSize + 1) != size) {
				// The next left-index
				final int index = start + 1 + lSize;
				final T o = l.get(index);
				if (c.compare(pivot, o) < 0) {
					// Swap to right side
					// Leaves element from right side as next to check
					l.set(index, l.set(end - 1 - rSize++, o));
				} else {
					// Moves the index
					lSize++;
				}
			}
			// Set pivot to correct index.
			// This element will never move,
			// and is skipped for sub-lists being queued
			l.set(start, l.set(start + lSize, pivot));
			// Queue left
			stack.add(tuple(start, start + lSize));
			// Queue right
			stack.add(tuple(start + lSize + 1, end));
		}
	}

	/**
	 * Sorts the specified sub-list in list.
	 * Calls to comparator are minimized, with up to
	 * <ul>
	 * <li>4-5 comparisons for 4 elements,</li>
	 * <li>2-3 comparisons for 3 elements,</li>
	 * <li>1 comparisons for 2 elements, and</li>
	 * <li>0 comparisons for 1 or no element.</li>
	 * </ul>
	 *
	 * @param start inclusive
	 * @param end exclusive
	 * @param l
	 * @param c
	 * @throws IllegalArgumentException for lists larger than 4, or end smaller than start
	 */
	static <T> void foursort(final int start, final int end, final List<T> l, final Comparator<? super T> c) throws IllegalArgumentException {
		final int
			i1 = start + 0,
			i2 = start + 1,
			i3 = start + 2,
			i4 = start + 3;
		switch (end - start) {
			default:
				throw new IllegalArgumentException(String.format(
					"Interval [%s,%s) is not 0 to 4 elements!",
					start,
					end
					));
			case 0:
			case 1:
				return;
			case 2: {
				final T o1 = l.get(start);
				if (c.compare(o1, l.get(i2)) > 0) {
					l.set(start, l.set(i2, o1));
				}
				return;
			}
			case 3: {
				final T o1 = l.get(i1);
				final T o2 = l.get(i2);
				final T o3 = l.get(i3);

				/*
				 * There are 6 permutations of 3 elements.
				 * All 6 cases are covered here,
				 * with up to 3 comparisons to delimit which.
				 */

				if (c.compare(o1, o2) > 0) {     // 2 < 1
					if (c.compare(o2, o3) > 0) {   // 3 < 2, [] = 3, 2, 1
						l.set(i1, o3);
						l.set(i3, o1);
					} else {                       // 2 < 3, [] = 2, (1, 3)
						l.set(i1, o2);
						if (c.compare(o1, o3) > 0) { // 3 < 1, [] = 2, 3, 1
							l.set(i2, o3);
							l.set(i3, o1);
						} else {                     // 1 < 3, [] = 2, 1, 3
							l.set(i2, o1);
						}
					}
				} else {                         // 1 < 2
					if (c.compare(o1, o3) > 0) {   // 3 < 1, [] = 3, 1, 2
						l.set(i1, o3);
						l.set(i2, o1);
						l.set(i3, o2);
					} else {                       // 1 < 3, [] = 1, (2, 3)
						if (c.compare(o2, o3) > 0) { // 3 < 2, [] = 1, 3, 2
							l.set(i2, o3);
							l.set(i3, o2);
						} else {                     // 2 < 3, [] = 1, 2, 3
							// NOOP
						}
					}
				}
				return;
			}
			case 4: {
				final T o1 = l.get(i1);
				final T o2 = l.get(i2);
				final T o3 = l.get(i3);
				final T o4 = l.get(i4);

				/*
				 * There are twenty-four permutations of four elements.
				 * All twenty-four cases are covered here,
				 * with 4 or 5 comparisons to delimit which.
				 *
				 * Certain short-circuits would increase necessary comparisons,
				 * such that, best case 3 comparisons increases worst case to 6.
				 *
				 * Currently, this performs
				 * - an implicit sort on first half
				 * - an implicit sort on second half
				 * - take smallest between halves to put first
				 * - take largest between halves to put last
				 * - a sort on middle two, if not implicitly solved already
				 */

				if (c.compare(o1, o2) > 0) {         // 2 < 1
					if (c.compare(o3, o4) > 0) {       // 4 < 3
						if (c.compare(o2, o4) > 0) {     // 4 < 2, [] = 4, (1, 2, 3)
							l.set(start, o4);
							if (c.compare(o1, o3) > 0) {   // 3 < 1, [] = 4, (2, 3), 1
								l.set(i4, o1);
								if (c.compare(o2, o3) > 0) { // 3 < 2, [] = 4, 3, 2, 1
									l.set(i2, o3);
									l.set(i3, o2);
								} else {                     // 2 < 3, [] = 4, 2, 3, 1
									// NOOP
								}
							} else {                       // 1 < 3, [] = 4, 2, 1, 3
								l.set(i3, o1);
								l.set(i4, o3);
							}
						} else {                         // 2 < 4, [] = 2, (1, 4, 3)
							l.set(start, o2);
							if (c.compare(o1, o3) > 0) {   // 3 < 1, [] = 2, 4, 3, 1
								l.set(i2, o4);
								l.set(i4, o1);
							} else {                       // 1 < 3, [] = 2, (1, 4), 3
								l.set(i4, o3);
								if (c.compare(o1, o4) > 0) { // 4 < 1, [] = 2, 4, 1, 3
									l.set(i2, o4);
									l.set(i3, o1);
								} else {                     // 1 < 4, [] = 2, 1, 4, 3
									l.set(i2, o1);
									l.set(i3, o4);
								}
							}
						}
					} else {                           // 3 < 4
						if (c.compare(o2, o3) > 0) {     // 3 < 2, [] = 3, (1, 2, 4)
							l.set(i1, o3);
							if (c.compare(o1, o4) > 0) {   // 4 < 1, [] = 3, (2, 4), 1
								l.set(i4, o1);
								if (c.compare(o2, o4) > 0) { // 4 < 2, [] = 3, 4, 2, 1
									l.set(i2, o4);
									l.set(i3, o2);
								} else {                     // 2 < 4, [] = 3, 2, 4, 1
									l.set(i3, o4);
								}
							} else {                       // 1 < 4, [] = 3, 2, 1, 4
								l.set(i3, o1);
							}
						} else {                         // 2 < 3, [] = 2, (1, 3, 4)
							l.set(start, o2);
							if (c.compare(o1, o4) > 0) {   // 4 < 1, [] = 2, 3, 4, 1
								l.set(i2, o3);
								l.set(i3, o4);
								l.set(i4, o1);
							} else {                       // 1 < 4, [] = 2, (1, 3), 4
								if (c.compare(o1, o3) > 0) { // 3 < 1, [] = 2, 3, 1, 4
									l.set(i2, o3);
									l.set(i3, o1);
								} else {                     // 1 < 3, [] = 2, 1, 3, 4
									l.set(i2, o1);
									l.set(i3, o3);
								}
							}
						}
					}
				} else {                             // 1 < 2
					if (c.compare(o3, o4) > 0) {       // 4 < 3
						if (c.compare(o1, o4) > 0) {     // 4 < 1, [] = 4, (1, 2, 3)
							l.set(i1, o4);
							if (c.compare(o2, o3) > 0) {   // 3 < 2, [] = 4, (1, 3), 2
								l.set(i4, o2);
								if (c.compare(o1, o3) > 0) { // 3 < 1, [] = 4, 3, 1, 2
									l.set(i2, o3);
									l.set(i3, o1);
								} else {                     // 1 < 3, [] = 4, 1, 3, 2
									l.set(i2, o1);
								}
							} else {                       // 2 < 3, [] = 4, 1, 2, 3
								l.set(i2, o1);
								l.set(i3, o2);
								l.set(i4, o3);
							}
						} else {                         // 1 < 4, [] = 1, (2, 3, 4)
							if (c.compare(o2, o3) > 0) {   // 3 < 2, [] = 1, 4, 3, 2
								l.set(i2, o4);
								l.set(i4, o2);
							} else {                       // 2 < 3, [] = 1, (4, 2), 3
								l.set(i4, o3);
								if (c.compare(o2, o4) > 0) { // 4 < 2, [] = 1, 4, 2, 3
									l.set(i2, o4);
									l.set(i3, o2);
								} else {                     // 2 < 4, [] = 1, 2, 4, 3
									l.set(i3, o4);
								}
							}
						}
					} else {                           // 3 < 4
						if (c.compare(o1, o3) > 0) {     // 3 < 1, [] = 3, (1, 2, 4)
							l.set(i1, o3);
							if (c.compare(o2, o4) > 0) {   // 4 < 2, [] = 3, (1, 4), 2
								l.set(i4, o2);
								if (c.compare(o1, o4) > 0) { // 4 < 1, [] = 3, 4, 1, 2
									l.set(i2, o4);
									l.set(i3, o1);
								} else {                     // 1 < 4, [] = 3, 1, 4, 2
									l.set(i2, o1);
									l.set(i3, o4);
								}
							} else {                       // 2 < 4, [] = 3, 1, 2, 4
								l.set(i2, o1);
								l.set(i3, o2);
							}
						} else {                         // 1 < 3, [] = 1, (2, 3, 4)
							if (c.compare(o2, o4) > 0) {   // 4 < 2, [] = 1, 3, 4, 2
								l.set(i2, o3);
								l.set(i3, o4);
								l.set(i4, o2);
							} else {                       // 2 < 4, [] = 1, (2, 3), 4
								if (c.compare(o2, o3) > 0) { // 3 < 2, [] = 1, 3, 2, 4
									l.set(i2, o3);
									l.set(i3, o2);
								} else {                     // 2 < 3, [] = 1, 2, 3, 4
									// NOOP
								}
							}
						}
					}
				}
			}
		}
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

	static <T> T pop(final ListIterator<? extends T> l) {
		final T o = l.previous();
		l.remove();
		return o;
	}

	/**
	 * Read file, transform lines, sort (quicksort), and return resulting data.
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
		quicksort(read, comparator);
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
		performer = Quicksort::stringPerform; // Default to strings
		for (final Iterator<String> it = Arrays.asList(args).iterator(); it.hasNext(); /* NOOP */) {
			final File fin;
			if (!("--".equals(token) || "-".equals(token))) {
				token = it.next();
				switch (token) {
				case "-s": case "-S":
					performer = Quicksort::stringPerform;
					continue;
				case "-i": case "-I":
				case "-n": case "-N":
				case "-l": case "-L":
					performer = Quicksort::integerPerform;
					continue;
				case "-f": case "-F":
				case "-d": case "-D":
					performer = Quicksort::floatPerform;
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
