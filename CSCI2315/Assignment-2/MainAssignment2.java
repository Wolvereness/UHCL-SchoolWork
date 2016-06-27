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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.InputMismatchException;
import java.util.Map.Entry;
import java.util.Scanner;

public class MainAssignment2 {

	/*
	// Comment block-comment start to include lossy functions
	static double findLossyAddedFraction(final int n) {
		return n == 0 ? 0 : findLossyAddedFraction(n - Integer.signum(n)) + 1.0 / n;
	}
	static double findLossyAccurateAddedFraction(final int n, final double v) {
		return n == 0 ? v : findLossyAccurateAddedFraction(n - Integer.signum(n), 1.0 / n + v);
	}
	//*/
	static Entry<BigInteger, BigInteger> findAddedFraction(final int n) {
		if (n < 1)
			throw new IllegalArgumentException("No fraction of 0/0: " + n);
		if (n == 1)
			return new SimpleImmutableEntry<>(BigInteger.ONE, BigInteger.ONE);

		final Entry<BigInteger, BigInteger> entry = findAddedFraction(n - 1);
		final BigInteger
				current = BigInteger.valueOf(n),
				numerator = entry.getKey(),
				denominator = entry.getValue();
		// This is probably not the theoretically best method, as using GCD may be better.
		// However, this method promises only one necessary division, as opposed to many for GCD worst-cases.
		// The denominator's factors may grow faster but,
		// is offset by simplification likeliness growing as well.
		//
		// All things considered, it's still correct, even if the fractions aren't completely simplified.
		final BigInteger[] divisor = denominator.divideAndRemainder(current);
		if (divisor[1].equals(BigInteger.ZERO)) // No remainder
			return new SimpleImmutableEntry<>(numerator.add(divisor[0]), denominator);
		return new SimpleImmutableEntry<>(numerator.multiply(current).add(denominator), denominator.multiply(current));
	}

	static void wrapPyramid(final int count, final StringBuilder string) {
		if (count < 0)
			throw new IllegalArgumentException("Cannot have negative stars: " + count);
		if (count == 0)
			return;
		final StringBuilder repetition = new StringBuilder(count + 1);
		for (int i = count; i > 0; i--) {
			repetition.append('*');
		}
		repetition.append('\n');
		string.insert(0, repetition).append(repetition);
		wrapPyramid(count - 1, string);
	}

	public static void main(String...args) {
		final int precision = args.length == 0 ? 80 : Integer.parseInt(args[0]);

		final Scanner in = new Scanner(System.in);
		boolean section = false;
		while (true) {
			System.out.println(!section
					? "Please provide an integer for the fraction summation (0 to quit):"
					: "Please provide an integer for the pyramid display (-1 to quit):"
					);
			try {
				final int token = in.nextInt();
				if (token == (section ? -1 : 0)) {
					return;
				} else if (token < 0)
					throw new InputMismatchException();

				if (section ^= true) {
					final Entry<BigInteger, BigInteger> value = findAddedFraction(token);
					final BigDecimal
							numerator = new BigDecimal(value.getKey()),
							denominator = new BigDecimal(value.getValue());
					try {
						System.out.println(numerator.divide(denominator).toPlainString());
					} catch (ArithmeticException ex) {
						System.out.println("Inexact decimal representation!");
						System.out.println("Round: " + numerator.divide(denominator, precision, BigDecimal.ROUND_HALF_UP).toPlainString());
					}
					/*
					// Comment the block-comment start to display lossy results
					double v;
					v = findLossyAddedFraction(token);
					System.out.println(" Near: " + v);
					System.out.println("Exact: " + new BigDecimal(v).toPlainString());
					v = findLossyAccurateAddedFraction(token, 0);
					System.out.println(" Near: " + v);
					System.out.println("Exact: " + new BigDecimal(v).toPlainString());
					//*/
				} else {
					final StringBuilder out = new StringBuilder();
					wrapPyramid(token, out);
					System.out.println(out);
				}
			} catch (StackOverflowError err) {
				System.err.println("Recursion sucks for operations trivially optimizable to work from a loop.");
				System.err.println("Your number overflowed the stack.");
			} catch (InputMismatchException ex) {
				System.err.println("Please give a better number.");
			}
		}
	}
}
