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
import java.util.Set;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

/**
 * A {@link Vertex} implementation that supports versioning capabilities.
 */
public class VersionedVertex<V extends Comparable<V>> extends EventVertex {
	private final VersionedGraph<?, V> graph;
	private V forVersion;

	protected VersionedVertex(Vertex rawVertex,
			List<GraphChangedListener> graphChangedListeners,
			EventTrigger trigger,
			VersionedGraph<?, V> graph,
			V version) {
		super(rawVertex, graphChangedListeners, trigger);
		this.graph = graph;
		this.forVersion = version;
	}

	@Override
	public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
		return new VersionedEdgeIterable<V>(((Vertex) this.baseElement).getEdges(direction, labels),
				this.graphChangedListeners,
				trigger,
				graph,
				forVersion);
	}

	@Override
	public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
		return new VersionedVertexIterable<V>(((Vertex) this.baseElement).getVertices(direction, labels),
				this.graphChangedListeners,
				trigger,
				graph,
				forVersion);
	}

	@Override
	public Object getProperty(String key) {
		return graph.getProperty(this, key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		return graph.getPropertyKeys(this);
	}

	/**
	 * Get the forVersion property of the vertex.
	 * 
	 * This property defines the version context of the vertex, Properties and edges will be filtered according to the
	 * set version.
	 * 
	 * @return The current version bound to the vertex
	 */
	public V getForVersion() {
		return this.forVersion;
	}

	/**
	 * The forVersion property of the vertex
	 * 
	 * @see #getForVersion()
	 * @param forVersion
	 *            The forVersion property
	 */
	public void setForVersion(V forVersion) {
		this.forVersion = forVersion;
	}
}
