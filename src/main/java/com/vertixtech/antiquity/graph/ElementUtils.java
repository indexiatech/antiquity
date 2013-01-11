/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vertixtech.antiquity.graph;

import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.tinkerpop.blueprints.Element;

/**
 * General {@link Element} utilities.
 */
public class ElementUtils {

	public static Map<String, Object> getPropertiesAsMap(Element element) {
		return getPropertiesAsMap(element, null);
	}

	/**
	 * Returns {@link Element}'s properties as an immutable map.
	 * 
	 * @return An immutable map of the specified {@link Element} properties exlcuding the specified keys
	 */
	public static Map<String, Object> getPropertiesAsMap(Element element, Set<String> excludedKeys) {
		Builder<String, Object> props = ImmutableMap.builder();

		for (String key : element.getPropertyKeys()) {
			if ((excludedKeys != null) && excludedKeys.contains(key))
				continue;

			props.put(key, element.getProperty(key));
		}

		return props.build();
	}

	/**
	 * <p>
	 * Calculate the private hash of an {@link Element}.
	 * </p>
	 * 
	 * <p>
	 * The private hash contains only the properties of the {@link Element}, without its associated elements.
	 * </p>
	 * 
	 * <p>
	 * The hash is calculated based on SHA1 algorithm
	 * </p>
	 * 
	 * TODO Handle arrays values properly.
	 * 
	 * @param element
	 *            The element to calculate the private hash for.
	 * @param excludedKeys
	 *            the keys to exclude when hash is calculated.
	 * @return A string representation of the hash
	 * @see Hasher#toString()
	 */
	public static String calculateElementPrivateHash(Element element, Set<String> excludedKeys) {
		Map<String, Object> props = ElementUtils.getPropertiesAsMap(element, excludedKeys);
		Joiner.MapJoiner propsJoiner = Joiner.on('&').withKeyValueSeparator("=");

		HashFunction hf = Hashing.sha1();
		Hasher h = hf.newHasher();

		h.putString("[" + element.getId().toString() + "]");
		h.putString(propsJoiner.join(props));

		return h.hash().toString();
	}

	/**
	 * Get the specified {@link Element} properties as a string.
	 * 
	 * @param withHeader
	 *            if true the element's id will be printed in the first line.
	 * @param e
	 *            The element to get the properties string for
	 * @return A formatted string containing the specified element's properties
	 */
	public static String getElementPropsAsString(Element e, boolean withHeader) {
		StringBuffer elementPropsStr = new StringBuffer();

		if (withHeader)
			elementPropsStr.append(e);
		for (String key : e.getPropertyKeys())
			elementPropsStr.append("\t").append(key).append("->").append(e.getProperty(key));

		return elementPropsStr.toString();
	}
}
