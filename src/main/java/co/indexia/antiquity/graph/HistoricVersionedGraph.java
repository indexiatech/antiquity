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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyGraph;
import com.tinkerpop.blueprints.util.wrappers.readonly.ReadOnlyTokens;
import co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;
import co.indexia.antiquity.range.Range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link com.tinkerpop.blueprints.Graph} wrapper to query for historical
 * elements.
 * 
 * <p>
 * This class must be kept immutable.
 * </p>
 */
public class HistoricVersionedGraph<T extends KeyIndexableGraph, V extends Comparable<V>> extends
        VersionedGraphBase<T, V> implements WrapperGraph<T> {
    Logger log = LoggerFactory.getLogger(HistoricVersionedGraph.class);

    private final ReadOnlyGraph<T> baseGraph;

    public HistoricVersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Configuration conf) {
        super(baseGraph, identifierBehavior, conf);
        this.baseGraph = new ReadOnlyGraph(baseGraph);
        validate();
    }

    @Override
    protected void validate() {
        // TODO super.validate();
    }

    @Override
    public T getBaseGraph() {
        return baseGraph.getBaseGraph();
    }

    @Override
    public Vertex getRootVertex() {
        return getRootVertex(VEProps.GRAPH_TYPE.HISTORIC);
    }

    // Graph operation overrides
    // --------------------------------------------------------------

    // Get a vertex by the active id, the latest revision is returned.
    @Override
    public Vertex getVertex(Object id) {
        Preconditions.checkNotNull(id, "id must be set.");

        List<HistoricVersionedVertex<V>> chain = buildVertexChain(id);

        if (chain.size() == 0) {
            log.debug("Vertex with [{}] was not found.", id);
            return null;
        } else {
            return chain.get(0);
        }
    }

    public HistoricVersionedVertex<V> getVertexByHardId(Object id) {
        Preconditions.checkNotNull(id, "id must be set.");

        Vertex vertex;
        if (isNaturalIds()) {
            vertex = getSingleVertex(VEProps.NATURAL_VERTEX_ID_PROP_KEY, id);
        } else {
            vertex = getBaseGraph().getVertex(id);
        }

        if (vertex != null) {
            utils.ensureHistoricType(vertex);

            if (vertex instanceof HistoricVersionedVertex) {
                return (HistoricVersionedVertex<V>) vertex;
            } else {
                return new HistoricVersionedVertex<V>(vertex, this, Range.range(utils.getStartVersion(vertex),
                        utils.getStartVersion(vertex)));
            }
        } else {
            log.debug("Vertex with [{}] was not found.", id);
            return null;
        }
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return getVertices(null, null);
    }

    @Override
    public Iterable<Vertex> getVertices(String key, Object value) {
        // NOTE: This methods returns *ALL* vertices (including all revisions)
        // in the entire graph.
        if (key != null) {
            return query().has(key, value).vertices();
        } else {
            return query().vertices();
        }

    }

    // Get a edge by the active id, if multiple revisions found, the latest is
    // returned.
    @Override
    public Edge getEdge(Object id) {
        Preconditions.checkNotNull(id, "id must be set.");

        List<HistoricVersionedEdge<V>> chain = buildEdgeChain(id);

        if (chain.size() == 0) {
            log.debug("Edge with [{}] was not found.", id);
            return null;
        } else {
            return chain.get(0);
        }
    }

    public HistoricVersionedEdge<V> getEdgeByHardId(Object id) {
        Preconditions.checkNotNull(id, "id must be set.");

        Edge edge;
        if (isNaturalIds()) {
            edge = getSingleEdge(VEProps.NATURAL_EDGE_ID_PROP_KEY, id);
        } else {
            edge = getBaseGraph().getEdge(id);
        }

        if (edge != null) {
            if (edge instanceof HistoricVersionedEdge) {
                return (HistoricVersionedEdge<V>) edge;
            } else {
                utils.ensureHistoricType(edge);
                V start = utils.getStartVersion(edge);
                return new HistoricVersionedEdge<V>(edge, this, Range.range(start, start));
            }
        } else {
            log.debug("Edge with [{}] was not found.", id);
            return null;
        }
    }

    @Override
    public Iterable<Edge> getEdges() {
        return query().edges();
    }

    @Override
    public Iterable<Edge> getEdges(String key, Object value) {
        // NOTE: This methods returns *ALL* edges in the graph without internal
        // elements.
        return ((HistoricGraphQuery<V>) query()).withInternals(false).has(key, value).edges();
    }

    @Override
    public GraphQuery query() {
        return new HistoricGraphQuery(this, this.baseGraph.getBaseGraph().query());
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(Class<T> elementClass) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    // ---- write protected methods
    @Override
    public Vertex addVertex(Object id) {
        return baseGraph.addVertex(id);
    }

    @Override
    public void removeVertex(Vertex vertex) {
        baseGraph.removeVertex(vertex);
    }

    @Override
    public Edge addEdge(Object id, Vertex outVertex, Vertex inVertex, String label) {
        return baseGraph.addEdge(id, outVertex, inVertex, label);
    }

    @Override
    public void removeEdge(Edge edge) {
        baseGraph.removeEdge(edge);
    }

    @Override
    public <T extends Element> void dropKeyIndex(String key, Class<T> elementClass) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    @Override
    public <T extends Element> void createKeyIndex(String key, Class<T> elementClass, Parameter... indexParameters) {
        throw new UnsupportedOperationException(ReadOnlyTokens.MUTATE_ERROR_MESSAGE);
    }

    // Enhanced methods for the standard blueprint graph API
    // ------------------------------------------------------

    /**
     * Get a vertex revision from the specified active vertex.
     * 
     * @param active The active vertex to find the matched historic version for
     * @param version The version to find
     * @return a historic vertex revision for the specified version
     */
    public HistoricVersionedVertex<V> getVertexForVersion(ActiveVersionedVertex<V> active, V version) {
        HistoricVersionedVertex<V> latest = getLatestHistoricRevision(active);
        latest.setVersion(Range.range(version, version));

        return getMatchedHistoricVersion(latest, version);
    }

    /**
     * Get a vertex revision from the specified active vertex.
     * 
     * @param activeId The active vertex to find the matched historic version
     *        for
     * @param version The version to find
     * @return a historic vertex revision for the specified version
     */
    public HistoricVersionedVertex<V> getVertexForVersion(Object activeId, V version) {
        HistoricVersionedVertex<V> latest = (HistoricVersionedVertex<V>) getVertex(activeId);
        latest.setVersion(Range.range(version, version));

        return getMatchedHistoricVersion(latest, version);
    }


    public HistoricVersionedEdge<V> getEdgeForVersion(Object activeId, V version) {
        HistoricVersionedEdge<V> latest = (HistoricVersionedEdge<V>) getEdge(activeId);
        latest.setVersion(Range.range(version, version));
        Range<V> verRange = utils.getVersionRange(latest);

        if (verRange.contains(version)) {
            return latest;
        }

        return null;
    }

    public HistoricVersionedEdge<V> getEdgeForVersion(ActiveVersionedEdge<V> active, V version) {
        HistoricVersionedEdge<V> edge = getLatestHistoricRevision(active);
        edge.setVersion(Range.range(version, version));
        Range<V> verRange = utils.getVersionRange(edge);

        if (verRange.contains(version)) {
            return edge;
        }

        return null;
    }

    public HistoricVersionedVertex<V> getMatchedHistoricVersion(HistoricVersionedVertex later, V version) {
        Range<V> verRange = utils.getVersionRange(later);

        log.trace("Finding vertex[{}] in revision history for version [{}].", later, version);
        log.trace("Is vertex [{}] with range [{}] contains version [{}]?", later, verRange, version);

        if (!verRange.contains(version)) {
            Iterable<Edge> prevVerEdges = (later.getBaseElement()).getEdges(Direction.OUT, VEProps.PREV_VERSION_LABEL);

            if (!prevVerEdges.iterator().hasNext()) {
                // throw ExceptionFactory.notFoundException(String.format(
                // "Cannot find vertex %s in revision history for version [%s].",
                // later, version));
                log.info("Cannot find vertex %s in revision history for version [%s].", later, version);
                return null;
            }

            Edge rawEdge = prevVerEdges.iterator().next();
            Vertex rawVertex = rawEdge.getVertex(Direction.IN);
            HistoricVersionedVertex<V> nextLater =
                    new HistoricVersionedVertex<V>(rawVertex, this, Range.range(version, version));
            return getMatchedHistoricVersion((HistoricVersionedVertex<V>) nextLater, version);
        }

        log.debug("Found vertex[{}] in revision history for version [{}].", later, version);
        return later;
    }

    /**
     * Get all vertices for the specified version.
     * 
     * @param version The version to get all vertices for.
     * @return An {@link Iterable} of the found vertices for the specified
     *         version.
     */
    public HistoricVersionedVertexIterable<V> getVertices(V version) {
        Preconditions.checkNotNull(version, "Version must be specified.");

        return new HistoricVersionedVertexIterable<V>(getVertices(), this, Range.range(version, version));
    }

    /**
     * Return an iterable to all the vertices in the graph that have a
     * particular key/value property for the specified version.
     * 
     * @param key The key of the property to filter vertices by
     * @param value The value of the property to filter vertices by
     * @param version version The version to get the vertices for
     * @return An {@link Iterable} of the found vertices for the specified
     *         criteria.
     */
    public Iterable<Vertex> getVertices(final String key, final Object value, V version) {
        Preconditions.checkNotNull(key, "Key is required.");
        Preconditions.checkNotNull(version, "Version is required.");

        // TODO: Consider forbidding retrieving edges by internal keys
        // (especially NATURAL_VERTEX_ID_PROP_KEY), otherwise throw exception.
        return new HistoricVersionedVertexIterable<V>(getVertices(key, value), this, Range.range(version, version));
    }

    /**
     * Get a single historic vertex by the specified key / value criteria.
     * 
     * This method is not very useful as typically multiple elements will answer
     * the same criteria.
     * 
     * @param key key to match
     * @param value value to match
     * @return a single found element or exception if multiple elements found.
     */
    public Vertex getSingleVertex(String key, Object value) {
        return ElementUtils.getSingleElement(this, key, value, Vertex.class);
    }

    /**
     * Get a single historic edge by the specified key / value criteria.
     * 
     * This method is not very useful as typically multiple elements will answer
     * the same criteria.
     * 
     * @param key key to match
     * @param value value to match
     * @return a single found element or exception if multiple elements found.
     */
    public Edge getSingleEdge(String key, Object value) {
        return ElementUtils.getSingleElement(this, key, value, Edge.class);
    }

    /**
     * Return an iterable to all the edges in the graph for the specified
     * version
     * 
     * @param version The version to get the edges for
     * @return An {@link Iterable} of the found edges for the specified version.
     */
    public Iterable<Edge> getEdges(V version) {
        return ((HistoricGraphQuery<V>) query()).forVersion(version).withInternals(false).edges();
    }

    /**
     * Return an iterable to all the edges in the graph that have a particular
     * key/value property for the specified version.
     * 
     * @param key The key of the property to filter edges by
     * @param value The value of the property to filter edges by
     * @param version The version to get the edges for
     * @return An {@link Iterable} of the found edges for the specified criteria
     */
    public Iterable<Edge> getEdges(final String key, final Object value, V version) {
        // TODO: Consider forbidding retrieving edges by internal keys
        // (especially NATURAL_VERTEX_ID_PROP_KEY), otherwise throw exception.
        return ((HistoricGraphQuery<V>) query()).forVersion(version).edges();
    }

    /**
     * Get the latest historic revision for the specified active vertex.
     * 
     * @param a the vertex to find the latest historic revision for
     * @return the latest historic revision.
     */
    public HistoricVersionedVertex<V> getLatestHistoricRevision(ActiveVersionedVertex<V> a) {
        return getVertexByHardId(a.getProperty(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY));
    }

    /**
     * Get the latest historic revision for the specified historic vertex.
     * 
     * @param h one of the historic vertex in the chain
     * @return the latest historic revision.
     */
    public HistoricVersionedVertex<V> getLatestHistoricRevision(HistoricVersionedVertex<V> h) {
        return getLatestHistoricRevision(h.getProperty(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY));
    }

    /**
     * Get the latest historic revision for the specified id.
     * 
     * @param historicLatestId the id of the latest historic revision
     * @return the latest historic revision.
     */
    public HistoricVersionedVertex<V> getLatestHistoricRevision(Object historicLatestId) {
        // TODO: Ensure it is the latest version, otherwise fail with an
        // exception.
        return getVertexByHardId(historicLatestId);
    }


    /**
     * Get the latest historic revision for the specified active edge.
     * 
     * @param a the edge to find the latest historic revision for
     * @return the latest historic revision.
     */
    public HistoricVersionedEdge<V> getLatestHistoricRevision(ActiveVersionedEdge<V> a) {
        return getEdgeByHardId(a.getProperty(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY));
    }

    /**
     * Get the vertex (history) chain of the specified active vertex id. The
     * returned list is sorted in reserved order where the first element is the
     * latest committed revision.
     * 
     * @param activeId The active ID to get the historic chain for
     * @return reserved versions ordered list
     */
    public List<HistoricVersionedVertex<V>> buildVertexChain(Object activeId) {
        Iterable<Vertex> vertices = getVertices(VEProps.REF_TO_ACTIVE_ID_KEY, activeId);

        // TODO: Better approach to do this comparison?
        List<HistoricVersionedVertex<V>> historicVertices = new ArrayList<HistoricVersionedVertex<V>>();
        for (Vertex v : vertices) {
            historicVertices.add((HistoricVersionedVertex<V>) v);
        }

        return Ordering.from(utils.getHistoricVersionedVertexComparator()).reverse().sortedCopy(historicVertices);
    }

    /**
     * An overload method to {@link #buildVertexChain(Object)} but receive
     * active element instead.
     * 
     * @param a The vertex to get the chain for
     * @return reserved versions ordered list
     */
    public List<HistoricVersionedVertex<V>> buildVertexChain(ActiveVersionedVertex<V> a) {
        return buildVertexChain(a.getId());
    }

    /**
     * Get the edge (history) chain of the specified active edge id. The
     * returned list is sorted in reserved order where the first element is the
     * latest committed revision.
     * 
     * @param activeId The active ID to get the historic chain for
     * @return reserved versions ordered list
     */
    public List<HistoricVersionedEdge<V>> buildEdgeChain(Object activeId) {
        Iterable<Edge> edges = getEdges(VEProps.REF_TO_ACTIVE_ID_KEY, activeId);

        // TODO: Better approach to do this comparison?
        List<HistoricVersionedEdge<V>> historicEdges = new ArrayList<HistoricVersionedEdge<V>>();
        for (Edge e : edges) {
            historicEdges.add((HistoricVersionedEdge<V>) e);
        }

        return Ordering.from(utils.getHistoricVersionedEdgeComparator()).reverse().sortedCopy(historicEdges);
    }

    /**
     * An overload method to {@link #buildVertexChain(Object)} but receive
     * active element instead.
     * 
     * @param a The edge to get the chain for
     * @return reserved versions ordered list
     */
    public List<HistoricVersionedEdge<V>> buildEdgeChain(HistoricVersionedEdge<V> a) {
        return buildEdgeChain(a.getId());
    }
}
