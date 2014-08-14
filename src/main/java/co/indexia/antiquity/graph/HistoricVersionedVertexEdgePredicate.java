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
import com.google.common.base.Predicate;
import com.tinkerpop.blueprints.Edge;
import co.indexia.antiquity.range.Range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Predicate} for filtering edges that are not
 * contained within a specified version range.
 * 
 * @see HistoricVersionedEdgeIterable
 * @param <V> The graph identifier type
 */
public class HistoricVersionedVertexEdgePredicate<V extends Comparable<V>> implements Predicate<Edge> {
    Logger log = LoggerFactory.getLogger(HistoricVersionedVertexEdgePredicate.class);

    /**
     * The version to filter by
     */
    private final Range<V> version;

    /**
     * The associated graph instance
     */
    private final HistoricVersionedGraph<?, V> graph;

    /**
     * If true internal edges (e.g edges used to create element historical
     * chain) will be included.
     */
    private final boolean withInternalEdges;

    /**
     * Create an instance of this class.
     * 
     * @param graph The {@link ActiveVersionedGraph} instance associated with
     *        this predicate
     * @param version the requested version range to filter upon.
     * @param withInternalEdges if true internal edges will be included
     */
    public HistoricVersionedVertexEdgePredicate(HistoricVersionedGraph<?, V> graph, Range<V> version,
            boolean withInternalEdges) {
        Preconditions.checkNotNull(graph, "Graph must be set.");

        this.version = version;
        this.graph = graph;
        this.withInternalEdges = withInternalEdges;
    }

    @Override
    public boolean apply(Edge edge) {
        // if internals should be filtered
        if (!withInternalEdges && graph.utils.isInternal(edge)) {
            return false;
        }

        // if internals should be included
        if (withInternalEdges && graph.utils.isInternal(edge)) {
            return true;
        }


        graph.utils.ensureHistoricType(edge);

        // if no version is specified, we do not filter.
        if (version == null) {
            return true;
        }

        Range<V> edgeRange = graph.utils.getVersionRange(edge);
        boolean isEdgeInRange = edgeRange.contains(version);
        log.trace("Is edge[{}] with version [{}] is valid for version [{}] ? {}", edge, edgeRange, version,
                isEdgeInRange);

        return isEdgeInRange;
    }
}
