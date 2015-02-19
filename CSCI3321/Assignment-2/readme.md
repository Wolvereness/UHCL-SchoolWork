## PROGRAMMER

Wesley Wolfe

## COURSE

CSCI 3321 Numerical Methods

## DATE

2015-1-18

## ASSIGNMENT

Assignment #2, Secant Method

## ENVIRONMENT

Single file, platform agnostic, requires Java 8

## FILES INCLUDED

* Main - SecantMethod.java

* License -

  * COPYING.txt

  * gpl-3.0.txt

* Description (this file) - Readme.md

## PURPOSE

To examine a solution to the equality:

    x*e^x - 0.01 = 0

using the secant method.

## INPUT

None

## OUTPUT

Output includes iteration number, followed by the current x in f(x),
and then the resulting f(x). When the newly calculated x value is not
closer to 0 than the previous, the line will be appended with this
otherwise hidden value.

## PRECONDITIONS

JDK 8 available on system path

## POSTCONDITIONS

Program contains no system side effects; only the stdout is affected.

## ALGORITHM

From the main method, it creates an instance of SecantMethod and iterates
over the iterations. Each Iteration represents a snapshot of searching
for a zero by an implementation of the secant method (curly represents
subscript):

    x{n+1}     x{n} - ( (x{n-1} - x{n}) / (f(x{n}) - f(x{n-1})) ) * f(x{n})

where n+1 is actually the current x_n, n is defined by x_n_1, and f(exp)
is by f_exp. Passed in is a predicate to indicate when execution should
cease. By default, the main method passes a predicate that checks the
absolute value of f(x) relative to 10^-7. When the distance of f(x) to
zero is less than 10^-7, that iteration of x will be the last.

A new iteration requires either an existing guess, or the prior 
iteration. Each iteration will always consider the value closer to zero
to be x{n}. The value x{n-1} will not be included in a subsequent
execution; it is to be replaced by either x{n} or x{n+1} depending on
which one is further from zero.

This is the Secant Method, as defined by the pseudocode in the textbook;
it may not be canonical Secant Method.

## ERRORS

Output is deterministic; errors are not applicable given a proper system
configuration.

## EXAMPLE

      0: f(+2.000000e+00) = +2.606706e-01 & f(-1.000000e+00) = -2.728282e+00
      1: f(+2.000000e+00) = +2.606706e-01 & f(+1.738366e+00) = +2.956177e-01
      2: f(+3.951529e+00) = +6.596927e-02
      3: f(+4.612752e+00) = +3.577912e-02
      4: f(+5.396384e+00) = +1.446150e-02
      5: f(+5.927986e+00) = +5.791212e-03
      6: f(+6.283064e+00) = +1.734690e-03
      7: f(+6.434905e+00) = +3.251967e-04
      8: f(+6.469938e+00) = +2.401651e-05
      9: f(+6.472731e+00) = +3.689732e-07
     10: f(+6.472775e+00) = +4.275902e-10
