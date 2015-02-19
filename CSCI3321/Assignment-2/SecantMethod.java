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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.DoublePredicate;
import java.util.function.DoubleUnaryOperator;

/**
 * Represents a use of the Secant Method to find a zero.
 */
public class SecantMethod implements Iterable<SecantMethod.Iteration> {
	/**
	 * Enclosing class for a single iteration of the secant method.
	 */
	static class Iteration {
		/**
		 * Current, f(n)
		 */
		final double f_n;
		/**
		 * Last, f(n-1)
		 */
		final double f_n_1;
		/**
		 * Current, x subscript n
		 */
		final double x_n;
		/**
		 * Last, x subscript n-1
		 */
		final double x_n_1;
		/**
		 * This condition indicates that our point is further,
		 * as in, not closer than our prior value
		 */
		final boolean further;

		/**
		 * Next iteration based on prior
		 * 
		 * @param last the prior iteration to use for the values of x
		 */
		Iteration(final DoubleUnaryOperator function, final Iteration last) {
			double x_n_1 = last.x_n,
				f_n_1 = last.f_n;
			double x_n = last.x_n - (last.x_n_1 - last.x_n) / (last.f_n_1 - last.f_n) * last.f_n ,
				f_n = function.applyAsDouble(x_n);

			if (this.further = Math.abs(f_n) > Math.abs(f_n_1)) {
				double swap;

				swap = f_n;
				f_n = f_n_1;
				f_n_1 = swap;

				swap = x_n;
				x_n = x_n_1;
				x_n_1 = swap;
			}

			this.x_n = x_n;
			this.x_n_1 = x_n_1;
			this.f_n = f_n;
			this.f_n_1 = f_n_1;
		}

		/**
		 * First iteration
		 */
		Iteration(final DoubleUnaryOperator function, final double guess1, final double guess2) {
			double f_n = function.applyAsDouble(guess1),
				x_n = guess1;
			double f_n_1 = function.applyAsDouble(guess2),
				x_n_1 = guess2;

			if (Math.abs(f_n) > Math.abs(f_n_1)) {
				double swap;

				swap = f_n;
				f_n = f_n_1;
				f_n_1 = swap;

				swap = x_n;
				x_n = x_n_1;
				x_n_1 = swap;
			}

			this.further = true;
			this.x_n = x_n;
			this.x_n_1 = x_n_1;
			this.f_n = f_n;
			this.f_n_1 = f_n_1;
		}
	}

	/**
	 * First initial guess
	 */
	final double guess1;
	/**
	 * Second initial guess
	 */
	final double guess2;
	/**
	 * Function for which to find the zero
	 */
	final DoubleUnaryOperator function;
	/**
	 * Condition indicating iterations to cease
	 */
	final DoublePredicate stop;

	/**
	 * 
	 * @param guess1 first guess for finding a zero
	 * @param guess2 second guess for finding a zero
	 * @param function the function for which to find a zero
	 * @param stop the condition, based on the output of the function for the current iteration, to indicate iterations should cease
	 */
	SecantMethod(final double guess1, final double guess2, final DoubleUnaryOperator function, final DoublePredicate stop) {
		this.guess1 = guess1;
		this.guess2 = guess2;
		this.function = function;
		this.stop = stop;
	}

	/**
	 * Runs the secant method until the distance from zero for f(x) &lt; 0.0000001
	 * 
	 * @param args ignored
	 */
	public static void main(final String...args) {
		int i = 0;
		for (final Iteration it
				: new SecantMethod(
					// These are the intial guesses
					-1, 2,
					// This can be any of the three defined functions
					SecantMethod::function1,
					// This is the end-condition,
					// such that when true,
					// it will have no further iterations
					f_n -> Math.abs(f_n) < 0.0000001
					)
				) {
			System.out.println(String.format(
				it.further
					? "%3s: f(%3$+6e) = %2$+6e & f(%5$+6e) = %4$+6e"
					: "%3s: f(%3$+6e) = %2$+6e",
				i++,
				it.f_n, it.x_n,
				it.f_n_1, it.x_n_1
				));
		}
	}

	/**
	 * The function utilizing {@link Math#expm1(double)}, with distributed multiplication by x
	 * 
	 * @param x value to calculate
	 * @return x * e^x - 0.01
	 */
	private static double function1(final double x) {
		return (x * Math.expm1(-x) + x) - 0.01;
	}

	/**
	 * The function utilizing {@link Math#expm1(double)}, by adding 1 back
	 * 
	 * @param x value to calculate
	 * @return x * e^x - 0.01
	 */
	private static double function2(final double x) {
		return x * (Math.expm1(-x) + 1) - 0.01;
	}

	/**
	 * The function utilizing {@link Math#exp(double)}
	 * 
	 * @param x value to calculate
	 * @return x * e^x - 0.01
	 */
	private static double function3(final double x) {
		return x * Math.exp(-x) - 0.01;
	}

	@Override
	public Iterator<Iteration> iterator() {
		return new Iterator<Iteration>() {
			/**
			 * This is updated each time next is returned
			 */
			Iteration last = new Iteration(SecantMethod.this.function, SecantMethod.this.guess1, SecantMethod.this.guess2);
			/**
			 * This is initialized each time hasNext is called after the prior next call;
			 * a next call sets next to null
			 */
			Iteration next = last;

			@Override
			public boolean hasNext() {
				if (this.next != null)
					return true;
				if (this.last == null)
					return false;

				if (SecantMethod.this.stop.test(this.last.f_n)) {
					this.last = null;
					return false;
				}
				this.next = new Iteration(SecantMethod.this.function, this.last);
				return true;
			}

			@Override
			public Iteration next() {
				if (!hasNext())
					throw new NoSuchElementException();

				final Iteration next = this.last = this.next;
				this.next = null;
				return next;
			}
		};
	}
}
