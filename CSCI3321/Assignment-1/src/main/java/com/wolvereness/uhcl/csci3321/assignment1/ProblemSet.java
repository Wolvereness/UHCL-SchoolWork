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
import java.math.RoundingMode;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This class is a bean representation of the assignment problem's
 * parameters. It includes the {@link #getInput() input value} being used,
 * the {@link #setScale(int) scale} as an accuracy for division, a {@link
 * #setDeltaLimit(BigDecimal) limit} on how many iterations to perform, and
 * a means to {@link #iterator() iterate} over the sequence.
 */
public class ProblemSet implements Iterable<ProblemSet.Iteration> {
	private static final BigDecimal TWO = BigDecimal.valueOf(2);

	/**
	 * This class represents a single iteration for the problem
	 * <blockquote><code>f'(x) ~ (f(x+h) - f(x)) / h</code></blockquote>
	 * where <code>f(x)</code> is <code>cosh(x)</code>, <code>x</code> is
	 * defined by {@link ProblemSet#getInput()}, <code>h</code> is defined
	 * by {@link #getBigDecimalDelta()}.
	 */
	public static class Iteration {
		private final double doubleValue;
		private final BigDecimal bigDecimalValue;
		private final BigDecimal bigDecimalDelta;

		Iteration(final double doubleValue, final BigDecimal bigDecimalValue, final BigDecimal bigDecimalDelta) {
			super();
			this.doubleValue = doubleValue;
			this.bigDecimalValue = bigDecimalValue;
			this.bigDecimalDelta = bigDecimalDelta;
		}

		/**
		 * This returns the value with double precision.
		 *
		 * @return the double-precision value
		 */
		public double getDoubleValue() {
			return doubleValue;
		}

		/**
		 * This returns the value with a BigDecimal of the solution for this
		 * particular iteration.
		 *
		 * @return the bigDecimalValue
		 */
		public BigDecimal getBigDecimalValue() {
			return bigDecimalValue;
		}

		/**
		 * This returns the current &Delta;.
		 *
		 * @return the bigDecimalDelta
		 */
		public BigDecimal getBigDecimalDelta() {
			return bigDecimalDelta;
		}
	}

	private int scale = 100;
	private BigDecimal input = new BigDecimal("0.881373587019543"); // Default, as per assignment instructions
	private BigDecimal deltaLimit = new BigDecimal("1.8E-12"); // Default, as per assignment instructions

	/**
	 * Default constructor. The would-be parameters can just be set
	 * explicitly post-creation.
	 */
	public ProblemSet() {}

	/**
	 * Gets current scale being used. This is for precision on {@link
	 * BigDecimal#divide(BigDecimal, int, java.math.RoundingMode)}
	 * operations of {@link #getInput()}.
	 *
	 * @return the scale being used
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * Sets current scale being used. This is for precision on {@link
	 * BigDecimal#divide(BigDecimal, int, java.math.RoundingMode)}
	 * operations of {@link #getInput()}.
	 *
	 * @param scale the scale to set
	 */
	public void setScale(final int scale) {
		this.scale = scale;
	}

	/**
	 * Gets the input value that will be used for calculations.
	 *
	 * @return the input
	 */
	public BigDecimal getInput() {
		return input;
	}

	/**
	 * Sets the input value that will be used for calculations.
	 *
	 * @param input the input to set
	 * @throws NullPointerException if deltaLimit is null
	 */
	public void setInput(final BigDecimal input) throws NullPointerException {
		if (input == null)
			throw new NullPointerException();
		this.input = input;
	}

	/**
	 * Gets the lowest &Delta; to be used for the calculations.
	 *
	 * @return the deltaLimit
	 */
	public BigDecimal getDeltaLimit() {
		return deltaLimit;
	}

	/**
	 * Sets the lowest &Delta; to be used for the calculations.
	 *
	 * @param deltaLimit the deltaLimit to set
	 * @throws NullPointerException if deltaLimit is null
	 * @throws IllegalArgumentException if deltaLimit is not positive
	 */
	public void setDeltaLimit(final BigDecimal deltaLimit) throws NullPointerException, IllegalArgumentException {
		if (input == null)
			throw new NullPointerException();
		if (deltaLimit.compareTo(BigDecimal.ZERO) <= 0)
			throw new IllegalArgumentException(deltaLimit + " must be a positive number");
		this.deltaLimit = deltaLimit;
	}

	/**
	 * <p>The iterator returned is not affected by future changes to this
	 * problem set.</p>
	 * <p>The iterations begin with a {@link Iteration#getBigDecimalDelta()
	 * &Delta;} of 1, and become subsequently halved each time.
	 * {@link Iterator#hasNext()} will return false before the &Delta;
	 * becomes less than {@link #getDeltaLimit()}.</p>
	 * <p>The problem for each iteration is described in {@link Iteration}.
	 * Both {@link Math#cosh(double)} and {@link MathUtil#cosh(BigDecimal,
	 * int, java.math.RoundingMode)} are used in this implementation,
	 * represented in {@link Iteration#getDoubleValue()} and {@link
	 * Iteration#getBigDecimalValue()} respectively.</p>
	 *
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<Iteration> iterator() {
		return new Iterator<ProblemSet.Iteration>() {
			// Copy the input variables, as ProblemSet is not immutable
			final int scale = ProblemSet.this.getScale();
			final BigDecimal input = ProblemSet.this.getInput();
			final BigDecimal coshInputBig = MathUtil.cosh(input, scale, RoundingMode.HALF_UP);
			final double coshInputDouble = Math.cosh(input.doubleValue());
			final BigDecimal deltaLimit = ProblemSet.this.getDeltaLimit();

			// Keep a reference to current delta
			BigDecimal delta = BigDecimal.ONE;

			@Override
			public boolean hasNext() {
				// Comparable.compareTo behaves as such:
				// this.compareTo(that) == this - that
				// Thus, if delta > deltaLimit,
				// then delta - deltaLimit > 0,
				// then delta.compareTo(deltaLimit) > 0
				// Holds for any other comparator
				return delta.compareTo(deltaLimit) >= 0;
			}

			@Override
			public Iteration next() {
				if (!hasNext())
					throw new NoSuchElementException(delta + " is lower than " + deltaLimit);
				final BigDecimal delta = this.delta; // Keep reference to current delta
				this.delta = delta.divide(TWO); // Decrement our delta
				return new Iteration(
					(Math.cosh(input.doubleValue() + delta.doubleValue()) - coshInputDouble) // Numerator f(x+h) - f(x)
						/ delta.doubleValue(), // Denominator h
					MathUtil.cosh(input.add(delta), scale, RoundingMode.HALF_UP).subtract(coshInputBig) // Numerator f(x+h) - f(x)
						.divide(delta, scale, RoundingMode.HALF_UP), // Denominator h
					delta
					);
			}
		};
	}
}
