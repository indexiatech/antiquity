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

import java.util.Iterator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import co.indexia.antiquity.range.Range;

/**
 * A sequence of edges that is filtered by validity according to the specified
 * version
 */
class HistoricVersionedEdgeIterable<V extends Comparable<V>> implements CloseableIterable<Edge> {

    /**
     * Unfiltered raw iterable of edges
     */
    private final Iterable<Edge> iterable;

    /**
     * The associated graph instance.
     */
    private final HistoricVersionedGraph<?, V> graph;

    /**
     * the requested version range to filter upon.
     * 
     * If no version is set, all edges will be returned, this is required in
     * order to return all edges in the graph when
     * {@link co.indexia.antiquity.graph.HistoricVersionedGraph#getEdges()}
     * is invoked.
     */
    private final Range<V> version;

    /**
     * If true internal edges (e.g edges that creates historical vertices chain)
     * will be filtered.
     */
    private final boolean withInternalEdges;

    public HistoricVersionedEdgeIterable(final Iterable<Edge> iterable, HistoricVersionedGraph<?, V> graph,
            Range<V> version, boolean withInternalEdges) {
        Preconditions.checkNotNull(iterable, "Iterable must be set.");
        Preconditions.checkNotNull(graph, "Graph must be set.");

        this.iterable = iterable;
        this.graph = graph;
        this.version = version;
        this.withInternalEdges = withInternalEdges;
    }

    @Override
    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Edge> itty = Iterables.filter(iterable,
                    new HistoricVersionedVertexEdgePredicate<V>(graph, version, withInternalEdges)).iterator();

            @Override
            public void remove() {
                this.itty.remove();
            }

            @Override
            public Edge next() {
                Edge edge = this.itty.next();

                if (edge instanceof HistoricVersionedEdge) {
                    // TODO: May happen?
                    return edge;
                } else {
                    Boolean internal = graph.utils.isInternal(edge);

                    if (!internal) {
                        Preconditions.checkArgument(graph.utils.getElementType(edge) == VEProps.GRAPH_TYPE.HISTORIC,
                                "Edge must be historic");
                    }

                    // If no version is specified (typically because
                    // h.getEdges() is invoked, we filter by start version.
                    Range<V> versionToFilterBy;
                    if (version == null) {
                        V startVer = graph.utils.getStartVersion(edge);
                        versionToFilterBy = Range.range(startVer, startVer);
                    } else {
                        versionToFilterBy = version;
                    }

                    return new HistoricVersionedEdge<V>(edge, graph, versionToFilterBy);
                }
            }

            @Override
            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }

    @Override
    public void close() {
        if (this.iterable instanceof CloseableIterable) {
            ((CloseableIterable<Edge>) this.iterable).close();
        }
    }
}
