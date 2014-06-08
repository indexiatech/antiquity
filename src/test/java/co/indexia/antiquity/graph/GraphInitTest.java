package co.indexia.antiquity.graph;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test graph creation & initialization
 */
public class GraphInitTest {
    @Test(expected = IllegalStateException.class)
    public void causeExceptionIfGraphNotInitialized() {
        TinkerGraph graph = new TinkerGraph();
        Configuration conf = new Configuration.ConfBuilder().build();
        ActiveVersionedGraph<TinkerGraph, Long> vg = new ActiveVersionedGraph.ActiveVersionedNonTransactionalGraphBuilder<TinkerGraph, Long>(
                graph, new LongGraphIdentifierBehavior()).init(false).conf(conf).build();

        graph.getVertices();
    }

    @Test
    public void shouldNotCreateDuplicatedRootVertices() {
        TinkerGraph graph = new TinkerGraph();
        Configuration conf = new Configuration.ConfBuilder().build();
        new ActiveVersionedGraph.ActiveVersionedNonTransactionalGraphBuilder<TinkerGraph, Long>(graph, new LongGraphIdentifierBehavior()).init(true).conf(conf).build();
        assertThat(Lists.newArrayList(graph.getVertices()).size(), is(2));
        assertThat(Lists.newArrayList(graph.getEdges()).size(), is(0));

        new ActiveVersionedGraph.ActiveVersionedNonTransactionalGraphBuilder<TinkerGraph, Long>(graph, new LongGraphIdentifierBehavior()).init(true).conf(conf).build();
        assertThat(Lists.newArrayList(graph.getVertices()).size(), is(2));
        assertThat(Lists.newArrayList(graph.getEdges()).size(), is(0));
    }
}
