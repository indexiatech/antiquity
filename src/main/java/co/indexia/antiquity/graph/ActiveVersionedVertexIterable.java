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
import com.tinkerpop.blueprints.Vertex;

/**
 * A sequence of edges that wraps each vertex as {@link ActiveVersionedVertex}
 */
public class ActiveVersionedVertexIterable<V extends Comparable<V>> implements CloseableIterable<Vertex> {
    /**
     * The raw iterable retrieved from the underline blueprints graph.
     */
    Iterable<Vertex> rawIterable;

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
    public ActiveVersionedVertexIterable(Iterable<Vertex> rawIterable, ActiveVersionedGraph<?, V> graph) {
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
    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private Iterator<Vertex> itty = rawIterable.iterator();

            @Override
            public boolean hasNext() {
                return itty.hasNext();
            }

            @Override
            public Vertex next() {
                Vertex v = this.itty.next();

                if (v instanceof ActiveVersionedVertex) {
                    // TODO: May happen? if so mention when
                    return v;
                } else {
                    return new ActiveVersionedVertex<V>(v, graph);
                }
            }

            @Override
            public void remove() {
                // TODO: Does iter remove the element from the graph?
                this.itty.remove();
            }
        };
    }
}
