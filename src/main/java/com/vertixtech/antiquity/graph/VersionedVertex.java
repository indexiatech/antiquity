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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private VersionedGraph<?,V> graph;
	private V version;

	protected VersionedVertex(Vertex rawVertex, List<GraphChangedListener> graphChangedListeners, EventTrigger trigger, VersionedGraph<?,V> graph, V version) {
		super(rawVertex, graphChangedListeners, trigger);
		this.graph = graph;
		this.version = version;
	}

	public Iterable<Edge> getEdges(final Direction direction, final String... labels) {
        return new VersionedEdgeIterable<V>(((Vertex) this.baseElement).getEdges(direction, labels), this.graphChangedListeners, trigger, graph, version);
    }
	
	public Iterable<Vertex> getVertices(final Direction direction, final String... labels) {
        return new VersionedVertexIterable<V>(((Vertex) this.baseElement).getVertices(direction, labels), this.graphChangedListeners, trigger, graph, version);
    }
	
	public Object getProperty(String key) {
		return graph.getProperty(this, key);
	}
	
	public Set<String> getPropertyKeys() {
		return graph.getPropertyKeys(this);  
	}
	
	public V getVersion() {
		return this.version;
	}
	
	public void setVersion(V version) {
		this.version = version;
	}
}
