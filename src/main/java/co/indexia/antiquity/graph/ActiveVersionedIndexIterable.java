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

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;

/**
 * A sequence of indices that applies the list of listeners into each element.
 */
class ActiveVersionedIndexIterable<T extends Element, V extends Comparable<V>> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;
    private final ActiveVersionedGraph<?, V> graph;

    public ActiveVersionedIndexIterable(final Iterable<Index<T>> iterable, final ActiveVersionedGraph<?, V> graph) {
        this.iterable = iterable;
        this.graph = graph;
    }

    @Override
    public Iterator<Index<T>> iterator() {
        return new Iterator<Index<T>>() {
            private final Iterator<Index<T>> itty = iterable.iterator();

            @Override
            public void remove() {
                this.itty.remove();
            }

            @Override
            public Index<T> next() {
                return new ActiveVersionedIndex<T, V>(this.itty.next(), graph);
            }

            @Override
            public boolean hasNext() {
                return itty.hasNext();
            }
        };
    }
}
