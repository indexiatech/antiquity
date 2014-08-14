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
