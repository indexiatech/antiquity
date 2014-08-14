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

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventElement;
import co.indexia.antiquity.range.Range;

/**
 * Utils to manipulates a single {@link Element} for versioning purposes.
 * 
 * @see ElementUtils
 */
public class VersionedElementUtils<V extends Comparable<V>> {
    public VersionedElementUtils() {
    }

    /**
     * defines the start or the end edges of a range.
     */
    enum StartOrEnd {
        START, END
    }

    /**
     * Calculate and set the private hash property for the specified active
     * vertex.
     * 
     * @param vertex active vertex to calculate and set the private hash for
     * @throws IllegalStateException if specified vertex is not active
     */
    public void setPrivateHash(Vertex vertex) {
        Preconditions.checkArgument(vertex instanceof ActiveVersionedVertex, "Vertex currently must be active.");
        String newHash = ElementUtils.calculateElementPrivateHash(vertex, VEProps.antiquityElementsKeys);

        ActiveVersionedVertex<V> av = ((ActiveVersionedVertex<V>) vertex);
        String oldHash = getPrivateHash(av);

        if (Strings.isNullOrEmpty(oldHash) || (!oldHash.equals(newHash))) {
            av.getRaw().setProperty(VEProps.PRIVATE_HASH_PROP_KEY, newHash);
        }
    }

    /**
     * Get the private hash of the specified vertex, return null if no private
     * hash exists.
     * 
     * @param vertex The vertex instance to get the private hash for
     * @return The private hash as a string
     */
    public String getPrivateHash(Vertex vertex) {
        Preconditions.checkArgument(vertex instanceof ActiveVersionedVertex, "Vertex currently must be active.");

        ActiveVersionedVertex<V> av = (ActiveVersionedVertex<V>)vertex;
        return av.getPrivateHash();
    }

    /**
     * Set a version range of the specified historic element.
     * 
     * @param versionedElement The element to set the version range.
     * @param range The version {@link co.indexia.antiquity.range.Range}
     */
    public void setVersion(HistoricVersionedElement versionedElement, Range<V> range) {
        setStartVersion(versionedElement, range.min());
        setEndVersion(versionedElement, range.max());
    }

    /**
     * Set the start version range of the specified historic element.
     * 
     * @param versionedElement The element to set the start version range.
     * @param startVersion The start version to set
     */
    public void setStartVersion(HistoricVersionedElement versionedElement, V startVersion) {
        setVersion(StartOrEnd.START, versionedElement, startVersion);
    }

    /**
     * Set the end version range of the specified historic element.
     * 
     * @param versionedElement The element to set the end version range.
     * @param endVersion The end version to set
     */
    public void setEndVersion(HistoricVersionedElement versionedElement, V endVersion) {
        setVersion(StartOrEnd.END, versionedElement, endVersion);
    }

    /**
     * Set the start or end version of the historic element
     * 
     * @param startOrEnd Whether to set the start or the end of the version
     *        range.
     * @param versionedElement The graph {@link Element} to set the version for
     * @param version The version to set
     */
    public void setVersion(StartOrEnd startOrEnd, HistoricVersionedElement versionedElement, V version) {
        if (startOrEnd == StartOrEnd.START) {
            versionedElement.getRaw().setProperty(VEProps.VALID_MIN_VERSION_PROP_KEY, version);
        } else {
            versionedElement.getRaw().setProperty(VEProps.VALID_MAX_VERSION_PROP_KEY, version);
        }
    }

    /**
     * Get the start version of the specified historic element
     * 
     * <p>
     * Note: this method receive a plain vertex and not
     * {@link HistoricVersionedVertex} as it's executed by
     * {@link HistoricVersionedVertexPredicate} over base graph elements.
     * </p>
     * 
     * @param versionedElement The element to get the start version.
     * @return The start version of the specified element.
     */
    @SuppressWarnings("unchecked")
    public V getStartVersion(Element versionedElement) {
        ensureHistoricType(versionedElement);
        return (V) versionedElement.getProperty(VEProps.VALID_MIN_VERSION_PROP_KEY);
    }

    /**
     * Get the end version of the specified historic element
     * 
     * @param versionedElement The element to get the end version.
     * @return The end version of the specified element.
     */
    @SuppressWarnings("unchecked")
    public V getEndVersion(Element versionedElement) {
        Element element = versionedElement;
        if (versionedElement instanceof HistoricVersionedElement) {
            // must be invoked on underline otherwise an infinent loop will
            // occur
            element = ((HistoricVersionedElement) versionedElement).getRaw();
        }

        return (V) element.getProperty(VEProps.VALID_MAX_VERSION_PROP_KEY);
    }

    /**
     * Get the version range of the specified historic element.
     * 
     * <p>
     * Note: this method receive a plain vertex and not
     * {@link HistoricVersionedVertex} as it's executed by
     * {@link HistoricVersionedVertexPredicate} over base graph elements.
     * </p>
     * 
     * @param versionedElement The element to get the version range for.
     * @return a {@link Range} of version of the specified element.
     */
    public Range<V> getVersionRange(Element versionedElement) {
        ensureHistoricType(versionedElement);
        return Range.range(getStartVersion(versionedElement), getEndVersion(versionedElement));
    }

    /**
     * Determine whether the specified version is the start version of the
     * specified historic element.
     * 
     * @param version The version to determine as the start of the version
     *        range.
     * @param versionedElement The element to check
     * @return true if the specified version is the start version of the
     *         specified element.
     */
    public boolean isStartVersion(V version, HistoricVersionedElement versionedElement) {
        return version.equals(getStartVersion(versionedElement));
    }

