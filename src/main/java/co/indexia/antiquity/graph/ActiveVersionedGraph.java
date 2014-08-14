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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrappedGraphQuery;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph.IdFactory;
import co.indexia.antiquity.graph.blueprints.EventGraph;
import co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;
import co.indexia.antiquity.range.Range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Graph} wrapper that adds versioning capabilities.
 * 
 * <p>
 * Antiquity maintains two separated disconnected graphs for high efficiency and
 * to separate the dirt historic data from the latest ('active') graph data.
 * </p>
 * The <i>active</i> graph contains the latest, up-to-date data, without
 * versioning support, in other words, the 'active' graph data looks exactly
 * like the graph as if Antiquity was not used at all.
 * <p>
 * 
 * </p>
 * The <i>historic</i> graph contains the elements history, every active element
 * has at least one corresponding historic element, each modification
 * (add/modify/delete) of elements in the active graph will produce new
 * corresponding historic element which stores the change.
 * 
 * use {@link #getHistoricGraph()} for a read-only {@link Graph} implementation
 * to query the historic data.
 * <p>
 * 
 * @see Graph
 * @see TransactionalGraph
 * @see NonTransactionalVersionedGraph
 * @see HistoricVersionedGraph
 */
public abstract class ActiveVersionedGraph<T extends KeyIndexableGraph, V extends Comparable<V>>
        extends VersionedGraphBase<T, V> implements GraphChangedListener, KeyIndexableGraph {
    Logger log = LoggerFactory.getLogger(ActiveVersionedGraph.class);

    /**
     * Wrapper graph with events support.
     */
    EventGraph<T> eventGraph;

    /**
     * Vertex ID factory. (required only if {@link #isNaturalIds()} is true)
     */
    private IdFactory vertexIdFactory;

    /**
     * Edge ID factory. (required only if {@link #isNaturalIds()} is true)
     */
    private IdFactory edgeIdFactory;

    /**
     * Reference to the historic graph instance
     */
    private HistoricVersionedGraph<T, V> hGraph;

    /**
     * Create an instance of this class.
     * 
     * @param baseGraph The base graph to wrap with versioning support.
     * @param identifierBehavior The graph identifier behavior implementation.
     * @param queue if true events will be queued, required for transactional
     *        implementation.
     */
    protected ActiveVersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Boolean queue) {
        this(baseGraph, identifierBehavior, null, null, null, queue);
    }

    /**
     * Create an instance of {@link ActiveVersionedGraph} with the specified
     * underline {@link Graph}.
     * 
     * @param baseGraph the underline base graph
     * @param identifierBehavior the graph identifier behavior implementation
     * @param conf the configuration instance of this instance.
     * @param vertexIdFactory the {@link IdFactory} of new vertices.
     * @param edgeIdFactory the {@link IdFactory} of new edges.
     * @param queue if true events will be queued, required for transactional
     *        implementation.
     */
    public ActiveVersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Configuration conf,
            final IdFactory vertexIdFactory, final IdFactory edgeIdFactory, boolean queue) {
        super(baseGraph, identifierBehavior, conf);

        this.eventGraph = new EventGraph<T>(baseGraph, queue);
        this.eventGraph.addListener(this);
        this.hGraph = new HistoricVersionedGraph<T, V>(baseGraph, identifierBehavior, conf);

        if (vertexIdFactory == null) this.vertexIdFactory = new DefaultIdFactory();
        if (edgeIdFactory == null) this.edgeIdFactory = new DefaultIdFactory();
    }


    // Graph general methods
    // --------------------------------------------------------------
    @Override
    public void validate() {
        super.validate();

        if (isNaturalIds()) {
            // TODO: This is needed?
            // ensure base graph supports vertex/edge,key indices
            Preconditions.checkState((getFeatures().supportsKeyIndices),
                    "With natural IDs enabled, The underline database must support vertex/edge,key indices.");

            this.vertexIdFactory = null == this.vertexIdFactory ? new DefaultIdFactory() : vertexIdFactory;
            this.edgeIdFactory = null == this.edgeIdFactory ? new DefaultIdFactory() : edgeIdFactory;
        }
    }

    /**
     * Initialize versioned graph data, this method expected to be invoked only
     * once for the whole life of the graph database.
     */
    public void init() {
        Vertex vertex = null;
        try {
            vertex = getRootVertex();
        } catch (IllegalStateException e) {
            log.info("Initializing graph...");
        }

        if (vertex != null) {
            return;
        }

        // Create the natural ID key indices
        KeyIndexableGraph keyIndexedGraph = getBaseGraph();
        if (!keyIndexedGraph.getIndexedKeys(Vertex.class).contains(VEProps.NATURAL_VERTEX_ID_PROP_KEY)) {
            keyIndexedGraph.createKeyIndex(VEProps.NATURAL_VERTEX_ID_PROP_KEY, Vertex.class);
        }

        if (!keyIndexedGraph.getIndexedKeys(Edge.class).contains(VEProps.NATURAL_EDGE_ID_PROP_KEY)) {
            keyIndexedGraph.createKeyIndex(VEProps.NATURAL_EDGE_ID_PROP_KEY, Edge.class);
        }

        //TODO: ROOT vertices should have a static unique known UUID for fast access
        Vertex historicRoot = utils.getNonEventableVertex(addActiveVertexInUnderline(null));
        historicRoot.setProperty(VEProps.ROOT_GRAPH_VERTEX_ID, VEProps.HISTORIC_ROOT_GRAPH_VERTEX_VALUE);
        historicRoot.setProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY, true);

        Vertex activeRoot = utils.getNonEventableVertex(addActiveVertexInUnderline(null));
        activeRoot.setProperty(VEProps.ROOT_GRAPH_VERTEX_ID, VEProps.ACTIVE_ROOT_GRAPH_VERTEX_VALUE);
        activeRoot.setProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY, false);

        if (getUneventableGraph() instanceof TransactionalGraph) {
            ((TransactionalGraph) getBaseGraph()).commit();
        }
    }


    /**
     * Return the unwrapped(Eventable)->unwrapped(The graph passed to {@link
     * this} to be wrapped) graph.
     * 
     * This is the concrete blueprints graph (e.g Neo4jGraph) wrapped by this
     * versioned graph.
     * 
     * This method is shell result the same instance as if
     * {@link #getBaseGraph()} was invoked.
     * 
     * @return The unwrapped->unwrapped graph.
     */
    public T getUneventableGraph() {
        return this.eventGraph.getBaseGraph();
    }

    /**
     * Return the underline eventable graph with the appropriate signature
     * 
     * @return An eventable graph
     */
    public EventGraph<T> getEventableGraph() {
        return this.eventGraph;
    }

    /**
     * Return the historic graph instance, used to query the historic graph.
     * 
     * @return A historic graph instance.
     */
    public HistoricVersionedGraph<T, V> getHistoricGraph() {
        return this.hGraph;
    }

    /**
     * Attach vertex to the corresponding {@link VEProps.GRAPH_TYPE} root vertex
     * 
     * @param vertex The vertex to be attached
     */
    public void attachToRoot(final Vertex vertex) {
        Vertex root = getRootVertex(utils.getElementType(vertex));
        addEdge(null, vertex, root, VEProps.ROOT_OF_EDGE_LABEL);
    }

    @Override
    public Vertex getRootVertex() {
        return getRootVertex(VEProps.GRAPH_TYPE.ACTIVE);
    }


    // Graph operation overrides
    // --------------------------------------------------------------
    @Override
    public Vertex addVertex(final Object id) {
        ActiveVersionedVertex vertex = addActiveVertexInUnderline(id);
        getEventableGraph().onVertexAdded(vertex.getEventableVertex());

        return vertex;
    }

    @Override
    public Vertex getVertex(final Object id) {
        Preconditions.checkNotNull(id, "id must be set.");

        Vertex vertex;
        if (isNaturalIds()) {
            vertex = getSingleVertex(VEProps.NATURAL_VERTEX_ID_PROP_KEY, id);
        } else {
            vertex = getEventableGraph().getVertex(id);
        }

        if (vertex != null) {
            if (vertex instanceof ActiveVersionedVertex) {
                return vertex;
            } else {
                utils.ensureActiveType(vertex);
                return new ActiveVersionedVertex<V>(vertex, this);
            }
        } else {
            log.debug("Vertex with [{}] was not found.", id);
            return null;
        }
    }

    @Override
    public Iterable<Vertex> getVertices() {
        return query().vertices();
    }

    @Override
    public Iterable<Vertex> getVertices(final String key, final Object value) {
        // We need to make Query to filter by key/value first to get a tinest
        // scope.
        // See DefaultQuery#hasContainers
        return query().has(key, value).vertices();
    }

    @Override
    public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex, final String label) {
        validateNewId(id, Edge.class);

        utils.ensureActiveType(outVertex);
        utils.ensureActiveType(inVertex);

        ActiveVersionedEdge<V> edge =
                addActiveEdgeInUnderline(id, (ActiveVersionedVertex<V>) outVertex, (ActiveVersionedVertex<V>) inVertex,
                        label);
        getEventableGraph().onEdgeAdded(edge);

        return edge;
    }

    @Override
    public Edge getEdge(Object id) {
        Preconditions.checkNotNull(id, "id must be set.");

        Edge edge;
        if (isNaturalIds()) {
            edge = ElementUtils.getSingleElement(this, VEProps.NATURAL_EDGE_ID_PROP_KEY, id, Edge.class);
        } else {
            edge = getEventableGraph().getEdge(id);
        }

        if (edge != null) {
            if (edge instanceof ActiveVersionedElement) {
                return edge;
            } else {
                utils.ensureActiveType(edge);
                return new ActiveVersionedEdge<V>(edge, this);
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
    public Iterable<Edge> getEdges(final String key, final Object value) {
        // TODO: Consider forbidding retrieving edges by internal keys
        // (especially NATURAL_EDGE_ID_PROP_KEY), otherwise throw exception.

        // We need to make Query to filter by key/value first to get a tinest
        // scope.
        // See DefaultQuery#hasContainers
        return query().has(key, value).edges();
    }

    @Override
    public void removeVertex(final Vertex vertex) {
        utils.ensureActiveType(vertex);
        getEventableGraph().removeVertex(((ActiveVersionedVertex) vertex).getEventableVertex());
    }

    @Override
    public void removeEdge(final Edge edge) {
        utils.ensureActiveType(edge);
        getEventableGraph().removeEdge(((ActiveVersionedEdge) edge).getEventableEdge());
    }

    @Override
    public GraphQuery query() {
        final ActiveVersionedGraph<T, V> ag = this;
        return new WrappedGraphQuery(getBaseGraph().query()) {
            @Override
            public Iterable<Edge> edges() {
                return new ActiveVersionedEdgeIterable<V>(getQuery().edges(), ag);
            }

            @Override
            public Iterable<Vertex> vertices() {
                return new ActiveVersionedVertexIterable<V>(getQuery().vertices(), ag);
            }

            public GraphQuery getQuery() {
                return this.query.has(VEProps.HISTORIC_ELEMENT_PROP_KEY, false);
            }
        };
    }

    @Override
    public <T extends Element> void dropKeyIndex(final String key, final Class<T> elementClass) {
        if (isNaturalIds()) {
            if (key.equals(VEProps.NATURAL_VERTEX_ID_PROP_KEY) || key.equals(VEProps.NATURAL_EDGE_ID_PROP_KEY)) {
                throw new IllegalArgumentException(String.format("Key [%s] is reserved and cannot be dropped.",
                        VEProps.NATURAL_VERTEX_ID_PROP_KEY));
            }
        }

        getEventableGraph().getBaseGraph().dropKeyIndex(key, elementClass);
    }

    @Override
    public <T extends Element> void createKeyIndex(final String key, final Class<T> elementClass,
            final Parameter... indexParameters) {
        if (key.equals(VEProps.NATURAL_VERTEX_ID_PROP_KEY) || key.equals(VEProps.NATURAL_EDGE_ID_PROP_KEY)) {
            throw new IllegalArgumentException(String.format("Index key [%s] is reserved and cannot be created",
                    VEProps.NATURAL_VERTEX_ID_PROP_KEY));
        }

        getEventableGraph().getBaseGraph().createKeyIndex(key, elementClass, indexParameters);
    }

    @Override
    public <T extends Element> Set<String> getIndexedKeys(final Class<T> elementClass) {
        final Set<String> keys = new HashSet<String>();
        keys.addAll(getEventableGraph().getBaseGraph().getIndexedKeys(elementClass));
        keys.remove(VEProps.NATURAL_VERTEX_ID_PROP_KEY);
        keys.remove(VEProps.NATURAL_EDGE_ID_PROP_KEY);
        return keys;
    }

    // Graph identifier methods
    // --------------------------------------------------------------

    /**
     * Get the next graph version.
     * 
     * @see #allocateNextGraphVersion(Comparable)
     * @param allocate whether to allocate the next version or not.
     * @return The next version of the graph
     */
    protected V getNextGraphVersion(boolean allocate) {
        V nextGraphVersion = identifierBehavior.getNextGraphVersion(getLatestGraphVersion());
        if (allocate) {
            allocateNextGraphVersion(nextGraphVersion);
        }

        // TODO: Unlock the configuration vertex if allocate=false, otherwise
        // unlock should occur during transaction
        // commit
        return nextGraphVersion;
    }

    /**
     * Allocate (persist) the specified next version in the graph in the
     * configuration vertex.
     * 
     * @param nextVersion The next version to allocate.
     */
    protected void allocateNextGraphVersion(V nextVersion) {
        // TODO: Unlock the configuration vertex
        getRootVertex(VEProps.GRAPH_TYPE.HISTORIC).setProperty(VEProps.LATEST_GRAPH_VERSION_PROP_KEY, nextVersion);
    }

    /**
     * Ensure that the new specified ID is valid.
     * 
     * Validation currently tests whether the specified ID already exist.
     * 
     * @param newId The new ID to verify
     * @param elementType The element type (Edge or Vertex)
     */
    public <T extends Element> void validateNewId(Object newId, Class<T> elementType) {
        if (isNaturalIds()) {
            boolean isVertex = (elementType.isAssignableFrom(Vertex.class));
            if (newId != null && (isVertex ? getVertex(newId) : getEdge(newId)) != null) {
                throw new IllegalArgumentException(String.format("%s with the given ID [%s] already exists.",
                        elementType.getSimpleName(), newId));
            }
        }
    }

    // Methods used by events responses
    // --------------------------------------------------------------
    /**
     * Version vertices in the graph.
     * 
     * Per created active vertex, create a corresponding historical one.
     * 
     * @param version The graph version that created the specified vertices
     * @param vertices The vertices to be versioned.
     */
    protected void versionAddedVertices(V version, Iterable<Vertex> vertices) {
        for (Vertex v : vertices) {
            utils.ensureActiveType(v);

            ActiveVersionedVertex<V> active = new ActiveVersionedVertex<V>(v, this);

            // Add corresponding historic vertex
            HistoricVersionedVertex<V> hv = addHistoricVertex(active, version, getMaxPossibleGraphVersion());
            utils.syncActiveAndLatestHistoric(active, hv);
            active.getRaw().setProperty(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY, hv.getHardId());

            if (conf.getPrivateVertexHashEnabled()) {
                utils.setPrivateHash(active);
            }
        }
    }

    /**
     * Version modified vertex.
     * 
     * @param latestGraphVersion The latest graph version
     * @param newVersion The new version to be committed
     * @param vertex The modified vertex
     * @param oldValues The old properties values of the modified vertex
     * 
     * @return The historical created vertex
     */
    protected Vertex versionModifiedVertex(V latestGraphVersion, V newVersion, Vertex vertex,
            Map<String, Object> oldValues) {
        ActiveVersionedVertex<V> active = new ActiveVersionedVertex<V>(vertex, this);
        HistoricVersionedVertex<V> latestHV = getHistoricGraph().getLatestHistoricRevision(active);

        // Note: order matters here, we need latestHV before we override it.
        HistoricVersionedVertex<V> newHV =
                addHistoricVertex(active, utils.getStartVersion(latestHV), latestGraphVersion);
        Set<String> excludedProps = new HashSet<String>();
        excludedProps.add(VEProps.NATURAL_VERTEX_ID_PROP_KEY);
        ElementUtils.copyProps(latestHV.getRaw(), newHV.getRaw(), excludedProps);

        // here it's safe to modify latest historic vertex.
        utils.setStartVersion(latestHV, newVersion);
        utils.syncActiveAndLatestHistoric(active, latestHV);

        addHistoricalVertexInChain(latestGraphVersion, newVersion, active, latestHV, newHV);

        if (conf.getPrivateVertexHashEnabled()) {
            utils.setPrivateHash(active);
        }

        return newHV;
    }

    /**
     * Version removed vertices
     * 
     * @param nextVer next version (to be committed) of the graph
     * @param maxVer current max version of the graph
     * @param vertices A map of removed vertices where key=removed vertex &
     *        value=A map of the removed vertex's properties.
     */
    protected void versionRemovedVertices(V nextVer, V maxVer, Map<Vertex, Map<String, Object>> vertices) {
        for (Map.Entry<Vertex, Map<String, Object>> v : vertices.entrySet()) {
            // we can't touch the vertex as it's deleted already
            // utils.ensureActiveType(v.getKey());
            // ActiveVersionedVertex<V> av = new
            // ActiveVersionedVertex<V>(v.getKey(), this);
            if (!v.getValue().containsKey(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY)) {
                throw new IllegalStateException("Expected removed vertx to contain key: "
                        + VEProps.REF_TO_LATEST_HISTORIC_ID_KEY);
            }

            HistoricVersionedVertex<V> hv =
                    getHistoricGraph().getLatestHistoricRevision(
                            (String) v.getValue().get(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY));

            // Remove ALL vertex's edges, must be invoked on getRaw to avoid
            // filtering.
            for (Edge e : hv.getRaw().getEdges(Direction.BOTH)) {
                if (utils.isInternal(e)) {
                    continue;
                }

                utils.ensureHistoricType(e);
                e.setProperty(VEProps.REMOVED_PROP_KEY, nextVer);
                e.setProperty(VEProps.VALID_MAX_VERSION_PROP_KEY, maxVer);
            }


            hv.getRaw().setProperty(VEProps.REMOVED_PROP_KEY, nextVer);
            hv.getRaw().setProperty(VEProps.VALID_MAX_VERSION_PROP_KEY, maxVer);
        }
    }

    /**
     * Version added edges in the graph.
     * 
     * Per created active edge, create a corresponding historical one.
     * 
     * @param version The graph version that created the specified edges
     * @param edges The edges to be versioned.
     */
    protected void versionAddedEdges(V version, Iterable<Edge> edges) {
        Range<V> range = Range.range(version, identifierBehavior.getMaxPossibleGraphVersion());

        for (Edge e : edges) {
            utils.ensureActiveType(e);

            ActiveVersionedEdge<V> ae = (ActiveVersionedEdge<V>) e;
            HistoricVersionedEdge ve =
                    addHistoricEdge((ActiveVersionedEdge<V>) e, version, getMaxPossibleGraphVersion(), null);
            utils.setVersion(ve, range);
            utils.getNonEventableElement(e).setProperty(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY, ve.getHardId());
            utils.syncActiveAndLatestHistoric(ae, ve);
        }
    }

    /**
     * Version removed edges
     * 
     * @param nextVer next version (to be committed) of the graph
     * @param maxVer current max version of the graph
     * @param edges A map of removed edges where key=removed edge & value=A map
     *        of the removed edge's properties.
     */
    protected void versionRemovedEdges(V nextVer, V maxVer, Map<Edge, Map<String, Object>> edges) {
        for (Map.Entry<Edge, Map<String, Object>> v : edges.entrySet()) {
            // we can't touch the edge as it's deleted already
            // utils.ensureActiveType(e);
            // ActiveVersionedEdge<V> av = new ActiveVersionedEdge<V>(e, this);
            // HistoricVersionedEdge<V> he =
            // getHistoricGraph().getLatestHistoricRevision(av);

            if (!v.getValue().containsKey(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY)) {
                throw new IllegalStateException("Expected removed vertx to contain key: "
                        + VEProps.REF_TO_LATEST_HISTORIC_ID_KEY);
            }

            HistoricVersionedEdge<V> he =
                    getHistoricGraph()
                            .getEdgeByHardId((String) v.getValue().get(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY));


            he.getRaw().setProperty(VEProps.REMOVED_PROP_KEY, nextVer);
            he.getRaw().setProperty(VEProps.VALID_MAX_VERSION_PROP_KEY, maxVer);
        }
    }

    /**
     * Version modified edge.
     * 
     * @param latestGraphVersion The latest graph version
     * @param newVersion The new version to be committed
     * @param edge The modified edge
     * @param oldValues The old properties values of the modified edge
     * 
     * @return The historical created edge
     */
    protected Edge versionModifiedEdge(V latestGraphVersion, V newVersion, Edge edge, Map<String, Object> oldValues) {
        ActiveVersionedEdge<V> active = new ActiveVersionedEdge<V>(edge, this);
        HistoricVersionedEdge<V> latestHE = getHistoricGraph().getLatestHistoricRevision(active);
        utils.syncActiveAndLatestHistoric(active, latestHE);


        return latestHE;
    }


    // Versioning helper methods
    // --------------------------------------------------------------
    /**
     * Add a plain vertex to the graph.
     * 
     * Note: this is not an eventable vertex thus no versioning will occur if
     * this element will be modified.
     * 
     * @param id The id of the vertex to set, if null, new id will be generated.
     * @return plain created vertex.
     */
    private Vertex addPlainVertexToGraph(Object id) {
        validateNewId(id, Vertex.class);
        final Vertex vertex;
        Object idVal = id == null ? vertexIdFactory.createId() : id;

        if (isNaturalIds()) {
            // we create an id just in case the underline doesn't ignore
            // supplied ids
            // and cannot recieve null but we ignore this id in the logic.
            vertex = getUneventableGraph().addVertex(vertexIdFactory.createId());
            vertex.setProperty(VEProps.NATURAL_VERTEX_ID_PROP_KEY, idVal);
        } else {
            vertex = getUneventableGraph().addVertex(idVal);
        }

        return vertex;
    }

    /**
     * Add an active vertex to the underline.
     * 
     * @param id specify explicit id, if null id will be generated, id may be
     *        ignored if underline ignores supplied ids.
     * @return The created active vertex.
     */
    private ActiveVersionedVertex addActiveVertexInUnderline(Object id) {
        Vertex vertex = addPlainVertexToGraph(id);
        vertex.setProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY, false);

        return new ActiveVersionedVertex<V>(vertex, this);
    }

    /**
     * Add a corresponding historic vertex to the specified active vertex.
     * 
     * Note: This method does not copy the properties from the active vertex to
     * the historic one.
     * 
     * @param a active vertex to add corresponding historic vertex for
     * @param startVersion the start version the historic vertex
     * @param endVersion the end version the historic vertex
     * @return an added historic vertex.
     */
    private HistoricVersionedVertex addHistoricVertex(ActiveVersionedVertex a, V startVersion, V endVersion) {
        Vertex vertex = addPlainVertexToGraph(null);
        vertex.setProperty(VEProps.REF_TO_ACTIVE_ID_KEY, a.getId());
        vertex.setProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY, true);

        // FIXME: Range is right?
        HistoricVersionedVertex hv =
                new HistoricVersionedVertex(vertex, this.getHistoricGraph(), Range.range(startVersion, startVersion));
        utils.setStartVersion(hv, startVersion);
        utils.setEndVersion(hv, endVersion);

        return hv;
    }

    /**
     * Add the historical vertex in the vertex versions chain
     * 
     * @param latestGraphVersion The latest graph version
     * @param newVersion The new version to be committed
     * @param activeModifiedVertex The active vertex that was modified.
     * @param latestHistoricVertex Latest historical vertex (the previous
     *        version of the newly created historic vertex)
     * @param newHistoricVertex The new historic vertex to be added in the chain
     */
    private void addHistoricalVertexInChain(V latestGraphVersion, V newVersion,
            ActiveVersionedVertex<V> activeModifiedVertex, HistoricVersionedVertex<V> latestHistoricVertex,
            HistoricVersionedVertex newHistoricVertex) {
        newHistoricVertex.getRaw().setProperty(VEProps.REF_TO_LATEST_HISTORIC_ID_KEY, latestHistoricVertex.getHardId());
        newHistoricVertex.getRaw().setProperty(VEProps.VALID_MAX_VERSION_PROP_KEY, latestGraphVersion);

        Edge prevEdge =
                ElementUtils.getSingleElement(latestHistoricVertex.getRaw().getEdges(Direction.OUT,
                        VEProps.PREV_VERSION_LABEL));

        if (prevEdge != null) {
            Vertex inVertex = prevEdge.getVertex(Direction.IN);
            getUneventableGraph().removeEdge(prevEdge);

            getBaseGraph().addEdge(edgeIdFactory.createId(), (Vertex) newHistoricVertex.getRaw(), inVertex,
                    VEProps.PREV_VERSION_LABEL);

        }

        getBaseGraph().addEdge(edgeIdFactory.createId(), latestHistoricVertex.getRaw(),
                (Vertex) newHistoricVertex.getRaw(), VEProps.PREV_VERSION_LABEL);
    }

    /**
     * Add the historical edge in the edge versions chain
     * 
     * @param latestGraphVersion The latest graph version
     * @param newVersion The new version to be committed
     * @param activeModifiedEdge The active edge that was modified.
     * @param latestHistoricEdge Latest historical edge (the previous version of
     *        the newly created historic edge)
     * @param newHistoricEdge The new historic edge to be added in the chain
     */
    private void addHistoricalEdgeInChain(V latestGraphVersion, V newVersion,
            ActiveVersionedEdge<V> activeModifiedEdge, HistoricVersionedEdge<V> latestHistoricEdge,
            HistoricVersionedEdge newHistoricEdge) {

        throw new UnsupportedOperationException(
                "This method is unsupported as version edges properties was configured to be disabled.");
    }



    /**
     * Add a plain edge to the graph.
     * 
     * Note: this is not an eventable vertex.
     * 
     * @param id The id of the edge to set, if null, new id will be generated.
     * @return plain created edge.
     */
    private Edge addPlainEdgeToGraph(Object id, Vertex out, Vertex in, String label) {
        validateNewId(id, Edge.class);
        final Edge edge;

        // TODO: Ensure we get raw vertices here.

        Object idVal = id == null ? edgeIdFactory.createId() : id;
        if (isNaturalIds()) {
            // we create an id just in case the underline doesn't ignore
            // supplied ids
            // and cannot recieve null but we ignore this id in the logic.
            edge = getUneventableGraph().addEdge(edgeIdFactory.createId(), out, in, label);
            edge.setProperty(VEProps.NATURAL_EDGE_ID_PROP_KEY, idVal);
        } else {
            edge = getUneventableGraph().addEdge(idVal, out, in, label);
        }

        return edge;
    }

    /**
     * Add an active edge to the underline.
     * 
     * @param id specify explicit id, if null id will be generated, id may be
     *        ignored if underline ignores supplied ids.
     * @param out the out vertex of the edge.
     * @param in the in vrtex of the edge.
     * @param label the label of the edge.
     * @return the created active edge.
     */
    private ActiveVersionedEdge addActiveEdgeInUnderline(Object id, ActiveVersionedVertex<V> out,
            ActiveVersionedVertex<V> in, String label) {
        Edge edge = addPlainEdgeToGraph(id, out.getRaw(), in.getRaw(), label);
        edge.setProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY, false);

        return new ActiveVersionedEdge<V>(edge, this);
    }

    /**
     * Add a corresponding historic edge to the specified active edge
     * 
     * @param a active edge to add corresponding historic edge for
     * @param startVersion the start version the historic edge
     * @param endVersion the end version the historic edge
     * @param oldValues The old (before modification) values of the specified
     *        active edge.
     * @return an added historic edge.
     */
    private HistoricVersionedEdge<V> addHistoricEdge(ActiveVersionedEdge<V> a, V startVersion, V endVersion,
            Map<String, Object> oldValues) {

        ActiveVersionedVertex<V> out = (ActiveVersionedVertex<V>) a.getVertex(Direction.OUT);
        ActiveVersionedVertex<V> in = (ActiveVersionedVertex<V>) a.getVertex(Direction.IN);

        if (out == null) {
            throw new IllegalStateException("Expected out vertex to exist.");
        }

        if (in == null) {
            throw new IllegalStateException("Expected in vertex to exist.");
        }

        HistoricVersionedVertex<V> hOut = getHistoricGraph().getLatestHistoricRevision(out);
        HistoricVersionedVertex<V> hIn = getHistoricGraph().getLatestHistoricRevision(in);

        Edge edge = addPlainEdgeToGraph(null, hOut.getRaw(), hIn.getRaw(), a.getLabel());
        edge.setProperty(VEProps.REF_TO_ACTIVE_ID_KEY, a.getId());
        edge.setProperty(VEProps.HISTORIC_ELEMENT_PROP_KEY, true);

        // FIXME: Range is right?
        HistoricVersionedEdge<V> hv =
                new HistoricVersionedEdge(edge, this.getHistoricGraph(), Range.range(startVersion, startVersion));
        utils.setStartVersion(hv, startVersion);
        utils.setEndVersion(hv, endVersion);

        return hv;
    }

    /**
     * Graph Builder.
     */
    public abstract static class ActiveVersionedGraphBuilder<T extends KeyIndexableGraph, V extends Comparable<V>> {
        Boolean init = false;
        T baseGraph;
        GraphIdentifierBehavior<V> identifierBehavior;
        Configuration conf;
        IdFactory vertexIdFactory;
        IdFactory edgeIdFactory;

        public ActiveVersionedGraphBuilder(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
            this.baseGraph = baseGraph;
            this.identifierBehavior = identifierBehavior;
        }


        public ActiveVersionedGraphBuilder init(Boolean init) {
            this.init = init;
            return this;
        }

        public ActiveVersionedGraphBuilder conf(Configuration conf) {
            this.conf = conf;
            return this;
        }

        public ActiveVersionedGraphBuilder vertexIdFactory(IdFactory vertexIdFactory) {
            this.vertexIdFactory = vertexIdFactory;
            return this;
        }

        public ActiveVersionedGraphBuilder edgeIdFactory(IdFactory edgeIdFactory) {
            this.edgeIdFactory = edgeIdFactory;
            return this;
        }

        public ActiveVersionedGraph<T, V> build() {
            ActiveVersionedGraph<T, V> instance = createInstance();
            if (init) {
                instance.init();
            }

            instance.validate();

            return instance;
        }

        protected abstract ActiveVersionedGraph<T, V> createInstance();
    }

    public static class ActiveVersionedTransactionalGraphBuilder<T extends KeyIndexableGraph & TransactionalGraph, V extends Comparable<V>>
            extends ActiveVersionedGraphBuilder<T, V> {
        public ActiveVersionedTransactionalGraphBuilder(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
            super(baseGraph, identifierBehavior);
        }

        public TransactionalVersionedGraph<T, V> build() {
            return (TransactionalVersionedGraph<T, V>) super.build();
        }

        protected TransactionalVersionedGraph<T, V> createInstance() {
            TransactionalVersionedGraph<T, V> instance =
                    new TransactionalVersionedGraph<T, V>(baseGraph, identifierBehavior, conf, vertexIdFactory,
                            edgeIdFactory);
            return instance;
        }
    }

    public static class ActiveVersionedNonTransactionalGraphBuilder<T extends KeyIndexableGraph, V extends Comparable<V>>
            extends ActiveVersionedGraphBuilder<T, V> {
        public ActiveVersionedNonTransactionalGraphBuilder(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
            super(baseGraph, identifierBehavior);
        }

        public NonTransactionalVersionedGraph<T, V> build() {
            return (NonTransactionalVersionedGraph<T, V>) super.build();
        }

        protected NonTransactionalVersionedGraph<T, V> createInstance() {
            return new NonTransactionalVersionedGraph<T, V>(baseGraph, identifierBehavior, conf, vertexIdFactory,
                    edgeIdFactory);
        }
    }
}
