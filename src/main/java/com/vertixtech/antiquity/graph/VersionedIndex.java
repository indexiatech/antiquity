/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
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
package com.vertixtech.antiquity.graph;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.event.EventElement;

/**
 * An @{link Index} that decorates versioned graph elements.
 */
public class VersionedIndex<T extends Element, V extends Comparable<V>> implements Index<T> {
    protected final Index<T> rawIndex;
    private final ActiveVersionedGraph<?, V> graph;

    public VersionedIndex(final Index<T> rawIndex, final ActiveVersionedGraph<?, V> graph) {
        this.rawIndex = rawIndex;
        this.graph = graph;
    }

    @Override
    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value, (T) ((EventElement) element).getBaseElement());
    }

    @Override
    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value, (T) ((EventElement) element).getBaseElement());
    }

    @Override
    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return new ActiveVersionedVertexIterable((Iterable) this.rawIndex.get(key, value), graph);
        } else {
            return (CloseableIterable<T>) new ActiveVersionedEdgeIterable<V>((Iterable<Edge>) this.rawIndex.get(key,
                    value), graph);
        }
    }

    @Override
    public CloseableIterable<T> query(final String key, final Object query) {
        throw new IllegalArgumentException("Query currently not supported.");
    }

    @Override
    public long count(final String key, final Object value) {
        return this.rawIndex.count(key, value);
    }

    @Override
    public String getIndexName() {
        return this.rawIndex.getIndexName();
    }

    @Override
    public Class<T> getIndexClass() {
        return this.rawIndex.getIndexClass();
    }

    @Override
    public String toString() {
        return StringFactory.indexString(this);
    }
}
