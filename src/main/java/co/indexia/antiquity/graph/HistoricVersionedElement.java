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

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import co.indexia.antiquity.range.Range;

/**
 * Base class for {@link HistoricVersionedVertex} and
 * {@link HistoricVersionedEdge}.
 */
public abstract class HistoricVersionedElement<V extends Comparable<V>, T extends Element> implements Element {
    /**
     * The graph instance which loaded this edge.
     */
    private final HistoricVersionedGraph<?, V> graph;

    /**
     * The raw element as retrieved from
     * {@link co.indexia.antiquity.graph.HistoricVersionedGraph}
     */
    private final T rawElement;

    /**
     * Defines the version context of the element, associated properties and
     * elements will match the specified version.
     */
    private Range<V> version;

    /**
     * Creates an instance.
     * 
     * @param rawElement the edge that was loaded from the underline graph.
     * @param graph the graph instance this element is associated with.
     * @param version the requested version range to filter upon.
     */
    public HistoricVersionedElement(T rawElement, HistoricVersionedGraph<?, V> graph, Range<V> version) {
        Preconditions.checkNotNull(rawElement, "Raw element must be set.");
        Preconditions.checkNotNull(graph, "Graph must be set.");
        Preconditions.checkNotNull(version, "Version must be set.");
        // Preconditions.checkNotNull(version, "Version must be set.");
        Preconditions.checkArgument((!(rawElement instanceof HistoricVersionedElement)),
                "rawElement cannot be instance of HistoricVersionElement");

        if (!graph.utils.isInternal(rawElement)) {
            Preconditions.checkArgument(graph.utils.getElementType(rawElement) != VEProps.GRAPH_TYPE.ACTIVE,
                    "Raw element cannot be active");
        }

        this.rawElement = rawElement;
        this.graph = graph;
        this.version = version;
    }

    /**
     * Every historic element has its own unique ID, this method return this
     * identifier, while invoking @{link getId()} returns the ID of the
     * corresponding active object.
     * 
     * @return an identifier.
     */
    public Object getHardId() {
        if (graph.isNaturalIds()) {
            if (rawElement instanceof Vertex) {
                return rawElement.getProperty(VEProps.NATURAL_VERTEX_ID_PROP_KEY);
            } else {
                return rawElement.getProperty(VEProps.NATURAL_EDGE_ID_PROP_KEY);
            }
        } else {
            return rawElement.getId();
        }
    }

    @Override
    public Object getId() {
        return rawElement.getProperty(VEProps.REF_TO_ACTIVE_ID_KEY);
    }

    /**
     * Get the associated graph
     * 
     * @return The associated graph
     */
    protected HistoricVersionedGraph<?, V> getGraph() {
        return graph;
    }

    /**
     * Get the raw element of this historic element. This method is useful if it
     * is required to modify the historic element.
     * 
     * @return The raw element
     */
    public T getRaw() {
        return this.rawElement;
    }

    /**
     * Get the version property of the element.
     * 
     * This property defines the version context of the vertex, associated
     * properties and elements will be filtered according to the set version.
     * 
     * @return The version bound to the vertex
     */
    public Range<V> getVersion() {
        return this.version;
    }

    public void setVersion(Range<V> version) {
        this.version = version;
    }

    @Override
    public String toString() {
        if (graph.utils.isInternal(this)) {
            return getRaw().getId().toString();
        } else {
            return getId().toString();
        }
    }
}
