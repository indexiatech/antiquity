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
package co.indexia.antiquity.graph;

import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;

/**
 * General read-only utils for a single {@link Element}.
 */
public class ElementUtils {

    /**
     * Get element's properties as an immutable java Map.
     * 
     * @param element The element to get the properties for.
     * @return a map of properties.
     */
    public static Map<String, Object> getPropertiesAsMap(Element element) {
        return getPropertiesAsMap(element, null);
    }

    /**
     * Returns {@link Element}'s properties as an immutable map.
     * 
     * @return An immutable map of the specified {@link Element} properties
     *         exlcuding the specified keys
     */
    public static Map<String, Object> getPropertiesAsMap(Element element, Set<String> excludedKeys) {
        Builder<String, Object> props = ImmutableMap.builder();

        for (String key : element.getPropertyKeys()) {
            if ((excludedKeys != null) && excludedKeys.contains(key)) {
                continue;
            }

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
     * The private hash contains only the properties of the {@link Element},
     * without its associated elements.
     * </p>
     * 
     * <p>
     * The hash is calculated based on SHA1 algorithm
     * </p>
     * 
     * TODO Handle arrays values properly.
     * 
     * @param element The element to calculate the private hash for.
     * @param excludedKeys the keys to exclude when hash is calculated.
     * @return A string representation of the hash
     * @see HashCode#toString()
     */
    public static String calculateElementPrivateHash(Element element, Set<String> excludedKeys) {
        Map<String, Object> props = ElementUtils.getPropertiesAsMap(element, excludedKeys);
        Joiner.MapJoiner propsJoiner = Joiner.on('&').withKeyValueSeparator("=");

        HashFunction hf = Hashing.sha1();
        Hasher h = hf.newHasher();

        //h.putString("[" + element.getId().toString() + "]");
        h.putString(propsJoiner.join(props), Charset.defaultCharset());

        return h.hash().toString();
    }

    /**
     * Copy properties from one element to another.
     * 
     * @param from element to copy properties from
     * @param to element to copy properties to
     * @param excludedKeys the keys that should be excluded from being copied.
     */
    public static void copyProps(Element from, Element to, Set<String> excludedKeys) {
        for (String k : from.getPropertyKeys()) {
            if (excludedKeys != null && excludedKeys.contains(k)) {
                continue;
            }

            to.setProperty(k, from.getProperty(k));
        }
    }

    /**
     * Get the specified {@link Element} properties as a string.
     * 
     * @param withHeader if true the element's id will be printed in the first
     *        line.
     * @param e The element to get the properties string for
     * @return A formatted string containing the specified element's properties
     */
    public static String getElementPropsAsString(Element e, boolean withHeader) {
        StringBuilder elementPropsStr = new StringBuilder();

        if (withHeader) {
            elementPropsStr.append(e);
        }
        for (String key : e.getPropertyKeys()) {
            elementPropsStr.append("\t").append(key).append("->").append(e.getProperty(key));
        }

        return elementPropsStr.toString();
    }

    /**
     * Get a single element by key, value.
     * 
     * @param graph The graph to get the element from
     * @param key The key of the element
     * @param value The value of the element
     * @param clazz The class element type, can be Vertex | Edge.
     * @return Found vertex that matches the specified criteria or null if not
     *         found.
     * @throws IllegalStateException if multiple vertices found that matches the
     *         specified criteria
     */
    @SuppressWarnings("unchecked")
    public static <E extends Element> E getSingleElement(Graph graph, String key, Object value, Class<E> clazz) {
        Iterable<?> it =
                Vertex.class.isAssignableFrom(clazz) ? graph.query().has(key, value).vertices() : graph.query().has(key, value).edges();
        Iterator<?> iter = it.iterator();

        E e = null;

        if (iter.hasNext()) {
            e = (E) iter.next();
        }

        return e;
    }

    /**
     * Get a single element.
     * 
     * @param it iterable of elements.
     * @param <E> must be of {@link Element} type.
     * @return single element.
     * @throws IllegalStateException if multiple elements found.
     */
    @SuppressWarnings("unchecked")
    public static <E extends Element> E getSingleElement(Iterable<E> it) {
        Iterator<?> iter = it.iterator();
        E e = null;

        if (iter.hasNext()) {
            e = (E) iter.next();
        }


        if (iter.hasNext()) throw new IllegalStateException(String.format("Multiple elements found."));

        return e;
    }

}
