package co.indexia.antiquity.range;

import java.util.Comparator;

/**
 * A natural ordering comparator.
 * 
 * Used when no {@link Comparator} is specified when instantiating a {@link RangeTest} object.
 * 
 * @see {@link RangeTest}
 */
@SuppressWarnings("rawtypes")
public enum NaturalOrderingComparator implements Comparator {
	NATURAL;

	/**
	 * Comparable based compare implementation.
	 * 
	 * @param o1
	 *            the first object to be compared.
	 * @param o2
	 *            the second object to be compared.
	 * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or greater
	 *         than the second.
	 * 
	 * @see {@link Comparable#compareTo()}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public int compare(Object o1, Object o2) {
		return ((Comparable) o1).compareTo(o2);
	}
}
