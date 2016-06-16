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
import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MainAssignment1 implements Runnable {

	public static void main(String...args) {
		new MainAssignment1(
				System.out::println,
				obj -> Arrays
					.stream(
						String.valueOf(obj)
						.split("\r\n?|\n", -1)
					).map("***** "::concat)
					.forEach(System.out::println),
				new Scanner(System.in)
		).run();
	}

	static OptionalInt parseInt(String token) {
		try {
			return OptionalInt.of(Integer.parseInt(token));
		} catch (NumberFormatException ex) {
			return OptionalInt.empty();
		}
	}

	static Optional<DayDate> parseDate(String token) {
		final Matcher matcher = Pattern.compile("^(\\d?\\d)(\\/?)(\\d\\d)\\2(\\d{4})?$").matcher(token);
		if (!matcher.matches())
			return Optional.empty();
		try {
			final Calendar date = Calendar.getInstance();
			// Throw exception when user enters bad date
			date.setLenient(false);
			// Resetting fields
			date.set(Calendar.HOUR_OF_DAY, date.getActualMinimum(Calendar.HOUR_OF_DAY));
			date.set(Calendar.MINUTE, date.getActualMinimum(Calendar.MINUTE));
			date.set(Calendar.SECOND, date.getActualMinimum(Calendar.SECOND));
			date.set(Calendar.MILLISECOND, date.getActualMinimum(Calendar.MILLISECOND));
			// Set to entered date
			// Should throw exception on bad input, eg day 33 or month 13
			date.set(
					Optional.ofNullable(matcher.group(4)).map(Integer::parseInt).orElse(date.get(Calendar.YEAR)),
					Integer.parseInt(matcher.group(1)) - 1, // Month, human 1-indexed -> computer 0-indexed
					Integer.parseInt(matcher.group(3)) // Day 1-indexed
			);
			return Optional.of(
					new DayDate(
							date.get(Calendar.YEAR),
							date.get(Calendar.MONTH),
							date.get(Calendar.DAY_OF_MONTH)
					)
			);
		} catch (Exception e) {
			// e.printStackTrace();
			return Optional.empty();
		}
	}

	final Writable stdOut;
	final Writable stdErr;
	final Scanner stdIn;

	MainAssignment1(Writable stdOut, Writable stdErr, Scanner stdIn) {
		this.stdOut = stdOut;
		this.stdErr = stdErr;
		this.stdIn = stdIn;
	}

	private String next() {
		return stdIn.nextLine();
	}

	public void run() {
		for (State ignored : start(AATM.getInstance()));
	}

	State startMenu(final AATM aatm) {
		return () -> {
			stdOut.println(
					"1. Simple Transaction/Inquiry.",
					"2. Complex Transaction/Inquiry.",
					"3. Quit."
					);
			final String token = next();
			switch (parseInt(token).orElse(0)) {
				case 1:
					return simpleMenu(aatm);
				case 2:
					return complexMenu(aatm);
				case 3:
					stdOut.println("Thank-you!", "Goodbye");
					return null;
				default:
					displayBadEntry(token);
					return startMenu(aatm);
			}
		};
	}

	State simpleMenu(final AATM aatm) {
		return () -> {
			stdOut.println(
					"1. Deposit.",
					"2. Withdrawal.",
					"3. Balance Inquiry.",
					"4. Go Back to the Main Menu."
					);
			final String token = next();
			switch (parseInt(token).orElse(0)) {
				case 1:
					return makeDeposit(aatm, this::simpleMenu, Optional.empty(), Optional.empty());
				case 2:
					return makeWithdrawal(aatm, this::simpleMenu, Optional.empty(), Optional.empty());
				case 3:
					return makeInquiry(aatm, this::simpleMenu, Optional.empty());
				case 4:
					return startMenu(aatm);
				default:
					displayBadEntry(token);
					return simpleMenu(aatm);
			}
		};
	}

	State makeDeposit(final AATM aatm, Function<AATM, State> nextState, Optional<DayDate> date, Optional<BigInteger> amount) {
		return () -> {
			if (!amount.isPresent())
				return grabAmount(a -> makeDeposit(aatm, nextState, date, a), "Making a deposit.");
			final BigInteger HUNDRED = BigInteger.valueOf(100);
			final BigInteger[] dollarsCents = amount.get().divideAndRemainder(HUNDRED);
			if (!date.isPresent())
				return grabDate(
						d -> makeDeposit(aatm, nextState, d, amount),
						String.format(
								"Making a %d.%02d deposit on a specific date.",
								dollarsCents[0], dollarsCents[1]
								)
						);
			return nextState.apply(aatm.withDeposit(date.get(), amount.get()).get());
		};
	}

	State makeWithdrawal(final AATM aatm, final Function<AATM, State> nextState, final Optional<DayDate> date, final Optional<BigInteger> amount) {
		return () -> {
			final BigInteger HUNDRED = BigInteger.valueOf(100);
			final BigInteger balance = aatm.getBalanceAsOf(DayDate.getMaxDate());
			final BigInteger[] balanceDollarsCents = balance.divideAndRemainder(HUNDRED);
			if (balance.compareTo(BigInteger.ZERO) <= 0) {
				stdOut.println("Not enough balance to make a withdrawal.");
				return nextState.apply(aatm);
			}
			if (!amount.isPresent()) {
				return grabAmount(
						a -> {
							if (a.isPresent() && a.get().compareTo(balance) > 0) {
								final BigInteger[] token = a.get().divideAndRemainder(HUNDRED);
								displayBadEntry(String.format("%d.%02d", token[0], token[1]));
								return makeWithdrawal(aatm, nextState, date, amount);
							}
							return makeWithdrawal(aatm, nextState, date, a);
						},
						String.format(
								"Total balance available for withdrawal is %d.%02d.",
								balanceDollarsCents[0], balanceDollarsCents[1]
						)
				);
			}
			final BigInteger[] dollarsCentsAmount = amount.get().divideAndRemainder(HUNDRED);
			if (!date.isPresent()) {
				return grabDate(
						d -> makeWithdrawal(aatm, nextState, d, amount),
						String.format("Making %d.%02d withdrawal on a specific date.", dollarsCentsAmount[0], dollarsCentsAmount[1])
						);
			}
			final Map.Entry<DayDate, BigInteger> minima = aatm.getMinimaAfter(date.get());
			if (minima.getValue().compareTo(amount.get()) < 0) {
				final BigInteger[] dollarsCentsOverdraft = minima.getValue().subtract(amount.get()).negate().divideAndRemainder(HUNDRED);
				stdOut.println(String.format(
						"A withdrawal on %d/%02d/%04d of %d.%02d resulted in overdraft of %d.%02d on %d/%02d/%04d.",
						date.get().getMonth() + 1,
						date.get().getDay(),
						date.get().getYear(),
						dollarsCentsAmount[0], dollarsCentsAmount[1],
						dollarsCentsOverdraft[0], dollarsCentsOverdraft[1],
						minima.getKey().getMonth() + 1,
						minima.getKey().getDay(),
						minima.getKey().getYear()
						));
			}
			return nextState.apply(aatm.withWithdraw(date.get(), amount.get()).get());
		};
	}

	State makeInquiry(final AATM aatm, Function<AATM, State> nextState, Optional<DayDate> date) {
		return () -> {
			if (!date.isPresent())
				return grabDate(d -> makeInquiry(aatm, nextState, d), "Making inquiry on a specific date.");
			return nextState.apply(aatm.withInquiry(date.get()).get());
		};
	}

	State complexMenu(final AATM aatm) {
		return () -> {
			stdOut.println(
					"1. Display All Transactions on a Specific Date.",
					"2. Display All the Deposits and Their Dates.",
					"3. Display All the Withdrawal and Their Dates.",
					"4. Display All the Balance Inquiries and Their Dates.",
					"5. Display All the Transactions.",
					"6. Go Back to the Main Menu."
					);
			final String token = next();
			switch (parseInt(token).orElse(0)) {
				case 1:
					return filterForDate(aatm, complexMenu(aatm), Optional.empty());
				case 2:
					return filterAndPrint(
							aatm, complexMenu(aatm),
							t -> t.getType() == AATM.Transaction.Type.DEPOSIT,
							() ->
								{
									stdOut.println("Sorry, but there are no deposits found.");
									return complexMenu(aatm);
								},
							true, true, true
							);
				case 3:
					return filterAndPrint(
							aatm, complexMenu(aatm),
							t -> t.getType() == AATM.Transaction.Type.WITHDRAWAL,
							() ->
								{
									stdOut.println("Sorry, but there are no withdrawals found.");
									return complexMenu(aatm);
								},
							true, true, true
							);
				case 4:
					return filterAndPrint(
							aatm, complexMenu(aatm),
							t -> t.getType() == AATM.Transaction.Type.INQUIRY,
							() ->
								{
									stdOut.println("Sorry, but there are no inquiries found.");
									return complexMenu(aatm);
								},
							true, true, false
							);
				case 5:
					return filterAndPrint(
							aatm, complexMenu(aatm),
							o -> true,
							() ->
								{
									stdOut.println("Sorry, but there are no transactions found.");
									return complexMenu(aatm);
								},
							true, true, true
							);
				case 6:
					return startMenu(aatm);
				default:
					displayBadEntry(token);
					return complexMenu(aatm);
			}
		};
	}

	State filterAndPrint(final AATM aatm, final State nextState, final Predicate<AATM.Transaction> condition, final State failure, boolean includeDate, boolean includeType, boolean includeAmount) {
		return () -> aatm.getFilteredMessages(condition, includeDate, includeType, includeAmount).map(tuple -> print(1, tuple, nextState)).orElse(failure);
	}

	State grabDate(final Function<Optional<DayDate>, State> nextState, String...messages) {
		return () -> {
			stdOut.println((Object[]) messages);
			stdOut.println("Please enter date as MMDD or MDD:");

			final String token = next();
			final Optional<DayDate> date = parseDate(token);
			if (!date.isPresent()) {
				displayBadEntry(token);
				return grabDate(nextState, messages);
			}
			return nextState.apply(date);
		};
	}

	State grabAmount(final Function<Optional<BigInteger>, State> nextState, String...messages) {
		return () -> {
			stdOut.println((Object[]) messages);
			stdOut.println("Please enter amount as dollars with cents like 0.00:");

			final String token = next();
			final Matcher matcher = Pattern.compile("^(\\d+)(?:\\.(\\d)(\\d)?)?$").matcher(token);
			if (!matcher.matches()) {
				displayBadEntry(token);
				return nextState.apply(Optional.empty());
			}
			return nextState.apply(
					Optional.of(
							new BigInteger(
									matcher.group(1)
									+ Optional.ofNullable(matcher.group(2)).orElse("0")
									+ Optional.ofNullable(matcher.group(3)).orElse("0")
									)
							)
					);
		};
	}

	State filterForDate(final AATM aatm, final State nextState, final Optional<DayDate> date) {
		return () -> {
			if (!date.isPresent())
				return grabDate(d -> filterForDate(aatm, nextState, d), "Displaying transactions for a specific date.");
			return filterAndPrint(
					aatm,
					nextState, t -> t.getDate().equals(date.get()),
					() ->
					{
						stdOut.println("Sorry, but there are no transactions on the date specified");
						return nextState;
					},
					true, true, true
					);
		};
	}

	State print(final int page, final Map.Entry<List<String>, List<String>> messages, final State nextState) {
		final int PER_PAGE_LIMIT = 30;
		return () -> {
			final List<String> headers = messages.getKey(), entries = messages.getValue();
			stdOut.println(headers);
			if (entries.size() <= PER_PAGE_LIMIT) {
				stdOut.println(entries);
				return nextState;
			}
			stdOut.println(entries.subList((page - 1) * PER_PAGE_LIMIT, Math.min(page * PER_PAGE_LIMIT, entries.size())));
			stdOut.println();
			stdOut.println(String.format("Page %d of %d", page, (entries.size() - 1) / PER_PAGE_LIMIT + 1));
			return paginate(PER_PAGE_LIMIT, messages, nextState);
		};
	}

	State paginate(final int perPageLimit, final Map.Entry<List<String>, List<String>> messages, final State nextState) {
		return () -> {
			final int pages = (messages.getValue().size() - 1) / perPageLimit + 1, page;
			stdOut.println(
					String.format("Please enter page number from 1 to %d,", pages),
					"Or enter 0 to return:"
					);
			final String token = next();
			page = parseInt(token).orElse(-1);
			if (page == 0)
				return nextState;
			if (page > pages) {
				displayBadEntry(token);
				return paginate(perPageLimit, messages, nextState);
			}
			return print(page, messages, nextState);
		};
	}

	private void displayBadEntry(final String token) {
		stdErr.println(
				"An illegal entry was entered:",
				"",
				token,
				""
				);
	}

	Iterable<State> start(final AATM aatm) {
		return () -> new Iterator<State>() {
			State state = startMenu(aatm);

			@Override
			public boolean hasNext() {
				return state != null;
			}

			@Override
			public State next() {
				final State state = this.state;
				if (!hasNext())
					throw new NoSuchElementException();
				this.state = state.next();
				return state;
			}
		};
	}
}