    /**
     * Determine whether the specified version is the end version of the
     * specified historic element.
     * 
     * @param version The version to determine as the end of the version range.
     * @param versionedElement The element to check
     * @return true if the specified version is the end version of the specified
     *         element.
     */
    public boolean isEndVersion(V version, HistoricVersionedElement versionedElement) {
        return version.equals(getEndVersion(versionedElement));
    }

    /**
     * Return base element if the specified element is eventable (probably
     * active).
     * 
     * Typically used by internal functions that requires to modify active
     * elements without triggering versioning.
     * 
     * @param element The element to check whether is active or not.
     * @return If the specified element is active, then return its base element.
     */
    public Element getNonEventableElement(Element element) {
        if (element instanceof ActiveVersionedElement) {
            return getNonEventableElement((((ActiveVersionedElement) element).getRaw()));
        } else if (element instanceof EventElement) {
            return getNonEventableElement((((EventElement) element).getBaseElement()));
        } else {
            return element;
        }
    }

    /**
     * Return the base vertex if the specified vertex is eventable.
     * 
     * @param vertex the vertex
     * @return non eventable vertex.
     */
    public Vertex getNonEventableVertex(Vertex vertex) {
        return (Vertex) getNonEventableElement(vertex);
    }

    /**
     * Return the base edge if the specified vertex is eventable.
     * 
     * @param edge the edge
     * @return non eventable edge.
     */
    public Edge getNonEventableEdge(Edge edge) {
        return (Edge) getNonEventableElement(edge);
    }

    /**
     * Determine if the specified element is internal, means it is an element
     * that is only used by antiquity to function properly and should probably
     * not be visible to the user.
     * 
     * @param e the element
     * @return true if the specified element is identified as internal.
     */
    public boolean isInternal(Element e) {
        Boolean isInternalElement =
                (!Sets.intersection(e.getPropertyKeys(), VEProps.internalPreservedElementKeys).isEmpty());
        Boolean isInternalEdge = false;
        if (e instanceof Edge) {
            isInternalEdge = VEProps.internalPreservedEdgeLabels.contains(((Edge) e).getLabel());
        }

        return isInternalElement || isInternalEdge;
    }

    /**
     * Ensure that the specified element is active.
     * 
     * @param e the element to check
     * @throws IllegalStateException if the specified element is not ACTIVE.
     */
    public void ensureActiveType(Element e) {
        if ((!(e instanceof ActiveVersionedVertex)) && getElementType(e) != VEProps.GRAPH_TYPE.ACTIVE) {
            throw new IllegalArgumentException("The specified ID is not of an active vertex.");
        }
    }

    /**
     * Ensure that the specified element is historic.
     * 
     * @param e the element to check
     * @throws IllegalStateException if the specified element is not HISTORIC.
     */
    public void ensureHistoricType(Element e) {
        if ((!(e instanceof HistoricVersionedVertex)) && getElementType(e) != VEProps.GRAPH_TYPE.HISTORIC) {
            throw new IllegalArgumentException("The specified ID is not of a historic vertex.");
        }
    }

    /**
     * Return the element type {@link VEProps.GRAPH_TYPE}.
     * 
     * @param e The element to test
     * @return {@link VEProps.GRAPH_TYPE#ACTIVE} or
     *         {@link VEProps.GRAPH_TYPE#HISTORIC}
     */
    public VEProps.GRAPH_TYPE getElementType(Element e) {
        // we may receive the underline element for test
        // Preconditions.checkArgument((e instanceof ActiveVersionedElement || e
        // instanceof HistoricVersionedElement),
        // "Unidentified element");
        Boolean historic = e.getProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY);
        Preconditions.checkNotNull(historic, "Element has no [" + VEProps.HISTORIC_ELEMENT_PROP_KEY + "] key.)");

        if ((Boolean) e.getProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY)) {
            return VEProps.GRAPH_TYPE.HISTORIC;
        } else {
            return VEProps.GRAPH_TYPE.ACTIVE;
        }
    }

    /**
     * True if vertex is attached to edges.
     * 
     * @param vertex The vertex to test
     * @return true if vertex is attached to some edge.
     */
    public boolean isVertexAttached(Vertex vertex) {
        return vertex.getEdges(Direction.BOTH).iterator().hasNext();
    }

    /**
     * Sync the specified active element with the corresponding one.
     * 
     * @param a active element
     * @param h historic, latest element.
     */
    public void syncActiveAndLatestHistoric(ActiveVersionedElement<V, ?> a, HistoricVersionedElement<V, ?> h) {

        Set<String> removedKeys = new HashSet<String>(h.getPropertyKeys());
        removedKeys.removeAll(VEProps.internalPreservedElementKeys);
        removedKeys.removeAll(a.getPropertyKeys());

        for (String k : a.getPropertyKeys()) {
            if (!VEProps.nonCopiableKeys.contains(k)) {
                h.getRaw().setProperty(k, a.getProperty(k));
            }
        }

        for (String k : removedKeys) {
            h.getRaw().removeProperty(k);
        }
    }

    public Comparator<HistoricVersionedVertex<V>> getHistoricVersionedVertexComparator() {
        return new Comparator<HistoricVersionedVertex<V>>() {
            @Override
            public int compare(HistoricVersionedVertex<V> o1, HistoricVersionedVertex<V> o2) {
                return o1.getVersion().max().compareTo(o2.getVersion().max());
            }
        };
    }

    public Comparator<HistoricVersionedEdge<V>> getHistoricVersionedEdgeComparator() {
        return new Comparator<HistoricVersionedEdge<V>>() {
            @Override
            public int compare(HistoricVersionedEdge<V> o1, HistoricVersionedEdge<V> o2) {
                return o1.getVersion().max().compareTo(o2.getVersion().max());
            }
        };
    }
}
