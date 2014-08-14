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
package co.indexia.antiquity.graph.blueprints;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;

/**
 * Increased visibility of
 * {@link com.tinkerpop.blueprints.util.wrappers.event.EventGraph}
 */
public class EventGraph<T extends KeyIndexableGraph> extends
        com.tinkerpop.blueprints.util.wrappers.event.EventGraph<T> {

    /**
     * Create a graph wrapper with events support.
     * 
     * @param baseIndexableGraph The base graph to wrap, this is usually the
     *        root level blueprint graph (e.g
     *        {@link com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph}
     * @param queue true if events should be queued until
     *        {@link EventTrigger#fireEventQueue()} is invoked.
     */
    public EventGraph(T baseIndexableGraph, boolean queue) {
        super(baseIndexableGraph);
        this.trigger = new EventTrigger(this, queue);
    }

    @Override
    public void onVertexAdded(Vertex vertex) {
        super.onVertexAdded(vertex);
    }

    @Override
    public void onEdgeAdded(Edge edge) {
        super.onEdgeAdded(edge);
    }
}
