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

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.event.EventElement;
import com.tinkerpop.blueprints.util.wrappers.event.EventIndex;

/**
 * An @{link Index} that decorates active versioned elements.
 */
public class ActiveVersionedIndex<T extends Element, V extends Comparable<V>> implements Index<T> {
    protected final EventIndex<T> eventIndex;
    private final ActiveVersionedGraph<?, V> graph;

    public ActiveVersionedIndex(final Index<T> rawIndex, final ActiveVersionedGraph<?, V> graph) {
        this.eventIndex = (EventIndex)rawIndex;
        this.graph = graph;
    }

    @Override
    public void remove(final String key, final Object value, final T element) {
        this.eventIndex.remove(key, value, (T) getEventElement(element));
    }

    @Override
    public void put(final String key, final Object value, final T element) {
        this.eventIndex.put(key, value, (T) getEventElement(element));
    }

    @Override
    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return new ActiveVersionedVertexIterable((Iterable) this.eventIndex.get(key, value), graph);
        } else {
            return (CloseableIterable<T>) new ActiveVersionedEdgeIterable<V>((Iterable<Edge>) this.eventIndex.get(key,
                    value), graph);
        }
    }

    @Override
    public CloseableIterable<T> query(final String key, final Object query) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return (CloseableIterable<T>) new ActiveVersionedVertexIterable(this.eventIndex.query(key, query), graph);
        } else {
            return (CloseableIterable<T>) new ActiveVersionedEdgeIterable(this.eventIndex.query(key, query), graph);
        }
    }

    @Override
    public long count(final String key, final Object value) {
        return this.eventIndex.count(key, value);
    }

    @Override
    public String getIndexName() {
        return this.eventIndex.getIndexName();
    }

    @Override
    public Class<T> getIndexClass() {
        return this.eventIndex.getIndexClass();
    }

    @Override
    public String toString() {
        return StringFactory.indexString(this);
    }

    private EventElement getEventElement(Element ae) {
        Preconditions.checkArgument(ae instanceof ActiveVersionedElement, "Element must be active.");

        EventElement eventElement = null;
        if (ae instanceof ActiveVersionedVertex) {
            eventElement = ((ActiveVersionedVertex) ae).getEventableVertex();
        } else {
            eventElement = ((ActiveVersionedEdge) ae).getEventableEdge();
        }

        return eventElement;
    }
}