/**
 * Data class with no built-in verification.
 * The purpose of this class is to store a (year,month,day) tuple
 * with no inclination that it respects a time during that day.
 */
final class DayDate implements Comparable<DayDate> {
	public static int compare(DayDate l, DayDate r) {
		return l.getYear() != r.getYear()
					? Integer.compare(l.getYear(), r.getYear())
				: l.getMonth() != r.getMonth()
					? Integer.compare(l.getMonth(), r.getMonth())
				: Integer.compare(l.getDay(), r.getDay())
				;
	}

	public static DayDate getMaxDate() {
		return new DayDate(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	public static DayDate getMinDate() {
		return new DayDate(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
	}

	private final int year, month, day;

	DayDate(int year, int month, int day) {
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || getClass() != obj.getClass())
			return false;

		final DayDate that = (DayDate) obj;

		return
				this.year == that.year
				&& this.month == that.month
				&& this.day == that.day
				;
	}

	@Override
	public int hashCode() {
		int result = year;
		result = 31 * result + month;
		result = 31 * result + day;
		return result;
	}

	@Override
	public int compareTo(final DayDate that) {
		return compare(this, that);
	}
}

class AATM {
	/**
	 * Data class used only to store the (date,type,amount) tuple.
	 */
	public static final class Transaction {
		public enum Type {
			DEPOSIT("Deposit"),
			WITHDRAWAL("Withdrawal"),
			INQUIRY("Balance Inquiry");

			private final String longName;

			Type(String longName) {
				this.longName = longName;
			}

			public String getLongName() {
				return longName;
			}

			public BigInteger mapAmount(BigInteger amount) {
				switch(this) {
					case DEPOSIT:
						return amount;
					case WITHDRAWAL:
						return amount.negate();
					case INQUIRY:
					default:
						throw new IllegalStateException("Cannot match amount to " + this);
				}
			}
		}

		private final DayDate date;
		private final Type type;
		private final Optional<BigInteger> amount;

		public DayDate getDate() {
			return date;
		}

		public Type getType() {
			return type;
		}

		public Optional<BigInteger> getAmount() {
			return amount;
		}

		Transaction(DayDate date, Type type, Optional<BigInteger> amount) {
			this.date = date;
			this.type = type;
			this.amount = amount;
		}
	}

	public static AATM getInstance() {
		return new AATM(new ArrayList<>());
	}

	private static String pad(String string, String pad, int targetLength, boolean left, boolean balanced) {
		if (string.length() == targetLength)
			return string;
		if (
				string.length() > targetLength
				|| pad.length() == 0
				|| (targetLength - string.length()) % pad.length() != 0
				)
			throw new IllegalArgumentException(String.format("Cannot match %d-characters from `%s' and `%s'", targetLength, string, pad));
		final StringBuilder ret = new StringBuilder(targetLength);
		final int count = (targetLength - string.length()) / pad.length();
		for (int i = balanced ? count / 2 + (left ? 0b1 & count : 0) : left ? count : 0; i > 0; i--) {
			ret.append(pad);
		}
		ret.append(string);
		for (int i = balanced ? count / 2 + (left ? 0 : 0b1 & count) : left ? 0 : count; i > 0; i--) {
			ret.append(pad);
		}
		return ret.toString();
	}

	private final List<Transaction> transactions;

	private AATM(final List<Transaction> transactions) {
		this.transactions = Collections.unmodifiableList(transactions);
	}

	public Stream<Transaction> getTransactions() {
		return transactions.stream();
	}

	public Optional<AATM> withWithdraw(final DayDate date, final BigInteger amount) {
		if (getBalanceAsOf(new DayDate(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE)).compareTo(amount) < 0)
			return Optional.empty();
		final List<Transaction> transactions = new ArrayList<>(this.transactions);
		transactions.add(new Transaction(date, Transaction.Type.WITHDRAWAL, Optional.of(amount)));
		return Optional.of(new AATM(transactions));
	}

	public Map.Entry<DayDate, BigInteger> getMinimaAfter(final DayDate date) {
		final int
				PREV_BEFORE_TARGET        = 0 * 9,
				PREV_AT_TARGET            = 1 * 9,
				PREV_AFTER_TARGET         = 2 * 9,
				PREV_BEFORE_TRANSACTION   = 0 * 3,
				PREV_AT_TRANSACTION       = 1 * 3,
				PREV_AFTER_TRANSACTION    = 2 * 3,
				TRANSACTION_BEFORE_TARGET = 0,
				TRANSACTION_AT_TARGET     = 1,
				TRANSACTION_AFTER_TARGET  = 2;
		BigInteger rollingBalance = BigInteger.ZERO;
		BigInteger lowestBalance = BigInteger.ZERO;
		DayDate previousDate = DayDate.getMinDate();
		DayDate lowestDate = null;
		final Iterator<Transaction> it = transactions.stream().filter(t -> t.getAmount().isPresent()).sorted(Comparator.comparing(Transaction::getDate, DayDate::compare)).iterator();
		while (it.hasNext()) {
			final Transaction transaction = it.next();
			switch(
					(Integer.signum(previousDate.compareTo(date)) + 1) * 9
					+ (Integer.signum(previousDate.compareTo(transaction.getDate())) + 1) * 3
					+ (Integer.signum(transaction.getDate().compareTo(date)) + 1)
					) {
				case PREV_BEFORE_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_AFTER_TARGET:  // p < r, p < a, r > a [ p a r ]
				case PREV_BEFORE_TRANSACTION + PREV_AT_TARGET + TRANSACTION_AFTER_TARGET:      // p < r, p = a, r > a [ p=a r ]
					// We're now passing the target!
					lowestBalance = rollingBalance;
					lowestDate = date;
					previousDate = transaction.getDate();
					break;

				case PREV_BEFORE_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_BEFORE_TARGET: // p < r, p < a, r < a [ p r a ]
				case PREV_BEFORE_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_AT_TARGET:     // p < r, p < a, r = a [ p r=a ]
					// Yet to reach target, but we advanced to a new day.
					previousDate = transaction.getDate();
					break;

				case PREV_BEFORE_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_AFTER_TARGET:   // p < r, p > a, r > a [ a p r ]
					// We advanced to a new day, but we passed the target a while ago.
					// Make sure the lowest is accurate.
					if (lowestBalance.compareTo(rollingBalance) > 0) {
						lowestDate = previousDate;
						lowestBalance = rollingBalance;
					}
					previousDate = transaction.getDate();
					break;

				case PREV_AT_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_BEFORE_TARGET:     // p = r, p < a, r < a [ p=r a ]
				case PREV_AT_TRANSACTION + PREV_AT_TARGET + TRANSACTION_AT_TARGET:             // p = r, p = a, r = a [ p=r=a ]
				case PREV_AT_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_AFTER_TARGET:       // p = r, p > a, r > a [ a p=r ]
					// Another transaction for today but,
					// we only do checks once all transactions for the day are processed.
					break;

				// Fail-fast cases! These violate transitivity.
				case PREV_BEFORE_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_BEFORE_TARGET: // p < r, p > a, r < a
				case PREV_BEFORE_TRANSACTION + PREV_AT_TARGET + TRANSACTION_BEFORE_TARGET:    // p < r, p = a, r < a
				case PREV_BEFORE_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_AT_TARGET:     // p < r, p > a, r = a
				case PREV_BEFORE_TRANSACTION + PREV_AT_TARGET + TRANSACTION_AT_TARGET:        // p < r, p = a, r = a
				case PREV_AT_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_AT_TARGET:        // p = r, p < a, r = a
				case PREV_AT_TRANSACTION + PREV_AT_TARGET + TRANSACTION_AFTER_TARGET:         // p = r, p = a, r < a
				case PREV_AT_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_AT_TARGET:         // p = r, p > a, r = a
				case PREV_AT_TRANSACTION + PREV_AT_TARGET + TRANSACTION_BEFORE_TARGET:        // p = r, p = a, r < a
				case PREV_AT_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_BEFORE_TARGET:     // p = r, p > a, r < a
				case PREV_AT_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_AFTER_TARGET:     // p = r, p < a, r > a

				// Back in time!
				case PREV_AFTER_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_BEFORE_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_AT_TARGET + TRANSACTION_BEFORE_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_BEFORE_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_AT_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_AT_TARGET + TRANSACTION_AT_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_AT_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_BEFORE_TARGET + TRANSACTION_AFTER_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_AT_TARGET + TRANSACTION_AFTER_TARGET:
				case PREV_AFTER_TRANSACTION + PREV_AFTER_TARGET + TRANSACTION_AFTER_TARGET:
				default:
					throw new AssertionError();
			}
			rollingBalance = rollingBalance.add(transaction.getType().mapAmount(transaction.getAmount().get()));
		}
		if (previousDate.compareTo(date) <= 0) {
			lowestBalance = rollingBalance;
			lowestDate = date;
		} else if (lowestBalance.compareTo(rollingBalance) > 0) {
			lowestBalance = rollingBalance;
			lowestDate = previousDate;
		}
		if (lowestDate == null)
			throw new AssertionError();
		return new AbstractMap.SimpleImmutableEntry(lowestDate, lowestBalance);
	}

	public BigInteger getBalanceAsOf(final DayDate date) {
		return
				transactions
						.stream().parallel()
						.filter(transaction -> date.compareTo(transaction.getDate()) >= 0)
						.filter(transaction -> transaction.getAmount().isPresent())
						.map(
								transaction ->
										transaction.getType() == Transaction.Type.WITHDRAWAL
												? transaction.getAmount().get().negate()
										: transaction.getType() == Transaction.Type.DEPOSIT
												? transaction.getAmount().get()
										: null
								)
						.reduce(BigInteger.ZERO, BigInteger::add)
				;
	}

	public Optional<AATM> withDeposit(DayDate date, BigInteger amount) {
		final List<Transaction> transactions = new ArrayList<>(this.transactions);
		transactions.add(new Transaction(date, Transaction.Type.DEPOSIT, Optional.of(amount)));
		return Optional.of(new AATM(transactions));
	}

	public Optional<AATM> withInquiry(DayDate date) {
		final List<Transaction> transactions = new ArrayList<>(this.transactions);
		transactions.add(new Transaction(date, Transaction.Type.INQUIRY, Optional.empty()));
		return Optional.of(new AATM(transactions));
	}

	public Optional<Map.Entry<List<String>, List<String>>> getFilteredMessages(Predicate<Transaction> condition, boolean hasDate, boolean hasType, boolean hasAmount) {
		final String AMOUNT = "Amount";
		final String TRANSACTION = "Transaction";
		final String DATE = pad("Date", " ", "XX/XX/XXXX".length(), false, false);
		final String SPACER = "  ";
		final String CENTS = "00";
		final List<Transaction> transactions = this.transactions.stream().filter(condition).collect(Collectors.toList());
		if (transactions.size() == 0 || !(hasDate || hasAmount || hasType))
			return Optional.empty();

		final int transactionLength = !hasType ? 0 : Math.max(
				TRANSACTION.length(),
				transactions.stream()
						.map(Transaction::getType)
						.map(Transaction.Type::getLongName)
						.mapToInt(String::length)
						.max()
						.getAsInt()
				);
		final int amountLength = !hasAmount ? 0 : Math.max(
				AMOUNT.length(),
				transactions.stream()
						.map(Transaction::getAmount)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.max(BigInteger::compareTo)
						.map(t -> t.toString(10))
						.orElse("")
						.length()
						+ "$ .".length()
				);
		final int totalLength =
				(hasDate ? DATE.length() : 0)
				+ transactionLength
				+ amountLength
				+ (hasDate && hasType && hasAmount ? SPACER.length() * 2 : hasDate ^ hasType ^ hasAmount ? 0 : SPACER.length())
				;
		final StringBuilder amountPlaceholder = new StringBuilder(pad("", "-", amountLength, false, false));
		final String format = (hasDate ? "%1$02d/%2$02d/%3$04d" : "") + (hasDate && hasType ? SPACER : "") + (hasType ? "%4$s" : "");
		return Optional.of(new AbstractMap.SimpleImmutableEntry<>(
				Arrays.asList(
						new StringBuilder(totalLength)
								.append(hasDate ? DATE : "")
								.append(hasDate && (hasType || hasAmount) ? SPACER : "")
								.append(hasType ? pad(TRANSACTION, " ", transactionLength, false, false) : "")
								.append(hasType && hasAmount ? SPACER : "")
								.append(hasAmount ? pad(AMOUNT, " ", amountLength, false, false) : "")
								.toString(),
						new StringBuilder(totalLength)
								.append(hasDate ? pad("", "=", DATE.length(), false, false) : "")
								.append(hasDate && (hasType || hasAmount) ? SPACER : "")
								.append(pad("", "=", transactionLength, false, false))
								.append(hasType && hasAmount ? SPACER : "")
								.append(pad("", "=", amountLength, false, false))
								.toString()
						),
				transactions.stream()
						.map(t ->
								new StringBuilder(totalLength)
									.append(!(hasDate || hasType) ? "" : pad(
											String.format(
													format,
													t.getDate().getMonth() + 1,
													t.getDate().getDay(),
													t.getDate().getYear(),
													t.getType().getLongName()
													),
											" ",
											(hasDate ? DATE.length() : 0) + (hasDate && hasType ? SPACER.length() : 0) + transactionLength,
											false,
											false
											))
									.append(hasAmount && (hasDate || hasType) ? SPACER : "")
									.append(!hasAmount ? "" : t.getAmount().map(
											n ->
													{
														final String str = n.toString(10);
														final StringBuilder ret = new StringBuilder(amountLength).append("$ ");
														return (str.length() <= CENTS.length()
																? ret
																	.append(pad("0.", " ", amountLength - ret.length() - CENTS.length(), true, false))
																	.append(str.length() == 1 ? "0" : "")
																	.append(str)
																: ret
																	.append(pad(str, " ", amountLength - ret.length() - ".".length(), true, false))
																	.insert(amountLength - CENTS.length() - ".".length(), ".")
														);
													}
											).orElse(amountPlaceholder))
									.toString()
								)
						.collect(Collectors.toList())
		));
	}
}

@FunctionalInterface
interface State {
	State next();
}

@FunctionalInterface
interface Writable {
	void println(Object o);

	default void println() {
		println("");
	}

	default void println(Object...objs) {
		Arrays.stream(objs).forEach(this::println);
	}

	default void println(Iterable<?> objs) {
		StreamSupport.stream(objs.spliterator(), false).forEach(this::println);
	}
}
