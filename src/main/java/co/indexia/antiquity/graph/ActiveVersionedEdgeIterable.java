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
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;

/**
 * A sequence of edges that wraps each edge as {@link ActiveVersionedEdge}.
 */
public class ActiveVersionedEdgeIterable<V extends Comparable<V>> implements CloseableIterable<Edge> {
    /**
     * The raw iterable retrieved from the underline blueprints graph.
     */
    Iterable<Edge> rawIterable;

    /**
     * The associated graph instance.
     */
    private final ActiveVersionedGraph<?, V> graph;

    /**
     * Creates an instance.
     * 
     * @param rawIterable the raw iterable retrieved from the underline
     *        blueprints graph.
     * @param graph the graph instance {@link this} associated with
     */
    public ActiveVersionedEdgeIterable(Iterable<Edge> rawIterable, ActiveVersionedGraph<?, V> graph) {
        Preconditions.checkNotNull(rawIterable, "Raw iterable must be set.");
        Preconditions.checkNotNull(graph, "Graph must be set.");

        this.rawIterable = rawIterable;
        this.graph = graph;
    }

    @Override
    public void close() {
        if (rawIterable instanceof CloseableIterable) {
            ((CloseableIterable) rawIterable).close();
        }
    }

    @Override
    public Iterator<Edge> iterator() {
        return new Iterator<Edge>() {
            private final Iterator<Edge> itty = rawIterable.iterator();

            public void remove() {
                this.itty.remove();
            }

            public Edge next() {
                Edge edge = this.itty.next();
                graph.utils.ensureActiveType(edge);

                if (edge instanceof ActiveVersionedEdge) {
                    // TODO: currently for safety, this condition may occur? if
                    // so then why?
                    return edge;
                } else {
                    return new ActiveVersionedEdge<V>(edge, graph);
                }
            }

            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
