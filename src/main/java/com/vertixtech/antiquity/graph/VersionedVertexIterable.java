/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
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
package com.vertixtech.antiquity.graph;

import java.util.Iterator;
import java.util.List;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

/**
 */
public class VersionedVertexIterable<V extends Comparable<V>> implements CloseableIterable<Vertex> {

	private final Iterable<Vertex> iterable;
	private final List<GraphChangedListener> graphChangedListeners;
	private final EventTrigger trigger;
	private final VersionedGraph<?, V> graph;
	private final V version;

	public VersionedVertexIterable(final Iterable<Vertex> iterable,
			final List<GraphChangedListener> graphChangedListeners,
			final EventTrigger trigger,
			final VersionedGraph<?, V> graph,
			V version) {
		this.iterable = iterable;
		this.graphChangedListeners = graphChangedListeners;
		this.trigger = trigger;
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
			private final Iterator<Vertex> itty = iterable.iterator();

			@Override
			public void remove() {
				this.itty.remove();
			}

			@Override
			public Vertex next() {
				Vertex v = this.itty.next();

				if (graph.isHistoricalOrInternal(v)) {
					return v;
				} else {
					return new VersionedVertex<V>(v, graphChangedListeners, trigger, graph, version);
				}
			}

			@Override
			public boolean hasNext() {
				return this.itty.hasNext();
			}
		};
	}
}
