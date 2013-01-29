/**
 * Copyright (c) 2012-2013 "Indexia Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package co.indexia.antiquity.graph;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Iterables;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

/**
 * A sequence of edges that is filtered by validity according to the specified version
 */
class VersionedEdgeIterable<V extends Comparable<V>> implements CloseableIterable<Edge> {

	private final Iterable<Edge> iterable;
	private final List<GraphChangedListener> graphChangedListeners;
	private final EventTrigger trigger;
	private final VersionedGraph<?, V> graph;
	private final V version;
	private final boolean withInternalEdges;

	public VersionedEdgeIterable(final Iterable<Edge> iterable, final List<GraphChangedListener> graphChangedListeners,
			final EventTrigger trigger, VersionedGraph<?, V> graph, V version, boolean withInternalEdges) {
		this.iterable = iterable;
		this.graphChangedListeners = graphChangedListeners;
		this.trigger = trigger;
		this.graph = graph;
		this.version = version;
		this.withInternalEdges = withInternalEdges;
	}

	@Override
	public Iterator<Edge> iterator() {
		return new Iterator<Edge>() {
			private final Iterator<Edge> itty = Iterables.filter(iterable,
					new VersionedVertexEdgePredicate<V>(graph, version, withInternalEdges)).iterator();

			@Override
			public void remove() {
				this.itty.remove();
			}

			@Override
			public Edge next() {
				Edge edge = this.itty.next();

				if (graph.isHistoricalOrInternal(edge) || (!graph.isVersionedEdge(edge))) {
					return edge;
				} else {
					return new VersionedEdge<V>(edge, graphChangedListeners, trigger, graph, version);
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
