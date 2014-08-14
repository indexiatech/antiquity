/**
 * Copyright (c) 2012-2014 "Indexia Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
 * This class is thread safe in case {@link T} objects and {@link Comparable}
 * implementations are thread safe.
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
 * <i>note: Implementation must override equal/hashCode and provide identify
 * based equality comparison</i>
 *
 * <pre>
 *          |-------------------|          compare to this one
 *    |---|                                ends before
 *                                |---|    starts after
 * </pre>
 *
 * @param <T> The element type of this range.
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
     * @param value1 the first value of the {@link Range}.
     * @param value2 the second value of the {@link Range}.
     * @param comparator the comparator to be used as the ordering scheme, null
     *        for natural ordering.
     * @throws IllegalArgumentException In case specified values are null.
     */
    @SuppressWarnings("unchecked")
    private Range(T value1, T value2, Comparator<T> comparator) {
        if (value1 == null || value2 == null) {
            throw new IllegalArgumentException("Range values cannot be null: value1 [" + value1 + "] value2 [" + value2
                    + "]");
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
     * Create a range using the specified value as the min and max edges of this
     * range.
     *
     * <p>
     * The range uses the natural ordering of the value to conclude where the
     * values lie in the range.
     * </p>
     *
     * @param value The value to set as the min and max values of this range.
     * @throws IllegalArgumentException if the value is null
     * @return ClassCastException if the specified value type doesn't implements
     *         {@code Comparable}
     */
    public static <T extends Comparable<T>> Range<T> is(T value) {
        return range(value, value, null);
    }

    /**
     * Create a range using the specified value as the min and max values of
     * this range.
     *
     * <p>
     * The range uses the specified {@link Comparable} to conclude where the
     * values lie in the range.
     * </p>
     *
     * @param value The value to set ax a min and max values of this range.
     * @param comparator The comparator ordering scheme
     * @throws IllegalArgumentException if the value is null
     * @return ClassCastException if the specified value type doesn't implements
     *         {@code Comparable}
     */
    public static <T extends Comparable<T>> Range<T> is(T value, Comparator<T> comparator) {
        return range(value, value, comparator);
    }

    /**
     * <p>
     * Create a range using the specified value as the inclusive min and max
     * values of this range
     * </p>
     *
     * <p>
     * The range uses the natural ordering of the value to conclude where the
     * values lie in the range.
     * </p>
     *
     * <p>
     * The values of the range may be passed in any order (min,max) or
     * (max,min).
     * </p>
     *
     * @param from the first value (inclusive) that defines the edge of the
     *        range.
     * @param to the second value (inclusive) that defines the edge of the
     *        range.
     * @return the created range object
     * @throws IllegalArgumentException if either of the specified values is
     *         null
     * @throws ClassCastException if the specified value type doesn't implements
     *         {@code Comparable}
     */
    public static <T extends Comparable<T>> Range<T> range(T from, T to) {
        return range(from, to, null);
    }

    /**
     * <p>
     * Create a range using the specified value as the inclusive min and max
     * values of this range
     * </p>
     *
     * <p>
     * The range uses the specified {@link Comparable} to conclude where the
     * values lie in the range.
     * </p>
     *
     * <p>
     * The values of the range may be passed in any order (min,max) or
     * (max,min).
     * </p>
     *
     * @param from the first value (inclusive) that defines the edge of the
     *        range.
     * @param to the second value (inclusive) that defines the edge of the
     *        range.
     * @param comparator the comparator to be used, null for natural ordering
     * @return the created range object, not null
     * @throws IllegalArgumentException if either value is null
     * @throws ClassCastException if the specified value type does not
     *         implements {@code Comparable}
     */
    public static <T> Range<T> range(T from, T to, Comparator<T> comparator) {
        return new Range<T>(from, to, comparator);
    }

    /**
     * Return the minimum edge of this range.
     *
     * @return the minimum edge of the range
     */
    public T min() {
        return min;
    }

    /**
     * Return the maximum edge of this range.
     *
     * @return the maximum edge of the range
     */
    public T max() {
        return max;
    }

    /**
     * <p>
     * Return the comparator used to determine if {@link Range}/Points are
     * within this {@link Range}.
     * </p>
     *
     * <p>
     * In case no comparator was specified, a natural ordering is used.
     * </p>
     *
     * @return the comparator being used, not null
     * @see NaturalOrderingComparator
     */
    public Comparator<T> getComparator() {
        return comparator;
    }

    // Points comparisons
    /**
     * Checks whether this range contains the specified point value.
     *
     * @param point the point value to check for
     * @return {@code true} if this range contains the specified point value
     * @throws IllegalArgumentException If the specified <i>point</i> is null.
     */
    public boolean contains(T point) {
        notNull(point);

        return ((comparator.compare(point, min())) > -1 && (comparator.compare(point, max())) < 1);
    }

    /**
     * Checks whether this range is before the specified point.
     *
     * @param point the point value to check for
     * @return {@code true} if this entire range is before the specified point
     * @throws IllegalArgumentException If the specified <i>point</i> is null.
     */
    public boolean isBefore(T point) {
        notNull(point);

        return comparator.compare(point, max()) > 0;
    }

    /**
     * Checks whether this range is after the specified point.
     *
     * @param point the point value to check for
     * @return {@code true} if this entire range is before the specified point
     * @throws IllegalArgumentException If the specified <i>point</i> is null.
     */
    public boolean isAfter(T point) {
        notNull(point);

        return comparator.compare(point, min()) < 0;
    }

    /**
     * Check whether this range starts with the specified point.
     *
     * @param point The point value to check if the range starts with
     * @return {@code true} if the range minimum value equals to the specified
     *         point.
     * @throws IllegalArgumentException If the specified <i>point</i> is null.
     */
    public boolean isStartedWith(T point) {
        return (comparator.compare(point, min()) == 0);
    }

    /**
     * Check whether this range starts with the specified point.
     *
     * @param point The point value to check if the range starts with
     * @return {@code true} if the range minimum value equals to the specified
     *         point.
     * @throws IllegalArgumentException If the specified <i>point</i> is null.
     */
    public boolean isEndedWith(T point) {
        return (comparator.compare(point, max()) == 0);
    }

    // Ranges
    /**
     * Checks whether the specified range is contained within this range.
     *
     * @param otherRange The range to test, null throws exception.
     * @return {@code true} if the specified range is contained within this
     *         range.
     * @throws IllegalArgumentException If otherRange is null.
     */
    public boolean contains(Range<T> otherRange) {
        notNull(otherRange);

        return contains(otherRange.min()) && contains(otherRange.max());
    }

    /**
     * <p>
     * Test if the entire of this range is before the specified range.
     * </p>
     *
     * <p>
     * If ranges have different comperators or types the test will fail.
     * </p>
     *
     * @param otherRange the range to check, null throws exception.
     * @return {@code true} if this range is before the specified range.
     * @throws RuntimeException if ranges cannot be compared
     */
    public boolean isBefore(Range<T> otherRange) {
        notNull(otherRange);

        return isBefore(otherRange.min());
    }

    /**
     * <p>
     * Test if the entire of this range is after the specified range.
     * </p>
     *
     * <p>
     * If ranges have different comperators or types the test will fail.
     * </p>
     *
     * @param otherRange the range to check, null throws exception.
     * @return {@code true} if this range is after the specified range.
     * @throws RuntimeException if ranges cannot be compared
     */
    public boolean isAfter(Range<T> otherRange) {
        notNull(otherRange);

        return isAfter(otherRange.max());
    }

    /**
     * <p>
     * Test if this range is overlapped by the specified range.
     * </p>
     *
     * <p>
     * Two ranges overlap each other if there is at least one point in common.
     * </p>
     *
     * <p>
     * If ranges have different comperators or types the test will fail.
     * </p>
     *
     * @param otherRange the range to check, null throws exception.
     * @return {@code true} if the specified range overlaps with this range
     */
    public boolean isOverlappedBy(Range<T> otherRange) {
        notNull(otherRange);

        return otherRange.contains(min()) || otherRange.contains(max()) || contains(otherRange.min());
    }

    /**
     * Return the intersection of this range and the specified Range.
     *
     * Ranges must overlap.
     *
     * @param otherRange other overlapping {@link Range}
     * @return range object which contains the intersection of this
     *         {@link Range} and the specified {@link Range}
     * @throws IllegalArgumentException if the specified {@link Range} does not
     *         overlap this {@link Range}.
     */
    public Range<T> intersectionWith(Range<T> otherRange) {
        if (this.equals(otherRange)) {
            return this;
        }

        if (!this.isOverlappedBy(otherRange)) {
            throw new IllegalArgumentException(String.format(
                    "The specified range %s does not overlap with this range %s", otherRange.toString(), toString()));
        }

        T min = getComparator().compare(min(), otherRange.min()) < 0 ? otherRange.min() : min();
        T max = getComparator().compare(max(), otherRange.max()) < 0 ? max() : otherRange.max();

        return range(min, max, getComparator());
    }

    // General
    /**
     * Check whether the specified value is null.
     *
     * @param any The object to test
     * @throws IllegalArgumentException If value is null
     */
    private void notNull(Object any) throws IllegalArgumentException {
        if (any == null) {
            throw new IllegalArgumentException("The specified value is null.");
        }
    }

    /**
     * <p>
     * Indicates whether some other object is "equal to" this range.
     * </p>
     *
     * <p>
     * Ranges are equal only if the min and max values are equal in both
     * objects, that ignores any differences in the {@link Comparator}.
     * </p>
     *
     * @param obj the reference object with which to compare.
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
            return min().equals(range.min()) && max().equals(range.max());
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
