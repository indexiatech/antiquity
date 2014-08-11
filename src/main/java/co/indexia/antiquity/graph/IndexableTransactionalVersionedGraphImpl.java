package co.indexia.antiquity.graph;

import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;

/**
 * TODO: This is a starting point of having a flavor of transactional graph that supports manual indexing as this
 * commit removed the native support for IndexableGraph since it's not supported by Titan and others
 *
 * To make it work, it still needs impl of another EventableGraph that implements IndexableGraph
 */
public class IndexableTransactionalVersionedGraphImpl<T extends TransactionalGraph & KeyIndexableGraph & IndexableGraph, V extends Comparable<V>>
        extends TransactionalVersionedGraph<T, V> implements IndexableGraph {

    IndexableTransactionalVersionedGraphImpl(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
        super(baseGraph, identifierBehavior);
    }

    IndexableTransactionalVersionedGraphImpl(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Configuration configuration,
                                             IdGraph.IdFactory vertexIdFactory, IdGraph.IdFactory edgeIdFactory) {
        super(baseGraph, identifierBehavior, configuration, vertexIdFactory, edgeIdFactory);
    }

    // Indices graph overrides
    // --------------------------------------------------------------
    @Override
    public void dropIndex(final String name) {
        this.getBaseGraph().dropIndex(name);
    }

    @Override
    public <T extends Element> Index<T> createIndex(final String indexName, final Class<T> indexClass,
            final Parameter... indexParameters) {
//        return new ActiveVersionedIndex<T, V>(getEventableGraph().createIndex(indexName, indexClass, indexParameters),
//                this);
        throw new IllegalStateException("Currently not supported.");
    }

    @Override
    public <T extends Element> Index<T> getIndex(final String indexName, final Class<T> indexClass) {
//        final Index<T> index = getEventableGraph().getIndex(indexName, indexClass);
//        if (null == index) {
//            return null;
//        } else {
//            return new ActiveVersionedIndex<T, V>(index, this);
//        }
        throw new IllegalStateException("Currently not supported.");
    }

    @Override
    public Iterable<Index<? extends Element>> getIndices() {
        //return new ActiveVersionedIndexIterable(getEventableGraph().getIndices(), this);
        throw new IllegalStateException("Currently not supported.");
    }
}
