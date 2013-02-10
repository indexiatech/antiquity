package co.indexia.antiquity.graph;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.Iterator;
import java.util.List;

/**
 * A sequence of indices that applies the list of listeners into each element.
 */
class VersionedIndexIterable<T extends Element, V extends Comparable<V>> implements Iterable<Index<T>> {

    private final Iterable<Index<T>> iterable;
    private final List<GraphChangedListener> graphChangedListeners;
    private final EventTrigger trigger;
    private final VersionedGraph<?, V> graph;
    private final V version;

    public VersionedIndexIterable(final Iterable<Index<T>> iterable, List<GraphChangedListener> graphChangedListeners,
            final EventTrigger trigger, final VersionedGraph<?, V> graph, V version) {
        this.iterable = iterable;
        this.graphChangedListeners = graphChangedListeners;
        this.trigger = trigger;
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
                return new VersionedIndex<T, V>(this.itty.next(), graphChangedListeners, trigger, graph);
            }

            @Override
            public boolean hasNext() {
                return itty.hasNext();
            }
        };
    }
}
