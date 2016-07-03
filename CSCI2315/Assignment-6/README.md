My application has two modes for execution. The secondary mode takes
any number of command line arguments, and will use those arguments as
file names to parse. It will then output 6 files, named the same as
the original, but appended with the number for which sort, and
out.txt. The input file should have the first number be the count of
number entries in the file. The output file puts every entry on its
own line, with an empty-line before listing duplicates.

The primary mode is with no command line arguments. It will give
directed prompts for choosing a sorting technique and the number of
inputs, and then entering all of the inputs. It will then list,
space-delimited, the inputs after having sorted them. It will also
then list, space-delimited, the duplicates found. The duplicates
match frequency with how many times duplicated, but are in no
particular order.

Each sort is implemented according to the associated named algorithm.

Heapsort uses a 2-step heapify (fix-up) deque (fix-down) procedure.
Fix up determines if an appended element should be migrated up the
binary tree. Fix down determines if a prepended element (replacing
the popped element) needs to be migrated down the binary tree.

Mergesort recursively breaks down the elements unto half-lists, until
the size matches 1. After the half-lists are sorted, it will copy the
left list into a buffer, and fill the original list from the left,
moving pointers as elements get pushed.

Insertion Sort reads the array, treating elements to the left of the
current iteration as pre-sorted, and swaps the next elements leftward
until it is in the correct sorted position.

Shell Sort treats the array in stripes, performing insertion sort on
each stripe. This implementation uses stripe-counts in the form of
2^p * 3^q with p and q being natural numbers, as suggested by
Pratt in his 1972 dissertation. Each set of stripes forms a residue
system modulo increment of the indexes, with the final increment 1
(p=q=0) being equivalent to a plain Insertion Sort. The largest
increment less than log2(size) is used first, followed by each
smaller one, in order.

Quick Sort chooses the element in the middle of the list, and uses it
to pivot. Smaller elements are pushed to the left, larger elements to
the right, and the pivot is now in its final position. It then
recursively sorts these sub-lists.

Bubble Sort iterates over the array, backwards, and swaps any two
consecutive elements that are out of order. When no elements have
been swapped, the sort is complete.
