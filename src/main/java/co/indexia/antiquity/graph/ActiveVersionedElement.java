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
import com.tinkerpop.blueprints.util.ElementHelper;
import com.tinkerpop.blueprints.util.wrappers.event.EventElement;
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyEdge;
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyVertex;

/**
 * Base class for {@link co.indexia.antiquity.graph.ActiveVersionedVertex}
 * and {@link co.indexia.antiquity.graph.ActiveVersionedEdge} active
 * elements implementations.
 */
public abstract class ActiveVersionedElement<V extends Comparable<V>, T extends Element> implements Element {
    /**
     * the graph instance this element is associated with.
     */
    private final ActiveVersionedGraph<?, V> graph;

    /**
     * The raw element as retrieved from the base blueprints graph
     * 
     * @see co.indexia.antiquity.graph.ActiveVersionedGraph#getBaseGraph()
     */
    private final T rawElement;

    /**
     * Creates an active element instance
     * 
     * @param rawElement raw element as retrieved from the base blueprints
     *        graph.
     * @param graph the graph instance this element is associated with.
     */
    public ActiveVersionedElement(T rawElement, ActiveVersionedGraph<?, V> graph) {
        Preconditions.checkNotNull(rawElement, "Raw element must be set.");
        Preconditions.checkNotNull(graph, "Graph must be set.");
        Preconditions.checkArgument((!(rawElement instanceof ActiveVersionedElement)),
                "rawElement cannot be instance of ActiveVersionElement");

        Preconditions.checkArgument(graph.utils.getElementType(rawElement) != VEProps.GRAPH_TYPE.HISTORIC,
                "Raw element cannot be historic");
        Preconditions.checkArgument((!(rawElement instanceof ActiveVersionedElement)));

        this.rawElement = rawElement;
        this.graph = graph;
    }

    @Override
    public Object getId() {
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

    /**
     * Get the graph instance associated with this element.
     * 
     * @return graph instance associated with this element.
     */
    protected ActiveVersionedGraph<?, V> getGraph() {
        return graph;
    }

    /**
     * Return the raw element.
     * 
     * @return raw element.
     */
    protected T getRaw() {
        if ((rawElement instanceof ActiveVersionedElement) || (rawElement instanceof HistoricVersionedElement)
                || (rawElement instanceof EventElement) || (rawElement instanceof ReadOnlyEdge)
                || (rawElement instanceof ReadOnlyVertex)) {
            throw new IllegalStateException("Incorrect wrapped rawElement.");
        }
        return this.rawElement;
    }

    @Override
    public String toString() {
        return getId().toString();
    }

    @Override
    public int hashCode() {
        return rawElement.hashCode();
    }

    @Override
    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }
}
