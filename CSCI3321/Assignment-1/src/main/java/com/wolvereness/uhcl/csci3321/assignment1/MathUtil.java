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
package com.wolvereness.uhcl.csci3321.assignment1;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * This class provides implementations of functions needed to perform
 * calculations.
 */
public class MathUtil {
	/**
	 * Used as a cache for {@link #factorial(int)}
	 */
	private static volatile BigInteger[] FACTORIALS = new BigInteger[] { null, BigInteger.ONE };
	private MathUtil() {}

	/**
	 * <p>This method calculates the cosh using a series expansion.<br>
	 * <img src="http://mathworld.wolfram.com/images/equations/SeriesExpansion/Inline6.gif"
	 * alt="Sourced from wolfram"></p>
	 * <p>This method uses the {@link #factorial(int)} method for demoninators,
	 * and {@link #power(BigDecimal, long)} for exponents.</p>
	 * <p>This expansion ceases execution when the next component in the series is
	 * equal to zero for the given scale.</p>
	 *
	 * @param dec the parameter to use
	 * @param scale the scale to use for division
	 * @param mode the rounding mode for division
	 * @return the calculated value
	 */
	public static BigDecimal cosh(final BigDecimal dec, final int scale, final RoundingMode mode) {
		BigDecimal value = BigDecimal.ONE; // Start at first entry in series expansion
		int nextIndex = 1;
		do {
			final int exponent = nextIndex++ << 1;
			final BigDecimal entry =
				power(dec, exponent) // Current numerator
				.divide(new BigDecimal(factorial(exponent)), scale, mode); // Current denominator
			if (entry.unscaledValue().equals(BigInteger.ZERO))
				break; // We are no longer getting good values
			value = value.add(entry); // Add this one into our previous
		} while (true);

		return value;
	}

	/**
	 * <p>This method returns a number that is the product of all natural
	 * numbers less than or equal to the provided number.</p>
	 * <p>Internally, this method keeps a cache of prior-calculated values for
	 * efficiency reasons. It is still thread-safe.</p>
	 *
	 * @param value the number to use
	 * @return the result
	 * @throws IllegalArgumentException if value &lt;= 0
	 */
	public static BigInteger factorial(final int value) {
		if (value <= 0)
			throw new IllegalArgumentException(value + " <=  0");
		BigInteger[] factorials = FACTORIALS;
		// This loop insures the size of our cache is large enough.
		// Although it creates one that is at-least large enough,
		// we might need to keep trying to get control of the field
		while (factorials.length <= value) {
			synchronized (factorials) { // Multi-threaded concern to reduce CPU consumption
				BigInteger[] refresh = FACTORIALS;
				// Make sure we have the latest copy post-synchronize
				if (refresh != factorials) {
					factorials = refresh;
					// Some computation already happened in another thread; retry logic
					continue;
				}
				refresh = new BigInteger[value + 1]; // Make a new array that is at-least big enough for our value
				System.arraycopy(factorials, 0, refresh, 0, factorials.length); // Preserve all the old calculations
				for (int i = factorials.length; i <= value; i++) { // Start at last calculated value, stop after the one we need
					refresh[i] = BigInteger
						.valueOf(i) // Our current index is the multiplication factor
						.multiply(refresh[i - 1]); // Multiply by the last value
				}
				FACTORIALS = factorials = refresh; // Update the cache and our local variable
				break;
			}
		}
		return factorials[value];
	}

	/**
	 * <p>This method returns the value multiplied by itself a number of times
	 * equal to the power.</p>
	 * <p>Internally, it uses bit-shifting and recursion to only perform
	 * <code>2 * ceil(lg(value))</code>or less multiplications.</p>
	 *
	 * @param value the value to multiply by itself
	 * @param power the number of times to multiply the value
	 * @return the resulting number
	 */
	public static BigDecimal power(final BigDecimal value, final long power) {
		// anything to zero is just 1
		if (power == 0) {
			return BigDecimal.ONE;
		}
		// the 'lowerValue' is our value raised to power / 2.
		BigDecimal lowerValue = power(value, power >>> 1);
		// since it was only raised to the power / 2, we need to square it to compensate for current power
		// Consider:
		// (value^(power/2))^2 ==
		// (value^(power/2)) * (value^(power/2)) ==
		// (value^(power/2 + power/2)) ==
		// value^power
		lowerValue = lowerValue.multiply(lowerValue);
		if ((power & 0x1l) == 0x1l) {
			// This means that the exponent is odd
			// Or, to rephrase, power == floor(power/2) + 1
			// Thus, value^power == value * (value^(floor(power/2)))^2
			return lowerValue.multiply(value);
		} else {
			// This means that the exponent is even
			// Or, to rephrase, power == floor(power/2)
			return lowerValue;
		}
	}
}
