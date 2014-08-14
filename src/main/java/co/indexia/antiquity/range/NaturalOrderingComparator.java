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
 * A natural ordering comparator.
 *
 * Used when no {@link Comparator} is specified when instantiating a
 * {@link Range} object.
 *
 * @see Range
 */
@SuppressWarnings("rawtypes")
public enum NaturalOrderingComparator implements Comparator {
    NATURAL;

    /**
     * Comparable based compare implementation.
     *
     * @param o1 the first object to be compared.
     * @param o2 the second object to be compared.
     * @return a negative integer, zero, or a positive integer as the first
     *         argument is less than, equal to, or greater than the second.
     *
     * @see Comparable#compareTo(Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compare(Object o1, Object o2) {
        return ((Comparable) o1).compareTo(o2);
    }
}
