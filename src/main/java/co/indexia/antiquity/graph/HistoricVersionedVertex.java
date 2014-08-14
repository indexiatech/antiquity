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
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyVertex;
import co.indexia.antiquity.range.Range;

/**
 * A read-only {@link Vertex} that represents an historic vertex queried from
 * {@link HistoricVersionedGraph}.
 */
public class HistoricVersionedVertex<V extends Comparable<V>> extends HistoricVersionedElement<V, Vertex> implements
        Vertex {
    /**
     * the raw vertex, wrapped as read-only.
     */
    private final ReadOnlyVertex vertex;

    /**
     * Creates an instance.
     * 
     * @param rawVertex the base vertex to be wrapped as read-only.
     * @param graph the graph instance this element is associated with.
     * @param version the requested version range to filter upon.
     */
    protected HistoricVersionedVertex(Vertex rawVertex, HistoricVersionedGraph<?, V> graph, Range<V> version) {
        super(rawVertex, graph, version);

        // this if condition may happen if rawVertex was created via
        // HistoricVersionedEdge#getVertex(Direction direction)
        if (rawVertex instanceof ReadOnlyVertex) {
            this.vertex = (ReadOnlyVertex) rawVertex;
        } else {
            this.vertex = new ReadOnlyVertex(rawVertex);
        }
    }

    @Override
    public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return getEdges(direction, false, labels);
    }

    /**
     * Return the historic edges associated with this vertex instance.
     * 
     * @param direction The direction of the edges to be retrieved.
     * @param internalEdges if true internal edges used for antiquity purposes
     *        only will be included in the result as well.
     * @param labels The labels of the edges to be retrieved.
     * @return an historic iterable of the edges that matches the criteria.
     */
    public Iterable<Edge> getEdges(final Direction direction, boolean internalEdges, final String... labels) {
        // FIXME: If no key that means we'r latest, consider replacing to a
        // safer approach
        if (getPropertyKeys(true).contains(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY)) {
            HistoricVersionedVertex<V> latest = getGraph().getLatestHistoricRevision(this);
            latest.setVersion(getVersion());
            return latest.getEdges(direction, labels);
        } else {
            return new HistoricVersionedEdgeIterable<V>((getRaw().getEdges(direction, labels)), getGraph(),
                    getVersion(), internalEdges);
        }
    }

    @Override
    public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new HistoricVersionedVertexIterable<V>((getRaw()).getVertices(direction, labels), getGraph(),
                getVersion());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object getProperty(String key) {
        return vertex.getProperty(key);
    }

    @Override
    public Set<String> getPropertyKeys() {
        Set<String> keys = getBaseElement().getPropertyKeys();
        keys.removeAll(VEProps.antiquityElementsKeys);

        return keys;
    }

    protected Set<String> getPropertyKeys(boolean withInternals) {
        if (withInternals)
            return getBaseElement().getPropertyKeys();
        else
            return this.getPropertyKeys();
    }

    @Override
    public VertexQuery query() {
        // return vertex.query();
        throw new UnsupportedOperationException("Query is currently unsupported.");
    }

    // ---- Write unsupported methods, protected by the read only vertex.
    @Override
    public Edge addEdge(String label, Vertex inVertex) {
        return vertex.addEdge(label, inVertex);
    }

    @Override
    public void setProperty(String key, Object value) {
        vertex.setProperty(key, value);
    }

    @Override
    public <T> T removeProperty(String key) {
        vertex.removeProperty(key);
        // should never get here
        return null;
    }

    @Override
    public void remove() {
        vertex.remove();
    }

    public ReadOnlyVertex getBaseElement() {
        return vertex;
    }
}
