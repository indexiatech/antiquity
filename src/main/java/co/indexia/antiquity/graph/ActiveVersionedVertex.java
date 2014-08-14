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
import com.tinkerpop.blueprints.VertexQuery;
import com.tinkerpop.blueprints.util.wrappers.WrapperVertexQuery;
import com.tinkerpop.blueprints.util.wrappers.event.EventEdgeIterable;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertexIterable;

/**
 * A {@link com.tinkerpop.blueprints.Vertex} that represents an active
 * (up-to-date) vertex queried from the {@link ActiveVersionedGraph}.
 * 
 * Modifications to properties will create a new vertex instance in the vertex's
 * historic chain, the updated vertex will be updated in the active graph as is.
 * 
 * If new edge is added to this vertex, the edge will be versioned and added to
 * the historic graph plus added to the active graph as is.
 */
public class ActiveVersionedVertex<V extends Comparable<V>> extends ActiveVersionedElement<V, Vertex> implements Vertex {
    /**
     * The raw vertex wrapped with events support.
     */
    private final EventVertex vertex;

    /**
     * Creates an instance.
     * 
     * @param rawVertex the base vertex to be wrapped with events support.
     * @param graph the graph instance this vertex is associated with.
     */
    protected ActiveVersionedVertex(Vertex rawVertex, ActiveVersionedGraph<?, V> graph) {
        super(rawVertex instanceof EventVertex ? ((EventVertex) rawVertex).getBaseVertex() : rawVertex, graph);

        if (rawVertex instanceof EventVertex) {
            this.vertex = (EventVertex) rawVertex;
        } else {
            this.vertex = new EventVertex(rawVertex, graph.getEventableGraph());
        }
    }

    @Override
    public Iterable<Edge> getEdges(Direction direction, String... labels) {
        return new ActiveVersionedEdgeIterable<V>(vertex.getEdges(direction, labels), getGraph());
    }

    @Override
    public Iterable<Vertex> getVertices(Direction direction, String... labels) {
        return new ActiveVersionedVertexIterable<V>(vertex.getVertices(direction, labels), getGraph());
    }

    @Override
    public VertexQuery query() {
        return new WrapperVertexQuery(((Vertex) getRaw()).query()) {
            @Override
            public Iterable<Vertex> vertices() {
                return new ActiveVersionedVertexIterable<V>(this.query.vertices(), getGraph());
            }

            @Override
            public Iterable<Edge> edges() {
                return new ActiveVersionedEdgeIterable<V>(this.query.edges(), getGraph());
            }
        };
    }

    @Override
    public Edge addEdge(String label, Vertex inVertex) {
        return vertex.addEdge(label, inVertex);
    }

    @Override
    public <T> T getProperty(String key) {
        return vertex.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        return vertex.getPropertyKeys();
    }

    @Override
    public void setProperty(String key, Object value) {
        setPropertyIfChanged(key, value);
    }

    @Override
    public <T> T removeProperty(String key) {
        return vertex.removeProperty(key);
    }

    @Override
    public void remove() {
        vertex.remove();
    }

    /**
     * Set the specified value for the specified property key only if the new
     * value is different from the current property.
     * 
     * @param key key of the property
     * @param value value of the property
     */
    public void setPropertyIfChanged(String key, Object value) {
        boolean modified = !getPropertyKeys().contains(key);

        if (!modified) {
            modified = !getProperty(key).equals(value);
        }

        if (modified) {
            vertex.setProperty(key, value);
        }
    }

    /**
     * Get the underline wrapped eventable vertex
     * 
     * @return The underline eventable vertex.
     */
    public EventVertex getEventableVertex() {
        return vertex;
    }

    /**
     * Get the private hash of the vertex or null if no private hash found.
     * 
     * @see Configuration#privateVertexHashEnabled
     * @return the private hash of the vertex or null if no private hash found.
     */
    public String getPrivateHash() {
        return getRaw().getProperty(VEProps.PRIVATE_HASH_PROP_KEY);
    }
}
