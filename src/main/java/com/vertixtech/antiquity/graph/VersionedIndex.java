package com.vertixtech.antiquity.graph;

import com.tinkerpop.blueprints.CloseableIterable;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.StringFactory;
import com.tinkerpop.blueprints.util.wrappers.event.EventElement;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

import java.util.List;

/**
 * An @{link Index} that decorates versioned graph elements.
 */
public class VersionedIndex<T extends Element, V extends Comparable<V>>
        implements Index<T> {
    protected final Index<T> rawIndex;
    protected final List<GraphChangedListener> graphChangedListeners;
    private final EventTrigger trigger;
    private final VersionedGraph<?, V> graph;

    public VersionedIndex(final Index<T> rawIndex,
            final List<GraphChangedListener> graphChangedListeners,
            final EventTrigger trigger, final VersionedGraph<?, V> graph) {
        this.rawIndex = rawIndex;
        this.graphChangedListeners = graphChangedListeners;
        this.trigger = trigger;
        this.graph = graph;
    }

    @Override
    public void remove(final String key, final Object value, final T element) {
        this.rawIndex.remove(key, value,
                (T) ((EventElement) element).getBaseElement());
    }

    @Override
    public void put(final String key, final Object value, final T element) {
        this.rawIndex.put(key, value,
                (T) ((EventElement) element).getBaseElement());
    }

    @Override
    public CloseableIterable<T> get(final String key, final Object value) {
        if (Vertex.class.isAssignableFrom(this.getIndexClass())) {
            return new VersionedVertexIterable(this.rawIndex.get(key, value),
                    this.graphChangedListeners, this.trigger, graph,
                    graph.getLatestGraphVersion());
        } else {
            return (CloseableIterable<T>) new VersionedEdgeIterable<V>(
                    (Iterable<Edge>) this.rawIndex.get(key, value),
                    this.graphChangedListeners, trigger, graph,
                    graph.getLatestGraphVersion(), false);
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
