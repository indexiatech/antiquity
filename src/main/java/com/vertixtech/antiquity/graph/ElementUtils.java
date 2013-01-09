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
	 * @return
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
}
