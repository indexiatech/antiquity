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
	/**
	 * The graph that this instance is bound with.
	 */

	private final VersionedGraph<?, V> graph;
	/**
	 * Defines the version context of the vertex, properties and edges associated with this vertex will match the
	 * specified version.
	 */
	private V forVersion;

	/**
	 * Whether this this is transient or not, Transient indicates that this instance was just created and was never
	 * persisted before,
	 *
	 * Several commands such as getting properties or associated edges will throw an exception when dealing with
	 * transient vertices.
	 */
	private boolean trans;

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
		return getEdges(direction, false, labels);
	}

	public Iterable<Edge> getEdges(final Direction direction, boolean internalEdges, final String... labels) {
		operationNotSupportedForTransient(this);

		return new VersionedEdgeIterable<V>(((Vertex) this.baseElement).getEdges(direction, labels),
				this.graphChangedListeners,
				trigger,
				graph,
				forVersion, internalEdges);
	}

	@Override
	public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
		operationNotSupportedForTransient(this);
		return new VersionedVertexIterable<V>(((Vertex) this.baseElement).getVertices(direction, labels),
				this.graphChangedListeners,
				trigger,
				graph,
				forVersion);
	}

	/**
	 * Set the specified value for the specified property key only if the new value is different from the current
	 * property.
	 *
	 * @param key
	 * @param value
	 */
	public void setPropertyIfChanged(String key, Object value) {
		if ((getPropertyKeys().contains(key)) && (!getProperty(key).equals(value))) {
			setProperty(key, value);
		}
	}

	@Override
	public Object getProperty(String key) {
		operationNotSupportedForTransient(this);
		return graph.getProperty(this, key);
	}

	@Override
	public Set<String> getPropertyKeys() {
		operationNotSupportedForTransient(this);
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

	/**
	 * Whether this instance is transient or not.
	 *
	 * @return true if this instance is transient.
	 */
	public boolean isTrans() {
		return trans;
	}

	/**
	 * Set the transient property.
	 *
	 * @param trans
	 *            The value to set
	 */
	public void setTrans(boolean trans) {
		this.trans = trans;
	}

	/**
	 * Throw an exception that the operation is unsupported in case this instance is transient.
	 *
	 * @param v
	 *            The vertex to test.
	 */
	private void operationNotSupportedForTransient(VersionedVertex<V> v) {
		if (v.isTrans())
			throw new IllegalStateException(String.format("The operation is not supported by transient vertex[%s]", v));
	}

    @Override
    public Object getId() {
        if (graph.isNaturalIds()) {
            return baseElement.getProperty(VersionedGraph.NATURAL_ID_PROP_KEY);
        } else {
            return super.getId();
        }
    }
}
