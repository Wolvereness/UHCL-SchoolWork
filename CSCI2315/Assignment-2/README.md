For added fractions, my application returns a tuple. The tuple is an
exact representation of the fraction as a numerator and denominator.
It is also stored as BigInteger, as calls on the order of 2*10^4
display factorial growth, overflowing long values at as low as 21!.
Finally, a division to output to a string is attempted for exact
accuracy, usually throwing a rounding exception, and then the first
80 digits after the decimal using `normal' rounding with up-on-5.

When comparing to using naive doubles, those can be observed to have
inaccuracy on the order of O(10^-13), likely much more depending on
the number used. Lossy (naive and accurate) double calculations are
included in block-comments, for comparisons.

The create the tuple, it will recursively call the previous result,
check if the denominator can be reused, and does a fractional-
addition as necessary.

For the asterisk pyramid, my application uses a rolling StringBuilder
as the printable target. The method accepts a StringBuilder and a
count, and will prepend and append a number of asterisks equal to the
count. It will then pass the StringBuilder to itself with decremented
count, resulting in recursively populating the StringBuilder until
count reaches zero.
