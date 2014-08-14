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

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;

/**
 * Version context is a graph wrapper which performs graph operations for a
 * certain version bound during context creation.
 * 
 * <p>
 * Note: This class is not thread safe.
 * </p>
 * 
 * @param <V> The graph's version identifier type
 */
public class VersionContextGraph<V extends Comparable<V>> implements Graph, WrapperGraph<HistoricVersionedGraph<?, V>> {
    /**
     * The version bound to this context
     */
    private final V version;

    /**
     * The {@link HistoricVersionedGraph} instance
     */
    private final HistoricVersionedGraph<?, V> graph;

    public VersionContextGraph(HistoricVersionedGraph<?, V> graph, V version) {
        this.version = version;
        this.graph = graph;
    }

    /**
     * A short static method for creating {@link VersionContextGraph} instance.
     * 
     * @param graph historic graph instance
     * @param version version of the context
     * @return instance of {@link VersionContextGraph} bound with the specified
     *         {@link ActiveVersionedGraph} and version.
     */
    public static <V extends Comparable<V>> VersionContextGraph<V> vc(HistoricVersionedGraph<?, V> graph, V version) {
        return new VersionContextGraph<V>(graph, version);
    }

    /**
     * Whether or not the specified vertex has a revision for the version in
     * context.
     * 
     * @param vertex The vertex to check
     * @return true if the graph contains a revision for the specified vertex
     *         for the version in context.
     */
    public boolean hasRevision(ActiveVersionedVertex<V> vertex) {
        return graph.getVertexForVersion(vertex, version) != null;
    }

    /**
     * Get a property value for the specified vertex and key in the bound
     * version
     * 
     * @param vertex vertex to return the value for
     * @param key key to return the value for
     * @return value of the specified vertex's property key for the bound
     *         version, null if not found.
     */
    public Object getProperty(ActiveVersionedVertex<V> vertex, String key) {
        Vertex v = graph.getVertexForVersion(vertex, version);
        return v.getProperty(key);
    }

    @Override
    public Features getFeatures() {
        return graph.getFeatures();
    }

    @Override
    public Vertex addVertex(Object id) {
        return graph.addVertex(id);
    }

    @Override
    public Vertex getVertex(Object id) {
        return graph.getVertexForVersion(id, version);
    }

    public Vertex getVertexByHardId(Object id) {
        HistoricVersionedVertex<V> v = graph.getVertexByHardId(id);

        if (v != null) {
            return graph.getVertexForVersion(v, version);
        }

        return null;
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return graph.getVertices(version);
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        return graph.getVertices(key, value, version);
    }

    @Override
    public Iterable<Edge> getEdges() {
        return graph.getEdges(version);
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        return graph.getEdges(key, value, version);
    }

    @Override
    public GraphQuery query() {
        return graph.query();
    }

    @Override
    public void shutdown() {
        graph.shutdown();

    }

    @Override
    public HistoricVersionedGraph<?, V> getBaseGraph() {
        return graph;
    }

    public V getVersion() {
        return version;
    }

    // -- Write protected methods
    @Override
    public void removeVertex(Vertex vertex) {
        graph.removeVertex(vertex);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return graph.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public Edge getEdge(Object id) {
        return graph.getEdge(id);
    }

    @Override
    public void removeEdge(Edge edge) {
        graph.removeEdge(edge);

    }
}
