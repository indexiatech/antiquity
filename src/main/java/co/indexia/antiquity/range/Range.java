package co.indexia.antiquity.range;

import java.util.Comparator;

/**
 * <p>
 * Defines a range of objects from a minimum to a maximum point.
 * </p>
 * 
 * <p>
 * Ranges are calculated using the {@link Comparable} interface.
 * </p>
 * 
 * <p>
 * This class is thread safe in case {@link T} objects and {@link Comparable} implementations are thread safe.
 * </p>
 * 
 * <p>
 * Range from/to values must be from the same type.
 * </p>
 * 
 * <p>
 * Range Overlapping possibilities:
 * </p>
 * 
 * <pre>
 * 
 *          |-------------------|          compare to this one
 *              |---------|                contained within
 *          |----------|                   contained within, equal start
 *                  |-----------|          contained within, equal end
 *          |-------------------|          contained within, equal start+end
 *    |------------|                       not fully contained, overlaps start
 *                  |---------------|      not fully contained, overlaps end
 *    |-------------------------|          overlaps start, bigger
 *          |-----------------------|      overlaps end, bigger
 *    |------------------------------|     overlaps entire period
 *                              |-----|    overlaps (only touches) end.
 *  |-------|                              overlaps (only touches) start.
 * 
 * </pre>
 * 
 * <p>
 * No overlapping possibilities
 * </p>
 * 
 * <i>note: Implementation must override equal/hashCode and provide identify based equality comparison</i>
 * 
 * <pre>
 *          |-------------------|          compare to this one
 *    |---|                                ends before
 *                                |---|    starts after
 * </pre>
 * 
 * @param <T>
 *            The element type of this range.
 */
public class Range<T> {
	/**
	 * The min inclusive value in this range.
	 */
	private final T min;

	/**
	 * The max inclusive value in this range.
	 */
	private final T max;

	/**
	 * The {@link Comparator} used in this range.
	 */
	private final Comparator<T> comparator;

	/**
	 * Creates a {@link Range} instance.
	 * 
	 * <p>
	 * Range values cannot be null.
	 * </p>
	 * 
	 * @param value1
	 *            the first value of the {@link Range}.
	 * @param value2
	 *            the second value of the {@link Range}.
	 * @param comparator
	 *            the comparator to be used as the ordering scheme, null for natural ordering.
	 * @throws IllegalArgumentException
	 *             In case specified values are null.
	 */
	@SuppressWarnings("unchecked")
	private Range(T value1, T value2, Comparator<T> comparator) {
		if (value1 == null || value2 == null) {
			throw new IllegalArgumentException("Range values cannot be null: value1 [" +
					value1 + "] value2 [" + value2 + "]");
		}

		if (comparator == null) {
			comparator = NaturalOrderingComparator.NATURAL;
		}

		if (comparator.compare(value1, value2) < 1) {
			this.min = value1;
			this.max = value2;
		} else {
			this.min = value2;
			this.max = value1;
		}
		this.comparator = comparator;
	}

	/**
	 * Create a range using the specified value as the min and max edges of this range.
	 * 
	 * <p>
	 * The range uses the natural ordering of the value to conclude where the values lie in the range.
	 * </p>
	 * 
	 * @param value
	 *            The value to set as the min and max values of this range.
	 * @throws IllegalArgumentException
	 *             if the value is null
	 * @return ClassCastException if the specified value type doesn't implements {@code Comparable}
	 * @see {#isNaturalOrdering()}
	 */
	public static <T extends Comparable<T>> Range<T> is(T value) {
		return range(value, value, null);
	}

	/**
	 * Create a range using the specified element as the min and max values of this range.
	 * 
	 * <p>
	 * The range uses the specified {@link Comparable} to conclude where the values lie in the range.
	 * </p>
	 * 
	 * @param value
	 *            The value to set ax a min and max values of this range.
	 * @param comparator
	 *            The comparator ordering scheme
	 * @throws IllegalArgumentException
	 *             if the value is null
	 * @return ClassCastException if the specified value type doesn't implements {@code Comparable}
	 */
	public static <T extends Comparable<T>> Range<T> is(T value, Comparator<T> comparator) {
		return range(value, value, comparator);
	}

	/**
	 * <p>
	 * Create a range using the specified value as the inclusive min and max values of this range
	 * </p>
	 * 
	 * <p>
	 * The range uses the natural ordering of the value to conclude where the values lie in the range.
	 * </p>
	 * 
	 * <p>
	 * The values of the range may be passed in any order (min,max) or (max,min).
	 * </p>
	 * 
	 * @param from
	 *            the first value (inclusive) that defines the edge of the range.
	 * @param to
	 *            the second value (inclusive) that defines the edge of the range.
	 * @return the created range object
	 * @throws IllegalArgumentException
	 *             if either of the specified values is null
	 * @return ClassCastException if the specified value type doesn't implements {@code Comparable}
	 */
	public static <T extends Comparable<T>> Range<T> range(T from, T to) {
		return range(from, to, null);
	}

