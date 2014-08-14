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
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyEdge;
import co.indexia.antiquity.range.Range;

/**
 * A read-only {@link Edge} that represents a historic edge queried from
 * {@link HistoricVersionedGraph}.
 */
public class HistoricVersionedEdge<V extends Comparable<V>> extends HistoricVersionedElement<V, Edge> implements Edge {
    /**
     * the raw edge, wrapped as read-only.
     */
    private final ReadOnlyEdge edge;

    /**
     * Creates an instance.
     * 
     * @param rawEdge the base edge to be wrapped as read-only.
     * @param graph the graph instance this element is associated with.
     * @param version the requested version range associated elements will be
     *        filtered upon.
     */
    protected HistoricVersionedEdge(Edge rawEdge, HistoricVersionedGraph<?, V> graph, Range<V> version) {
        super(rawEdge, graph, version);

        if (rawEdge instanceof ReadOnlyEdge) {
            this.edge = (ReadOnlyEdge) rawEdge;
        } else {
            this.edge = new ReadOnlyEdge(rawEdge);
        }
    }

    @Override
    public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
        // HistoricVersionedVertex<V> latest =
        // new HistoricVersionedVertex<V>(getRaw().getVertex(direction),
        // getGraph(), getVersion());
        // edges are only attached to latest vertices.
        Vertex latest = getRaw().getVertex(direction);

        getGraph().utils.ensureHistoricType(latest);
        HistoricVersionedVertex<V> lhv = new HistoricVersionedVertex<V>(latest, getGraph(), getVersion());
        if (getGraph().utils.getVersionRange(latest).contains(getVersion().min())) {
            return lhv;
        } else {
            return getGraph().getMatchedHistoricVersion(lhv, getVersion().min());
        }
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
        Set<String> keys = edge.getPropertyKeys();
        keys.removeAll(VEProps.antiquityElementsKeys);

        return keys;
    }

    // ---- Write unsupported methods, protected by the read only vertex.
    @Override
    public void setProperty(String key, Object value) {
        edge.setProperty(key, value);
    }

    @Override
    public <T> T removeProperty(String key) {
        edge.removeProperty(key);
        // should never get here
        return null;
    }


    @Override
    public void remove() {
        edge.remove();
    }
}
