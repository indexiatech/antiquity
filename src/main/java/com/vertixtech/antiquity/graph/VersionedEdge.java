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

import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventEdge;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

/**
 * An {@link Edge} implementation that supports versioning capabilities.
 */
public class VersionedEdge<V extends Comparable<V>> extends EventEdge {
	private final VersionedGraph<?, V> graph;
	private final V version;

	protected VersionedEdge(Edge rawEdge,
			List<GraphChangedListener> graphChangedListeners,
			EventTrigger trigger,
			VersionedGraph<?, V> graph,
			V version) {
		super(rawEdge, graphChangedListeners, trigger);
		this.graph = graph;
		this.version = version;
	}

	@Override
	public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
		return new VersionedVertex<V>(this.getBaseEdge().getVertex(direction),
				this.graphChangedListeners,
				this.trigger,
				graph,
				version);
	}
}
