package co.indexia.antiquity.graph;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;

import java.util.Iterator;

/**
 * A sequence of indices that applies the list of listeners into each element.
 */
class VersionedIndexIterable<T extends Element, V extends Comparable<V>> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;
    private final VersionedGraph<?, V> graph;
    private final V version;

    public VersionedIndexIterable(final Iterable<Index<T>> iterable, final VersionedGraph<?, V> graph, V version) {
        this.iterable = iterable;
        this.graph = graph;
        this.version = version;
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
                return new VersionedIndex<T, V>(this.itty.next(), graph);
            }

            @Override
            public boolean hasNext() {
                return itty.hasNext();
            }
        };
    }
}
