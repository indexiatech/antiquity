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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import co.indexia.antiquity.range.Range;

import org.junit.Test;

public abstract class TransactionalLongVersionedGraphTest extends VersionedGraphTestSuite<Long> {
    public void commit() {
        ((TransactionalGraph) graph).commit();
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicatedVertexIdTest() {
        graph.addVertex("v1");
        graph.addVertex("v1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicatedEdgeIdTest() {
        Vertex v1 = graph.addVertex("v1");
        Vertex v2 = graph.addVertex("v2");

        graph.addEdge("e1", v1, v2, "L1");
        graph.addEdge("e1", v1, v2, "L2");
    }

    @Test
    public void testMultipleChangesInOneTransactionHaveSameVersion() {
        Vertex initial = graph.addVertex("vInit");
        commit();
        Object initialId = initial.getId();
        Long ver1 = graph.getLatestGraphVersion();
        assertThat(ver1, notNullValue());

        Vertex v1 = graph.addVertex("v1");
        Vertex v2 = graph.addVertex("v2");
        Vertex v3 = graph.addVertex("v3");
        graph.removeVertex(initial);
        commit();
        Long ver2 = graph.getLatestGraphVersion();

        assertThat(graph.utils.getVersionRange(graph.getHistoricGraph().getVertex(initialId)),
                is(Range.range(ver1, ver1)));
        assertThat(graph.utils.getVersionRange(graph.getHistoricGraph().getVertex(v1.getId())),
                is(Range.range(ver2, graph.getMaxPossibleGraphVersion())));
        assertThat(graph.utils.getVersionRange(graph.getHistoricGraph().getVertex(v2.getId())),
                is(Range.range(ver2, graph.getMaxPossibleGraphVersion())));
        assertThat(graph.utils.getVersionRange(graph.getHistoricGraph().getVertex(v3.getId())),
                is(Range.range(ver2, graph.getMaxPossibleGraphVersion())));
    }

    @Test
    public void testCreateAndModifyAndDeleteInOneCommit() {
        Vertex v1 = graph.addVertex("v1");
        Vertex v2 = graph.addVertex("v2");
        Vertex v3 = graph.addVertex("v3");
        Vertex v4 = graph.addVertex("v4");
        v1.setProperty("key1", "foo1");
        v1.setProperty("key11", "foo11");
        v1.setProperty("key111", "foo111");
        v2.setProperty("key2", "foo2");
        v2.setProperty("key22", "foo22");
        v2.setProperty("key222", "foo222");
        v3.setProperty("key3", "foo3");
        v3.setProperty("key33", "foo33");
        v3.setProperty("key333", "foo333");
        v4.setProperty("key4", "foo4");
        v4.setProperty("key44", "foo44");
        v4.setProperty("key444", "foo444");
        // TODO: Causes a bug.
        // graph.removeVertex(v4);
        commit();
        Long ver = graph.getLatestGraphVersion();

        // h1
        HistoricVersionedVertex<Long> hv1 = (HistoricVersionedVertex) graph.getHistoricGraph().getVertex(v1.getId());
        assertThat(hv1, notNullValue());
        assertThat(hv1.getVersion(), is(Range.range(ver, ver)));
        assertThat(graph.utils.getVersionRange(hv1), is(Range.range(ver, graph.getMaxPossibleGraphVersion())));

        // h2
        HistoricVersionedVertex<Long> hv2 = (HistoricVersionedVertex) graph.getHistoricGraph().getVertex(v2.getId());
        assertThat(hv2, notNullValue());
        assertThat(hv2.getVersion(), is(Range.range(ver, ver)));
        assertThat(graph.utils.getVersionRange(hv2), is(Range.range(ver, graph.getMaxPossibleGraphVersion())));

        // h3
        HistoricVersionedVertex<Long> hv3 = (HistoricVersionedVertex) graph.getHistoricGraph().getVertex(v3.getId());
        assertThat(hv3, notNullValue());
        assertThat(hv3.getVersion(), is(Range.range(ver, ver)));
        assertThat(graph.utils.getVersionRange(hv3), is(Range.range(ver, graph.getMaxPossibleGraphVersion())));

        // h4

        assertThat(graph.getHistoricGraph().buildVertexChain(v1.getId()).size(), is(1));
        assertThat(graph.getHistoricGraph().buildVertexChain(v2.getId()).size(), is(1));
        assertThat(graph.getHistoricGraph().buildVertexChain(v3.getId()).size(), is(1));
    }

    public void testCreateAndSetOnSameVertexShouldCreateOnlyVertexOnly() {
        /*
         * HistoricVersionedVertex<Long> fooV = (HistoricVersionedVertex<Long>)
         * graph.addVertex("fooV"); // Base as currently transient vertex cannot
         * be queried assertNull(fooV.getBaseElement().getProperty("key1"));
         * assertNull(fooV.getBaseElement().getProperty("key2"));
         * fooV.setProperty("key1", "foo"); assertEquals("foo",
         * fooV.getBaseElement().getProperty("key1")); fooV.setProperty("key2",
         * "bar"); assertEquals("foo",
         * fooV.getBaseElement().getProperty("key1")); assertEquals("bar",
         * fooV.getBaseElement().getProperty("key2")); graph.commit();
         * ArrayList<Vertex> vertexChain = new ArrayList<Vertex>();
         * ActiveVersionedGraph.buildVertexChain(vertexChain, fooV);
         * assertEquals(1, vertexChain.size());
         */
    }

    /**
     * Empty transactions should not be versioned.
     * 
     * This is the expected default graph configuration behavior
     * 
     * @see Configuration#doNotVersionEmptyTransactions
     */
    public void testEmptyTransactionShouldNotBeVersioned() {
        Long ver1 = graph.getLatestGraphVersion();
        commit();
        commit();
        commit();
        assertThat(graph.getLatestGraphVersion(), is(ver1));
    }

    /**
     * Ensure that natural IDs are enabled. This is expected because graph conf
     * {@link Configuration#useNaturalIdsOnlyIfSuppliedIdsAreIgnored} is true
     * and the underline is neo4j and it ignores supplied keys
     */
    @Test
    public void testIsNatureIdsEnabled() {
        assertThat(graph.isNaturalIds(), is(Boolean.TRUE));
    }

    @Test
    public void testNaturalIds() {
        String vId = "TEST-ID";
        Vertex v = graph.addVertex(vId);
        commit();
        assertThat(v.getId(), is((Object) vId));

        Vertex vLoaded = graph.getVertex(vId);
        assertThat(vLoaded, notNullValue());
        assertThat(vLoaded.getId(), is(v.getId()));

        Vertex v1 = graph.addVertex(null);
        assertThat(v1.getId(), notNullValue());
    }
}
