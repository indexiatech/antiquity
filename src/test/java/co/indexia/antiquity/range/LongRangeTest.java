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

import static co.indexia.antiquity.range.Range.range;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

public class LongRangeTest implements RangeTest<Long> {
    private static Range<Long> x = null;

    /**
     * Set up the test case.
     * 
     * Initialize x variable where all tests compares to the 'x' {@link Range}.
     */
    @BeforeClass
    public static void setUp() {
        x = range(1L, 10L);
    }

    @Override
    @Test
    public void testRangesEquality() {
        assertEquals(range(1L, 10L), x);
        assertEquals(range(10L, 1L), x);

        assertEquals(range(10L, 1L), x.intersectionWith(range(1L, 10L)));
    }

    @Override
    @Test
    public void testIfXContainsPointY() {
        assertFalse(x.contains(-1L));
        assertFalse(x.contains(0L));

        for (long l = 1; l <= 10; l++) {
            assertTrue(x.contains(l));
        }

        assertFalse(x.contains(11L));
        assertFalse(x.contains(12L));
    }

    @Override
    @Test
    public void testIfXIsBeforePointY() {
        assertFalse(x.isBefore(5L));
        assertFalse(x.isBefore(1L));
        assertFalse(x.isBefore(10L));
        assertTrue(x.isBefore(11L));
    }

    @Override
    @Test
    public void testIfXIsAfterPointY() {
        assertFalse(x.isAfter(5L));
        assertFalse(x.isAfter(1L));
        assertFalse(x.isAfter(10L));
        assertTrue(x.isAfter(0L));
    }

    @Override
    @Test
    public void testIfRangeYIsContainedWithinX() {
        Range<Long> y = range(2L, 8L);
        assertTrue(x.contains(y));
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(y, x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeYIsConttainedWithinEqualStartX() {
        Range<Long> y = range(1L, 5L);
        assertTrue(x.contains(y));
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(y, x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeYIsContainedWithinEqualEndX() {
        Range<Long> y = range(4L, 10L);
        assertTrue(x.contains(y));
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(y, x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeYIsContainedWitinEqualStartAndEndX() {
        Range<Long> y = range(1L, 10L);
        assertTrue(x.contains(y));
        assertEquals(y, x);
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(y, x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeNotFullyContainedOverlapsStart() {
        Range<Long> y = range(-3L, 8L);

        assertFalse(x.contains(y));
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(range(1L, 8L), x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeNotFullyContainedOverlapsEnd() {
        Range<Long> y = range(4L, 15L);
        assertFalse(x.contains(y));
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(range(4L, 10L), x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeOverlapsEntirePeriod() {
        Range<Long> y = range(-4L, 15L);
        assertFalse(x.contains(y));
        assertFalse(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertTrue(x.isOverlappedBy(y));

        assertEquals(x, x.intersectionWith(y));
    }

    @Override
    @Test
    public void testIfRangeXStartsBeforeY() {
        Range<Long> y = range(-5L, -2L);
        assertFalse(x.contains(y));
        assertFalse(x.isBefore(y));
        assertTrue(x.isAfter(y));
        assertFalse(x.isOverlappedBy(y));

        // should throw exception
        try {
            x.intersectionWith(y);
            assertFalse(true);
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    public void testIfRangeXEndsBeforeY() {
        Range<Long> y = range(11L, 15L);
        assertFalse(x.contains(y));
        assertTrue(x.isBefore(y));
        assertFalse(x.isAfter(y));
        assertFalse(x.isOverlappedBy(y));

        // should throw exception
        try {
            x.intersectionWith(y);
            assertFalse(true);
        } catch (IllegalArgumentException e) {

        }
    }
}
