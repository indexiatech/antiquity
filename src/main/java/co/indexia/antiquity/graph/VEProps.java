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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;

/**
 * Versioned {@link com.tinkerpop.blueprints.Element}s properties.
 */
public class VEProps {
    private VEProps() {
    }

    public enum GRAPH_TYPE {
        ACTIVE, HISTORIC
    }

    /**
     * Property keys used by Antiquity across all active and historic elements.
     * Filtering these elements should produce an element that exactly matches
     * the properties set by the user.
     */
    public static final Set<String> antiquityElementsKeys;

    /**
     * Property keys that should not be copied from active element to a
     * corresponding historic element. This is a mix of vertex/edge properties.
     */
    public static final Set<String> nonCopiableKeys;

    /**
     * Property keys of preserved elements used by antiquity to function, these
     * are preserved elements that should not be exposed to users.
     */
    public static final Set<String> internalPreservedElementKeys;

    /**
     * Edge labels used by internal elements preserved elements.
     */
    public static final Set<String> internalPreservedEdgeLabels;

    public static final Set<String> vertexIndexedKeys;

    public static final Set<String> edgeIndexedKeys;

    // ----- Versioned Vertex internal properties
    /**
     * A marker property key which indicates that the
     * {@link com.tinkerpop.blueprints.Element} is removed.
     */
    @ReservedKey(copiable = false, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.HISTORIC)
    public static final String REMOVED_PROP_KEY = "__REMOVED__";

    /**
     * A property key which holds the minimum valid version of this element.
     */
    @ReservedKey(copiable = false, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.HISTORIC)
    public static final String VALID_MIN_VERSION_PROP_KEY = "__VALID_MIN_VERSION__";

    /**
     * A property key which holds the maximum valid version of this element.
     */
    @ReservedKey(copiable = false, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.HISTORIC)
    public static final String VALID_MAX_VERSION_PROP_KEY = "__VALID_MAX_VERSION__";

    /**
     * An element property key which indicates whether the element is for
     * historical purposes or not.
     * 
     * Historical elements are elements which were created for audit purposes
     * and are not the active/alive data.
     */
    @ReservedKey(copiable = false, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.BOTH)
    public static final String HISTORIC_ELEMENT_PROP_KEY = "__HISTORIC__";

    /**
     * The key name of the private hash calculation.
     */
    @ReservedKey(copiable = true, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.ACTIVE)
    public static final String PRIVATE_HASH_PROP_KEY = "__PRIVATE_HASH__";

    /**
     * The key name of the natural identifier of a vertex.
     */
    @ReservedKey(copiable = false, internal = false, indexed=true, elementType = Element.class, relevance = ReservedKey.RestrictionType.ACTIVE)
    public static final String NATURAL_VERTEX_ID_PROP_KEY = "__VID__";

    /**
     * The key name of the natural identifier of an edge.
     */
    @ReservedKey(copiable = false, internal = false, indexed=true, elementType = Element.class, relevance = ReservedKey.RestrictionType.ACTIVE)
    public static final String NATURAL_EDGE_ID_PROP_KEY = "__EID__";

    /**
     * The key name of the active element which holds a reference to the latest
     * historic element.
     */
    @ReservedKey(copiable = false, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.ACTIVE)
    public static final String REF_TO_LATEST_HISTORIC_ID_KEY = "__LATEST_H_ID_REF__";

    /**
     * The key name of the historic element which holds a reference to the
     * active element id
     */
    @ReservedKey(copiable = false, internal = false, elementType = Element.class, relevance = ReservedKey.RestrictionType.HISTORIC)
    public static final String REF_TO_ACTIVE_ID_KEY = "__A_ID_REF__";

    // ----- General Internal Properties
    /**
     * The root vertex identifier of historic/active graphs
     */
    @ReservedKey(copiable = false, internal = true, elementType = Vertex.class, relevance = ReservedKey.RestrictionType.BOTH)
    public static final String ROOT_GRAPH_VERTEX_ID = "__ROOT_VERTEX__";

    /**
     * The property key which stores the last graph version
     */
    @ReservedKey(copiable = false, internal = true, elementType = Vertex.class, relevance = ReservedKey.RestrictionType.HISTORIC)
    public static final String LATEST_GRAPH_VERSION_PROP_KEY = "__LATEST_GRAPH_VERSION__";

    // -----------------------
    // Labels

    /**
     * The label name of the edge which creates the chain of a vertex revisions
     */
    public static final String PREV_VERSION_LABEL = "__PREV_VERSION__";

    /**
     * The label that attaches vertices that has no edges to the corresponding
     * (ACTIVE/HISTORIC) root elements.
     */
    public static final String ROOT_OF_EDGE_LABEL = "__ROOT_OF__";



    // -----------------------
    // Values

    /**
     * The value of the {@link VEProps#ROOT_GRAPH_VERTEX_ID} to identify the
     * root vertex of a historic graph.
     */
    public static final String HISTORIC_ROOT_GRAPH_VERTEX_VALUE = "__ROOT_HISTORIC_VERTEX__";

    /**
     * The value of the {@link VEProps#ROOT_GRAPH_VERTEX_ID} to identify the
     * root vertex of an active graph.
     */
    public static final String ACTIVE_ROOT_GRAPH_VERTEX_VALUE = "__ROOT_ACTIVE_VERTEX__";


    // -----------------------
    // Indices

    /**
     * The name of the index which contains the vertex identifiers.
     */
    public static final String GRAPH_VERTEX_IDENTIFIERS_INDEX_NAME = "IDENTIFIER_IDX";

    static {
        Set<String> allKeysSet = new HashSet<String>();
        Set<String> nonCopiableKeysSet = new HashSet<String>();
        Set<String> internalElementsKeysSet = new HashSet<String>();
        Set<String> vertexIndexedKeysSet = new HashSet<String>();
        Set<String> edgeIndexedKeysSet = new HashSet<String>();

        for (Field f : VEProps.class.getFields()) {
            ReservedKey rk = f.getAnnotation(ReservedKey.class);
            if (rk != null) {
                try {
                    if (f.getType() == String.class) {
                        String key = (String) f.get(null);
                        allKeysSet.add(key);
                        if (!rk.copiable()) {
                            nonCopiableKeysSet.add(key);
                        }
                        if (rk.internal()) {
                            internalElementsKeysSet.add(key);
                        }
                        if (rk.indexed()) {
                            if (rk.elementType().equals(Vertex.class)) {
                                vertexIndexedKeysSet.add(key);
                            } else if (rk.elementType().equals(Vertex.class)) {
                                edgeIndexedKeysSet.add(key);
                            } else if (rk.elementType().equals(Element.class)) {
                                vertexIndexedKeysSet.add(key);
                                edgeIndexedKeysSet.add(key);
                            } else {
                                throw new IllegalStateException(String.format(
                                        "Key %s is indexed but its type must be of type Vertex/Edge/Element", f.getName()));
                            }
                        }
                    } else {
                        throw new IllegalStateException(String.format("Found key field %s which is not a string type",
                                f.getName()));
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        antiquityElementsKeys = ImmutableSet.copyOf(allKeysSet);
        nonCopiableKeys = ImmutableSet.copyOf(nonCopiableKeysSet);
        internalPreservedElementKeys = ImmutableSet.copyOf(internalElementsKeysSet);
        vertexIndexedKeys = ImmutableSet.copyOf(vertexIndexedKeysSet);
        edgeIndexedKeys = ImmutableSet.copyOf(edgeIndexedKeysSet);
        internalPreservedEdgeLabels = ImmutableSet.of(PREV_VERSION_LABEL);
    }
}
