My program has 2 fundamental functionalities.

 1. My program maintains a shallow frame system for menu navigation.
    This means that at any given point, there is an active State, and
    it has one method to simply return the next state. Processing is
    done during this one method, and it stays shallow - when another
    method needs to parse input, it will instead return that next
    method as the next state. Normally, these would be returned
    explicitly, however, Java does not have method-curry in the
    standard libraries. So, the method is called by name, returning
    an anonymous class (short-handed as a lambda) implementing the
    State interface. The advantage to using shallow frames is simple:
    slightly more complex menu hierarchies, which can otherwise lead
    to complex recursion overflows, are now trivialized. This method
    is also therefor reusable for new menu systems in future
    endeavors.

    Since all of these methods are referred to by name, their names
    are meaningful, descriptive, and plain-english, therefor self-
    documenting.

    Creating the chain of States is bootstrapped in the run method
    defined by Runnable, implemented by an instance of
    MainAssignment1. It then loops "infinitely" until a State does
    not return the next State. This functionality is hidden into the
    start method.

    A Writable class was used to implement a sane, reusable way to
    make output, including output with specific behavior (like
    printing asterisks before the messages).

 2. My program also maintains an immutable AATM class. Since the
    scope of the request "NO GLOBAL VARIABLES ARE ALLOWED" is
    ambiguous, instances are passed as parameters. Operations on the
    AATM class do not modify the instance, but instead create a new
    instance each time (assuming the transaction is valid). This
    subsequently requires the Transaction and DayDate to be immutable
    as well. The AATM also utilizes BigInteger, which means there is
    no practical limit on the size of the currencies (they may well
    exceed 2^64, and is probably close to the order of your system's
    memory, or 2^(2^32), whichever comes first).

    The AATM class' methods have meaningful, descriptive, and plain-
    english names, which are therefor also self-documenting.

    getInstance - instantiates new AATM with no transactions

    withWithdraw - creates a new AATM with a new withdraw transaction

    withDeposit - creates a new AATM with a new deposit transaction

    withInquiry - creates a new AATM with a new inquiry transaction

    getFilteredMessages - creates a list of messages to render to the
    output, discriminating based on the provided filterAndPrint-predicate.
    This method is used to for getting all withdraw, deposit,
    inquiry, or specific date transactions. Those requests are
    properties of the caller, not a responsibility of the AATM,
    therefor abstracted out as a programming best-practice.

    getTransactions - returns a stream of transactions, to process

    getMinimaAfter - returns the minima balance as of a specific
    date, useful for detecting an overdraft

    getBalanceAsOf - returns a balance leading up through a specific
    date
