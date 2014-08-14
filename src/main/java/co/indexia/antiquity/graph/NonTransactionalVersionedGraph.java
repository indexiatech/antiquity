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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph.IdFactory;
import co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A non transactional {@link ActiveVersionedGraph} implementation.
 * 
 * @param <T> The type of the graph
 * @param <V> The type of the graph version
 */
public class NonTransactionalVersionedGraph<T extends KeyIndexableGraph, V extends Comparable<V>>
        extends ActiveVersionedGraph<T, V> {
    Logger log = LoggerFactory.getLogger(NonTransactionalVersionedGraph.class);

    /**
     * Create an instance of this class.
     * 
     * @param baseGraph The base class to wrap with versioning support
     * @param identifierBehavior The graph identifier behavior implementation.
     */
    public NonTransactionalVersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
        super(baseGraph, identifierBehavior, false);
    }

    /**
     * Create an instance of this class.
     * 
     * @param baseGraph The base class to wrap with versioning support
     * @param identifierBehavior The graph identifier behavior implementation.
     * @param conf The configuration instance of this instance.
     */
    NonTransactionalVersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Configuration conf,
            IdFactory vertexIdFactory, IdFactory edgeIdFactory) {
        super(baseGraph, identifierBehavior, conf, vertexIdFactory, edgeIdFactory, false);
    }

    // Versioned Graph Events
    // --------------------------------------------------------------
    @Override
    public void vertexAdded(Vertex vertex) {
        log.debug("==Vertex [{}] added==", vertex);
        versionAddedVertices(getNextGraphVersion(true), Arrays.asList(vertex));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue) {
        log.debug("==Vertex [{}] property[{}] was modified [{} -> {}]==", vertex, key, oldValue, setValue);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(key, oldValue);

        versionModifiedVertex(getLatestGraphVersion(), getNextGraphVersion(true), vertex, props);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue) {
        log.debug("==Vertex property [{}] was removed [{}->{}]==", vertex, removedValue);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(key, removedValue);

        versionModifiedVertex(getLatestGraphVersion(), getNextGraphVersion(true), vertex, props);
    }

    @Override
    public void vertexRemoved(Vertex vertex, Map<String, Object> props) {
        log.debug("==Vertex [{}] removed==", vertex);
        V last = getLatestGraphVersion();
        Map<Vertex, Map<String, Object>> vertices = new HashMap<Vertex, Map<String, Object>>();
        vertices.put(vertex, props);
        versionRemovedVertices(getNextGraphVersion(true), last, vertices);
    }

    @Override
    public void edgeAdded(Edge edge) {
        log.debug("==Edge [{}] added==", edge);
        versionAddedEdges(getNextGraphVersion(true), Arrays.asList(edge));
    }

    @Override
    public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue) {
        log.debug("==Edge [{}] property[{}] was modified [{} -> {}]==", edge, key, oldValue, setValue);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(key, oldValue);

        versionModifiedEdge(getLatestGraphVersion(), getNextGraphVersion(true), edge, props);
    }

    @Override
    public void edgePropertyRemoved(Edge edge, String key, Object removedValue) {
        log.debug("==Edge property [{}] was removed [{}->{}]==", edge, removedValue);

        Map<String, Object> props = new HashMap<String, Object>();
        props.put(key, removedValue);

        versionModifiedEdge(getLatestGraphVersion(), getNextGraphVersion(true), edge, props);
    }

    @Override
    public void edgeRemoved(Edge edge, Map<String, Object> props) {
        log.debug("==Edge [{}] removed==", edge);
        V last = getLatestGraphVersion();

        Map<Edge, Map<String, Object>> edges = new HashMap<Edge, Map<String, Object>>();
        edges.put(edge, props);
        versionRemovedEdges(getNextGraphVersion(true), last, edges);
    }
}
