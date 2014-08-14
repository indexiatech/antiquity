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
import com.tinkerpop.blueprints.Vertex;
import co.indexia.antiquity.range.Range;

/**
 * A sequence of vertices that is filtered by validity according to the
 * specified version
 */
public class HistoricVersionedVertexIterable<V extends Comparable<V>> implements CloseableIterable<Vertex> {
    /**
     * Unfiltered raw iterable of vertices
     */
    private final Iterable<Vertex> iterable;

    /**
     * The associated graph instance.
     */
    private final HistoricVersionedGraph<?, V> graph;

    /**
     * the requested version range to filter upon.
     * 
     * If no version is set, all vertices will be returned, this is required in
     * order to return all vertices in the graph when
     * {@link co.indexia.antiquity.graph.HistoricVersionedGraph#getVertices()}
     * is invoked.
     */
    private final Range<V> version;

    /**
     * Create an instance of this class.
     * 
     * @param iterable
     * @param graph
     * @param version the requested version range to filter upon.
     */
    public HistoricVersionedVertexIterable(final Iterable<Vertex> iterable, final HistoricVersionedGraph<?, V> graph,
            Range<V> version) {
        Preconditions.checkNotNull(iterable, "Iterable must be set.");
        Preconditions.checkNotNull(graph, "Graph must be set.");
        // Preconditions.checkNotNull(version, "Version must be set.");

        this.iterable = iterable;
        this.graph = graph;
        this.version = version;
    }

    @Override
    public void close() {
        if (iterable instanceof CloseableIterable) {
            ((CloseableIterable<Vertex>) iterable).close();
        }
    }

    @Override
    public Iterator<Vertex> iterator() {
        return new Iterator<Vertex>() {
            private final Iterator<Vertex> itty = Iterables.filter(iterable,
                    new HistoricVersionedVertexPredicate<V>(graph, version)).iterator();

            @Override
            public void remove() {
                this.itty.remove();
            }

            @Override
            public Vertex next() {
                Vertex v = this.itty.next();
                Preconditions.checkArgument(graph.utils.getElementType(v) != VEProps.GRAPH_TYPE.ACTIVE,
                        "Vertex cannot be active.");

                if (v instanceof HistoricVersionedVertex) {
                    return v;
                } else {
                    Range<V> versionToFilterBy = null;

                    // If no version is specified (typically because
                    // h.getVertices() is invoked, we filter by start version.
                    if (version == null) {
                        V startVer = graph.utils.getStartVersion(v);
                        versionToFilterBy = Range.range(startVer, startVer);
                    } else {
                        versionToFilterBy = version;
                    }

                    return new HistoricVersionedVertex<V>(v, graph, versionToFilterBy);
                }
            }

            @Override
            public boolean hasNext() {
                return this.itty.hasNext();
            }
        };
    }
}
