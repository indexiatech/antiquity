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

import java.util.Set;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventEdge;

/**
 * An {@link com.tinkerpop.blueprints.Edge} that represents an
 * active(up-to-date) edge queried from {@link ActiveVersionedGraph}.
 */
public class ActiveVersionedEdge<V extends Comparable<V>> extends ActiveVersionedElement<V, Edge> implements Edge {
    /**
     * The raw edge, wrapped with events support.
     */
    private final EventEdge edge;

    /**
     * Creates a new instance.
     * 
     * @param rawEdge the base edge to be wrapped with events support.
     * @param graph the graph instance this element is associated with.
     */
    protected ActiveVersionedEdge(Edge rawEdge, ActiveVersionedGraph<?, V> graph) {
        super(rawEdge instanceof EventEdge ? ((EventEdge) rawEdge).getBaseEdge() : rawEdge, graph);

        if (rawEdge instanceof EventEdge) {
            // this occurs
            this.edge = (EventEdge) rawEdge;
        } else {
            this.edge = new EventEdge(rawEdge, graph.getEventableGraph());
        }
    }

    @Override
    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        return new ActiveVersionedVertex<V>(this.edge.getVertex(direction), getGraph());
    }

    @Override
    public String getLabel() {
        return edge.getLabel();
    }

    @Override
    public <T> T getProperty(String key) {
        // Currently edge's properties versioning is unsupported.
        return edge.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        // Currently edge's properties versioning is unsupported.
        return edge.getPropertyKeys();
    }

    @Override
    public void setProperty(String key, Object value) {
        edge.setProperty(key, value);
    }

    @Override
    public <T> T removeProperty(String key) {
        return edge.removeProperty(key);
    }

    @Override
    public void remove() {
        edge.remove();
    }

    /**
     * Get the underline wrapped eventable edge
     * 
     * @return The underline eventable edge.
     */
    public EventEdge getEventableEdge() {
        return edge;
    }
}