	/**
	 * <p>
	 * Create a range using the specified value as the inclusive min and max values of this range
	 * </p>
	 * 
	 * <p>
	 * The range uses the specified {@link Comparable} to conclude where the values lie in the range.
	 * </p>
	 * 
	 * <p>
	 * The values of the range may be passed in any order (min,max) or (max,min).
	 * </p>
	 * 
	 * @param from
	 *            the first value (inclusive) that defines the edge of the range.
	 * @param to
	 *            the second value (inclusive) that defines the edge of the range.
	 * @param comparator
	 *            the comparator to be used, null for natural ordering
	 * @return the created range object, not null
	 * @throws IllegalArgumentException
	 *             if either element is null
	 * @return ClassCastException if the specified value type does not implements {@code Comparable}
	 */
	public static <T> Range<T> range(T from, T to, Comparator<T> comparator) {
		return new Range<T>(from, to, comparator);
	}

	/**
	 * Return the minimum edge of this range.
	 * 
	 * @return
	 */
	public T min() {
		return min;
	}

	/**
	 * Return the maximum edge of this range.
	 * 
	 * @return
	 */
	public T max() {
		return max;
	}

	/**
	 * <p>
	 * Return the comparator used to determine if {@link Range}/Points are within this {@link Range}.
	 * </p>
	 * 
	 * <p>
	 * In case no comparator was specified, a natural ordering is used.
	 * </p>
	 * 
	 * @return the comparator being used, not null
	 * @see {@link #isNaturalOrdering()}.
	 * @see NaturalOrderingComparator
	 */
	public Comparator<T> getComparator() {
		return comparator;
	}

	// Points comparisons
	/**
	 * Checks whether this range contains the specified point value.
	 * 
	 * @param point
	 *            the point value to check for
	 * @return true if this range contains the specified point value
	 * @throws IllegalArgumentException
	 *             If the specified <i>point</i> is null.
	 */
	public boolean contains(T point) {
		notNull(point);

		return ((comparator.compare(point, min())) > -1 && (comparator.compare(point, max())) < 1);
	}

	/**
	 * Checks whether this range is before the specified point.
	 * 
	 * @param point
	 *            the point value to check for
	 * @return true if this entire range is before the specified point
	 * @throws IllegalArgumentException
	 *             If the specified <i>point</i> is null.
	 */
	public boolean isBefore(T point) {
		notNull(point);

		return comparator.compare(point, max()) > 0;
	}

	/**
	 * Checks whether this range is after the specified point.
	 * 
	 * @param point
	 *            the point value to check for
	 * @return true if this entire range is before the specified point
	 * @throws IllegalArgumentException
	 *             If the specified <i>point</i> is null.
	 */
	public boolean isAfter(T point) {
		notNull(point);

		return comparator.compare(point, min()) < 0;
	}

	/**
	 * Check whether this range starts with the specified point.
	 * 
	 * @param point
	 *            The point value to check if the range starts with
	 * @return true if the range minimum value equals to the specified point.
	 * @throws IllegalArgumentException
	 *             If the specified <i>point</i> is null.
	 */
	public boolean isStartedWith(T point) {
		return (comparator.compare(point, min()) == 0);
	}

	/**
	 * Check whether this range starts with the specified point.
	 * 
	 * @param point
	 *            The point value to check if the range starts with
	 * @return true if the range minimum value equals to the specified point.
	 * @throws IllegalArgumentException
	 *             If the specified <i>point</i> is null.
	 */
	public boolean isEndedWith(T point) {
		return (comparator.compare(point, max()) == 0);
	}

	/**
	 * Check whether the specified value is null.
	 * 
	 * @param value
	 *            The value of the range type {@link T}
	 * @throws IllegalArgumentException
	 *             If value is null
	 */
	private void notNull(T value) throws IllegalArgumentException {
		if (value == null)
			throw new IllegalArgumentException("Value is null.");
	}

	/**
	 * <p>
	 * Indicates whether some other object is "equal to" this range.
	 * </p>
	 * 
	 * <p>
	 * Ranges are equal only if the min and max values are equal in both objects, that ignores any differences in the
	 * {@link Comparator}.
	 * </p>
	 * 
	 * @param obj
	 *            the reference object with which to compare.
	 * @return {@code true} if this object is the same as the obj
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || obj.getClass() != getClass()) {
			return false;
		} else {
			@SuppressWarnings("unchecked")
			// OK because we checked the class above
			Range<T> range = (Range<T>) obj;
			return min().equals(range.min()) &&
					max().equals(range.max());
		}
	}

	/**
	 * <p>
	 * Gets a hash code for this {@link Range}.
	 * </p>
	 * 
	 * @return a hash code value for this {@link Range}
	 */
	@Override
	public int hashCode() {
		int result;
		result = 17;
		result = 37 * result + getClass().hashCode();
		result = 37 * result + min().hashCode();
		result = 37 * result + max().hashCode();

		return result;
	}

	/**
	 * <p>
	 * Gets the range as a {@code String}.
	 * </p>
	 * 
	 * <p>
	 * The format of the String is '[<i>min</i>..<i>max</i>]'.
	 * </p>
	 * 
	 * @return the {@code String} representation of this range
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(32);
		return sb.append('(').append(min()).append("..").append(max()).append(")").toString();
	}
}
