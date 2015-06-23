## PROGRAMMER

Wesley Wolfe

## COURSE

CSCI 3352 Advanced Data Structures and Algorithms

## DATE

2015-6-23

## ASSIGNMENT

Assignment #1, Heapsort and Quicksort

## ENVIRONMENT

Single files, platform agnostic, requires Java 8

## FILES INCLUDED

* Main1 - Heapsort.java
* Main2 - Quicksort.java

* License -

  * COPYING.txt

  * gpl-3.0.txt

## OBJECTIVE

To demonstrate understanding of the heapsort/quicksort algorithms

## SCOPE

Sorting simplistic line-delimited file input using the specified algorithm.

## LIMITATIONS

* Files approaching 1-2 billion lines long may cause integer overflow issues.

* Lines approaching 1-2 billion in length may cause integer overflow issues.

* Decimals and natural numbers are not otherwise limited by primitive
containers.

## INPUT

Filenames should be passed to the application via parameters. The files should
be line delimited data points. By default, files are sorted by plaintext. Use
the parameter -h or -H to receive further documentation on specifying the
parsing style.

## PRECONDITIONS

* JDK 8 available on system path

* Files with line delimited data of either whole numbers, strings, or decimal
numbers.

## OUTPUT

No stdout is provided, except where a warning (trailing parameter flag) or an
error (invalid input or IO exceptions) has occured.

## POSTCONDITIONS

A file with .out prepended to the extension is created, and populated with all
data points sorted from lowest to highest, or in reverse if specified.

## ALGORITHM - HEAPSORT

The application reads all lines into a list of tuples (parsed data + original
representation). It then heapifies the list in-place, by including elements at
the bottom of the heap, and shifting them upward when necessary. After all
elements are included in the heap, it begins to dequeue the maximum element,
swapping it to the end of the heap, truncating the heap, and then shifting the
top-most element downward when necessary.

## ALGORITHM - QUICKSORT

The application reads all lines into a list of tuples (parsed data + original
representation). It then performs a quasi-recursive* operation to sort sub-
lists, starting with the entire list. For each sub-list, it chooses the
element in the middle of the sub-list, partitioning the list into elements
greater than (right) or less than (left). It then takes each of those lists
and recursively* sorts them. When the size of the list to be sorted reaches 4
or less entries, it delegates to a method that enumerates all possible
permutations with minimal comparisons.

* Here, normally, recursive would refer to a method calling itself. However,
I removed the recursive call and instead opted to push the local variables to
an explicitly handled stack and looped until the stack was empty.

## ERRORS

This application runs without error in normal use. Exceptions are logged and
displayed when they occur, however, the program will continue parsing input
files.

## EXAMPLE

    javac Quicksort.java Heapsort.java

(no output)
    

    java Quicksort -f numbers.txt -n integers.txt

(no output, see text files created numbers.out.txt, integers.out.txt)
    

    java Heapsort names.txt

(no output, see text file created names.out.txt)
    
