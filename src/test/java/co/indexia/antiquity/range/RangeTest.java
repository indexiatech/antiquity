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

/**
 * An interface for testing a {@link Range} of a certain type.
 * 
 * @param <T> The type elements of the {@link Range}
 */
public interface RangeTest<T> {
    /**
     * <p>
     * Test whether {@link Range}s are equal
     * 
     * Element orders is not important (i.e if number is compared than range
     * (1,10) equals (10,1))
     * </p>
     * 
     * @see {@link Range#equals(Object)}
     */
    public void testRangesEquality();

    /**
     * <p>
     * Test whether range X contains point Y
     * </p>
     * 
     * @see {@link Range#contains(Object)}
     */
    public void testIfXContainsPointY();

    /**
     * <p>
     * Test whether range X is before point Y
     * </p>
     * 
     * @see {@link Range#isBefore(Object)}
     */
    public void testIfXIsBeforePointY();

    /**
     * <p>
     * Test whether range X is after point Y
     * </p>
     * 
     * @see {@link Range#isAfter(Object)}
     */
    public void testIfXIsAfterPointY();

/**
	 * <p>Test if Y {@link Range} is contained with X {@link Range}.</p>
	 *
	 * <p>Where (x.min < y.min) && (x.max > y.max)
	 *
	 * <p>{Expected result of @link Range#contains(Object) is true</p>
	 * <p>{Expected result of @link Range#isBefore(Object) is false</p>
	 * <p>{Expected result of @link Range#isAfter(Object) is false</p>
	 * <p>{Expected result of @link Range#isOverlappedBy(Object) is true</p>
	 * @see {@link Range#contains(Range)}
	 * @see {@link Range#isBefore(Range)
	 * @see {@link Range#isAfter(Range)
	 */
    public void testIfRangeYIsContainedWithinX();

    /**
     * <p>
     * Test if Y {@link Range} is contained within X {@link Range} and start at
     * the same point.
     * </p>
     * 
     * <p>
     * Where (x.min == y.min) && (x.max > y.max)
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is true
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is true
     * </p>
     */
    public void testIfRangeYIsConttainedWithinEqualStartX();

    /**
     * <p>
     * Test if Y {@link Range} is contained within X {@link Range} and ends
     * (where x.start == y.start) at the same point.
     * </p>
     * 
     * <p>
     * Where (x.min >= y.min) && (x.max == y.max)
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is true
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is true
     * </p>
     * 
     * @see {@link Range#contains(Range)}
     */
    public void testIfRangeYIsContainedWithinEqualEndX();

    /**
     * <p>
     * Test if Y {@link Range} is contained within X {@link Range} and start and
     * ends are equal.
     * </p>
     * 
     * <p>
     * This also means that both {@link Range}s objects are equal.
     * </p>
     * 
     * <p>
     * Where (x.min == y.min) && (x.end == y.end)
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is true
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is true
     * </p>
     * 
     * @see {@link Range#contains(Range)}
     */
    public void testIfRangeYIsContainedWitinEqualStartAndEndX();

    /**
     * <p>
     * Test if Y {@link Range} is not fully contained within X {@link Range} and
     * overlaps the start of it.
     * </p>
     * 
     * <p>
     * Where (y.min > x.min) && (x.end > y.end)
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is true
     * </p>
     */
    public void testIfRangeNotFullyContainedOverlapsStart();

    /**
     * <p>
     * Test if Y {@link Range} is not fully contained within X {@link Range} and
     * overlaps the end of it.
     * </p>
     * 
     * <p>
     * Where (x.min < y.min) && (x.end < y.end)
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is true
     * </p>
     */
    public void testIfRangeNotFullyContainedOverlapsEnd();

    /**
     * <p>
     * Test if Y {@link Range} overlaps X period (in both of its edges)
     * </p>
     * 
     * <p>
     * Where (x.min > y.min) && (x.max < y.max)
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is true
     * </p>
     */
    public void testIfRangeOverlapsEntirePeriod();

    /**
     * <p>
     * Test whether X starts before Y
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is true
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is false
     * </p>
     */
    public void testIfRangeXStartsBeforeY();

    /**
     * <p>
     * Test whether X ends before Y
     * </p>
     * 
     * <p>
     * {Expected result of @link Range#contains(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isBefore(Object) is true
     * </p>
     * <p>
     * {Expected result of @link Range#isAfter(Object) is false
     * </p>
     * <p>
     * {Expected result of @link Range#isOverlappedBy(Object) is false
     * </p>
     */
    public void testIfRangeXEndsBeforeY();
}
