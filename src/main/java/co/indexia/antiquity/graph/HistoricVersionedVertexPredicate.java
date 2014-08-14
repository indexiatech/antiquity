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
import com.tinkerpop.blueprints.Vertex;
import co.indexia.antiquity.range.Range;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of {@link Predicate} for filtering vertices that are not
 * contained within a specified version range.
 * 
 * @see HistoricVersionedEdgeIterable
 * @param <V> The graph identifier type
 */
public class HistoricVersionedVertexPredicate<V extends Comparable<V>> implements Predicate<Vertex> {
    Logger log = LoggerFactory.getLogger(HistoricVersionedVertexPredicate.class);
    private final Range<V> version;
    private final HistoricVersionedGraph<?, V> graph;

    /**
     * Create an instance of this class.
     * 
     * @param graph The {@link HistoricVersionedGraph} instance associated with
     *        this predicate
     * @param version the requested version range to filter upon.
     */
    public HistoricVersionedVertexPredicate(HistoricVersionedGraph<?, V> graph, Range<V> version) {
        Preconditions.checkNotNull(graph, "Graph must be set.");

        this.version = version;
        this.graph = graph;
    }

    @Override
    public boolean apply(Vertex vertex) {
        if (graph.utils.isInternal(vertex)) {
            return false;
        }

        graph.utils.ensureHistoricType(vertex);

        // we dont filter if no version is specified.
        if (version == null) {
            return true;
        }

        Range<V> r = graph.utils.getVersionRange(vertex);
        boolean isElementInRange = r.contains(version);
        log.debug("Is vertex[{}] [{}] is valid for version [{}] ? {}", vertex, r, version, isElementInRange);

        return isElementInRange;
    }
}
