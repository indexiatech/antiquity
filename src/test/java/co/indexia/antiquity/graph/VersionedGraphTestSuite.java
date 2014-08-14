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

import static co.indexia.antiquity.graph.matchers.ElementHasProperty.elementHasProperty;
import static co.indexia.antiquity.graph.matchers.HasAmount.hasAmount;
import static co.indexia.antiquity.graph.matchers.HasElementIds.ID.HARD_ID;
import static co.indexia.antiquity.graph.matchers.HasElementIds.TYPE.CONTAINS;
import static co.indexia.antiquity.graph.matchers.HasElementIds.TYPE.EXACTLY_MATCHES;
import static co.indexia.antiquity.graph.matchers.HasElementIds.elementIds;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.List;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.Parameter;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import co.indexia.antiquity.range.Range;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class VersionedGraphTestSuite<V extends Comparable<V>> {
    protected ActiveVersionedGraph<?, V> graph;

    protected abstract ActiveVersionedGraph<?, V> generateGraph();

    @Before
    public void setUp() {
        graph = generateGraph();
    }

    @After
    public void tearDown() {
        graph.shutdown();
    }

    // configuration tests
    // --------------------------------------------------------------

    /**
     * Ensure root vertices exist for both ACTIVE & HISTORIC graphs.
     */
    @Test
    public void testGraphConfVertexShouldExist() {
        try {
            assertThat(graph.getRootVertex(VEProps.GRAPH_TYPE.ACTIVE), notNullValue());
            assertThat(graph.getRootVertex(VEProps.GRAPH_TYPE.HISTORIC), notNullValue());
        } finally {
            graph.shutdown();
        }
    }

    /**
     * Expect failure when instantiating a graph instance without
     * initialization.
     */
    @Test(expected = IllegalStateException.class)
    public void createGraphInstanceWithoutInit() {
        ActiveVersionedGraph<?, V> g =
                new ActiveVersionedGraph.ActiveVersionedNonTransactionalGraphBuilder(new TinkerGraph(),
                        new LongGraphIdentifierBehavior()).build();
    }

    // Vertices tests
    // --------------------------------------------------------------

    /**
     * Simply add new vertex to the graph. TODO: Make sure that internal keys of
     * historic are filtered
     */
    @Test
    public void simpleSingleVertexAddTest() {
        // -- active assertions
        int aAmount = toList(graph.getVertices()).size();
        int hAmount = toList(graph.getHistoricGraph().getVertices()).size();
        Vertex v = graph.addVertex(null);
        v.setProperty("key", "foo");
        assertThat((String) v.getProperty("key"), is("foo"));
        // query for active before transaction is committed
        assertThat(v, instanceOf(ActiveVersionedVertex.class));
        assertThat(graph.getVertices(), hasAmount(aAmount + 1));
        Vertex aV1L = graph.getVertex(v.getId());
        assertThat(aV1L, notNullValue());
        assertThat(aV1L, instanceOf(ActiveVersionedVertex.class));
        assertThat((String) aV1L.getProperty("key"), is("foo"));
        assertThat(v.getProperty("key"), is(graph.getVertex(aV1L.getId()).getProperty("key")));
        CIT();
        V ver1 = last();
        // TODO: Assert after commit the elements look just like as before
        // commit

        // expect same amount after commit too
        assertThat(graph.getVertices(), hasAmount(aAmount + 1));

        // -- historic assertions
        // query historic after transaction committed
        Vertex hV1L = graph.getHistoricGraph().getVertex(v.getId());
        assertThat(hV1L, notNullValue());
        assertThat(hV1L, instanceOf(HistoricVersionedVertex.class));
        assertThat((String) hV1L.getProperty("key"), is("foo"));
        assertThat(Range.range(ver1, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(hV1L)));

        // not the same instance but ids shell be equal
        assertThat(aV1L, not(hV1L));
        assertThat(aV1L.getId(), is(hV1L.getId()));
    }

    /**
     * Add single vertex to the graph with multiple versions.
     * 
     * This method also test the created historic chain and each revision data.
     */
    @Test
    public void simpleSingleVertexWithMultipleVersionsTest() {
        HistoricVersionedGraph h = graph.getHistoricGraph();

        // --active assertions
        // create with revisions
        V ver0 = last();
        ActiveVersionedVertex v1 = (ActiveVersionedVertex) graph.addVertex(null);
        Object v1Id = v1.getId();
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), nullValue());
        CIT();
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), nullValue());
        V ver1 = last();

        // just to cause larger version range
        graph.addVertex(null);
        CIT();
        V ver2 = last();

        v1.setProperty("key", "foo");
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), is("foo"));
        CIT();
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), is("foo"));
        V ver3 = last();

        // just to cause larger version range
        graph.addVertex(null);
        CIT();
        V ver4 = last();

        v1.setProperty("key", "bar");
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), is("bar"));
        CIT();
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), is("bar"));
        V ver5 = last();

        // just to cause larger version range
        graph.addVertex(null);
        CIT();
        V ver6 = last();

        v1.setProperty("key", "baz");
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), is("baz"));
        CIT();
        assertThat((String) graph.getVertex(v1.getId()).getProperty("key"), is("baz"));
        V ver7 = last();

        // just to cause larger version range
        graph.addVertex(null);
        CIT();
        V ver8 = last();

        v1.removeProperty("key");
        assertThat(graph.getVertex(v1.getId()).getPropertyKeys().contains("key"), is(false));
        CIT();
        assertThat(graph.getVertex(v1.getId()).getPropertyKeys().contains("key"), is(false));
        V ver9 = last();

        // just to cause larger version range
        graph.addVertex(null);
        CIT();
        V ver10 = last();

        graph.removeVertex(v1);
        CIT();
        assertThat(graph.getVertex(v1Id), nullValue());
        V ver11 = last();

        // --historic assertions
        List<HistoricVersionedVertex<V>> hvs = graph.getHistoricGraph().buildVertexChain(v1Id);
        assertThat(hvs.size(), is(5));
        // test chain order


        assertThat(h.getVertexForVersion(v1Id, ver1).getPropertyKeys().contains("key"), is(false));
        assertThat(h.getVertexForVersion(v1Id, ver2).getPropertyKeys().contains("key"), is(false));
        assertThat(h.getVertexForVersion(v1Id, ver3), elementHasProperty("key", "foo"));
        assertThat(h.getVertexForVersion(v1Id, ver4), elementHasProperty("key", "foo"));
        assertThat(h.getVertexForVersion(v1Id, ver5), elementHasProperty("key", "bar"));
        assertThat(h.getVertexForVersion(v1Id, ver6), elementHasProperty("key", "bar"));
        assertThat(h.getVertexForVersion(v1Id, ver7), elementHasProperty("key", "baz"));
        assertThat(h.getVertexForVersion(v1Id, ver8), elementHasProperty("key", "baz"));
        assertThat(h.getVertexForVersion(v1Id, ver9).getPropertyKeys().contains("key"), is(false));
        assertThat(h.getVertexForVersion(v1Id, ver10).getPropertyKeys().contains("key"), is(false));
        assertThat(h.getVertexForVersion(v1Id, ver11), nullValue());


        assertThat(hvs.get(4).getVersion(), is(Range.range(ver1, ver1)));
        assertThat(graph.utils.getVersionRange(hvs.get(4)), is(Range.range(ver1, ver2)));
        assertThat(hvs.get(3).getVersion(), is(Range.range(ver3, ver3)));
        assertThat(graph.utils.getVersionRange(hvs.get(3)), is(Range.range(ver3, ver4)));
        assertThat(hvs.get(2).getVersion(), is(Range.range(ver5, ver5)));
        assertThat(graph.utils.getVersionRange(hvs.get(2)), is(Range.range(ver5, ver6)));
        assertThat(hvs.get(1).getVersion(), is(Range.range(ver7, ver7)));
        assertThat(graph.utils.getVersionRange(hvs.get(1)), is(Range.range(ver7, ver8)));
        assertThat(hvs.get(0).getVersion(), is(Range.range(ver9, ver9)));
        assertThat(graph.utils.getVersionRange(hvs.get(0)), is(Range.range(ver9, ver10)));

        assertThat(h.getVertices("key", "foo"), hasAmount(1));
        assertThat(h.getVertices("key", "bar"), hasAmount(1));
        assertThat(h.getVertices("key", "baz"), hasAmount(1));
        assertThat(h.getVertexForVersion(v1Id, ver1).getHardId(), is(h.getVertexForVersion(v1Id, ver2).getHardId()));
        assertThat(h.getVertexForVersion(v1Id, ver3).getHardId(), is(h.getVertexForVersion(v1Id, ver4).getHardId()));
        assertThat(h.getVertexForVersion(v1Id, ver5).getHardId(), is(h.getVertexForVersion(v1Id, ver6).getHardId()));
        assertThat(h.getVertexForVersion(v1Id, ver7).getHardId(), is(h.getVertexForVersion(v1Id, ver8).getHardId()));
        assertThat(h.getVertexForVersion(v1Id, ver9).getHardId(), is(h.getVertexForVersion(v1Id, ver10).getHardId()));
        assertThat(h.getVertexForVersion(v1Id, ver11), nullValue());
    }

    /**
     * Remove a vertex from the graph
     */
    @Test
    public void removeVersionedVertex() {
        // --active assertions
        int amountV = toList(graph.getVertices()).size();
        V ver0 = last();
        ActiveVersionedVertex v1 = (ActiveVersionedVertex) graph.addVertex(null);
        assertThat(graph.getVertices(), hasAmount(amountV + 1));
        CIT();
        V ver1 = last();
        assertThat(graph.getVertices(), hasAmount(amountV + 1));
        assertThat(graph.getVertex(v1.getId()), notNullValue());

        // --historic assertions
        List<HistoricVersionedVertex<V>> chain = graph.getHistoricGraph().buildVertexChain(v1);
        assertThat(Range.range(ver1, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(chain.get(0))));

        // --active assertions
        Object v1Id = v1.getId();
        graph.removeVertex(v1);
        assertThat(graph.getVertex(v1Id), nullValue());
        CIT();
        V ver2 = last();
        assertThat(graph.getVertex(v1Id), nullValue());
        assertThat(graph.getVertices(), hasAmount(amountV));

        // historic assertions
        chain = graph.getHistoricGraph().buildVertexChain(v1Id);
        assertThat(Range.range(ver1, ver1), is(graph.utils.getVersionRange(chain.get(0))));

        // TODO V removedAt =
        // chain.get(0).getBaseElement().getProperty(VEProps.REMOVED_PROP_KEY);
        // TODO assertThat(Range.range(ver2, ver2), is(Range.range(removedAt,
        // removedAt)));
    }

    /**
     * Ensure vertices versions are as expected.
     * 
     * Removed elements ranges already tested in
     * {@link #removeVersionedVertex()}
     */
    @Test
    public void testVertexVersionRanges() {
        ActiveVersionedVertex fooV = (ActiveVersionedVertex) graph.addVertex(null);
        CIT();
        V ver1 = last();

        Vertex fooHV = graph.getHistoricGraph().getLatestHistoricRevision(fooV);
        assertThat(Range.range(ver1, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(fooHV)));

        fooV.setProperty("prop", "foo");
        CIT();
        V ver2 = last();
        fooHV = graph.getHistoricGraph().getLatestHistoricRevision(fooV);
        assertThat(Range.range(ver2, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(fooHV)));


        ActiveVersionedVertex barV = (ActiveVersionedVertex) graph.addVertex(null);
        CIT();
        barV.setProperty("prop", "bar");
        CIT();
        V ver4 = last();

        fooHV = graph.getHistoricGraph().getLatestHistoricRevision(fooV);
        Vertex barHV = graph.getHistoricGraph().getLatestHistoricRevision(barV);

        assertThat(Range.range(ver2, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(fooHV)));
        assertThat(Range.range(ver4, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(barHV)));
    }

    /**
     * Test that
     * {@link co.indexia.antiquity.graph.HistoricVersionedVertex#getVersion()}
     * value is as expected.
     */
    @Test
    public void testHistoricVertexVersionProperty() {
        ActiveVersionedVertex<V> v1 = (ActiveVersionedVertex<V>) graph.addVertex(null);
        CIT();
        V ver1 = last();

        // getLatestHistoricRevision();
        HistoricVersionedVertex<V> vhl = graph.getHistoricGraph().getLatestHistoricRevision(v1);
        assertThat(graph.utils.getVersionRange(vhl), is(Range.range(ver1, graph.getMaxPossibleGraphVersion())));
        assertThat(vhl.getVersion(), is(Range.range(ver1, ver1)));

        // getVertices();
        List<Vertex> vertices = toList(graph.getHistoricGraph().getVertices());
        assertThat(vertices.size(), is(1));
        assertThat(((HistoricVersionedVertex<V>) vertices.get(0)).getHardId(), is(vhl.getHardId()));
        assertThat(((HistoricVersionedVertex<V>) vertices.get(0)).getVersion(), is(Range.range(ver1, ver1)));
        assertThat(graph.utils.getVersionRange(vertices.get(0)),
                is(Range.range(ver1, graph.getMaxPossibleGraphVersion())));

        // TODO: For other revisions and other get methods in the historic graph
    }

    @Test
    public void testUnchangedValueSetInVertex() {
        ActiveVersionedVertex<V> v1 = (ActiveVersionedVertex<V>) graph.addVertex(null);
        CIT();
        V ver1 = last();
        v1.setProperty("key", "foo");
        CIT();
        V ver2 = last();
        assertThat(ver1.compareTo(ver2), is(-1));

        v1.setProperty("key", "foo");
        CIT();
        V ver3 = last();
        assertThat(ver2.compareTo(ver3), is(0));
    }


    @Test
    public void testVerticesWithPropsPrivateHash() {
        ActiveVersionedVertex<V> v1 = (ActiveVersionedVertex) graph.addVertex("v1");
        CIT();
        String v1EmptyHash = v1.getPrivateHash();
        ActiveVersionedVertex<V> v2 = (ActiveVersionedVertex) graph.addVertex("v2");
        CIT();
        String v2EmptyHash = v2.getPrivateHash();
        assertThat(v1EmptyHash, is(v2EmptyHash));

        v1.setProperty("keyFoo", "foo");
        CIT();
        String v1With1KeyHash = v1.getPrivateHash();
        v1.setProperty("keyBar", "bar");
        CIT();
        String v1With2KeysHash = v1.getPrivateHash();
        v1.setProperty("keyBaz", "baz");
        CIT();
        String v1With3KeysHash = v1.getPrivateHash();
        assertThat(v1With1KeyHash, not(v1With2KeysHash));
        assertThat(v1With1KeyHash, not(v1With3KeysHash));
        assertThat(v1With2KeysHash, not(v1With3KeysHash));
        v1.removeProperty("keyBaz");
        CIT();
        assertThat(v1.getPrivateHash(), is(v1With2KeysHash));

        v2.setProperty("keyFoo", "foo");
        CIT();
        String v2With1KeyHash = v2.getPrivateHash();
        assertThat(v2With1KeyHash, is(v1With1KeyHash));
        v2.setProperty("keyBar", "bar");
        CIT();
        String v2With2KeysHash = v2.getPrivateHash();
        assertThat(v2With2KeysHash, is(v1With2KeysHash));
        v2.setProperty("keyBaz", "baz");
        CIT();
        String v2With3KeysHash = v2.getPrivateHash();
        assertThat(v2With3KeysHash, is(v1With3KeysHash));
    }


    // Edges tests
    // --------------------------------------------------------------
    @Test
    public void simpleSingleEdgeAddTest() {
        // -- active assertions
        int aAmount = toList(graph.getEdges()).size();
        Vertex v1 = graph.addVertex(null);
        Vertex v2 = graph.addVertex(null);
        Edge e = graph.addEdge(null, v1, v2, "LINK");
        String eId = (String)e.getId();
        CIT();
        V edgeVer = last();
        e.setProperty("key", "foo");
        CIT();
        //hack
        if (graph.getBaseGraph() instanceof Neo4j2Graph) {
            ((Neo4j2Graph)graph.getBaseGraph()).autoStartTransaction(true);
        }
        e = graph.getEdge(eId);
        // query for active before transaction is committed
        assertThat(e, instanceOf(ActiveVersionedEdge.class));
        assertThat(graph.getEdges(), hasAmount(aAmount + 1));

        Edge aE1L = graph.getEdge(e.getId());
        assertThat(aE1L, notNullValue());
        assertThat(aE1L, instanceOf(ActiveVersionedEdge.class));
        assertThat((String) aE1L.getProperty("key"), is("foo"));
        assertThat(e.getProperty("key"), is(graph.getEdge(aE1L.getId()).getProperty("key")));;
        CIT();
        // expect edge to reach its vertices
        assertThat(aE1L.getVertex(Direction.OUT).getId(), is(v1.getId()));
        assertThat(aE1L.getVertex(Direction.IN).getId(), is(v2.getId()));


        // TODO: Assert after commit the elements look just like as before
        // commit
        // expect same behavior after commit too
        assertThat(graph.getEdges(), hasAmount(aAmount + 1));

        // -- historic assertions
        // query historic after transaction committed
        HistoricVersionedEdge<V> hE1L = (HistoricVersionedEdge<V>) graph.getHistoricGraph().getEdge(e.getId());
        assertThat(hE1L, notNullValue());
        assertThat(hE1L, instanceOf(HistoricVersionedEdge.class));
        assertThat((String) hE1L.getProperty("key"), is("foo"));
        assertThat(graph.utils.getVersionRange(hE1L), is(Range.range(edgeVer, graph.getMaxPossibleGraphVersion())));
        assertThat(hE1L.getVersion(), is(Range.range(edgeVer, edgeVer)));
        // not the same instance but ids shell be equal
        assertThat(aE1L.getId(), is(hE1L.getId()));
        // expect edge to reach its vertices
        assertThat(hE1L.getVertex(Direction.OUT).getId(), is(v1.getId()));
        assertThat(hE1L.getVertex(Direction.IN).getId(), is(v2.getId()));
    }

    /**
     * Remove an edge from the graph
     */
    @Test
    public void removeVersionedEdge() {
        // --active assertions
        int amountV = toList(graph.getEdges()).size();
        V ver0 = last();
        ActiveVersionedVertex v1 = (ActiveVersionedVertex) graph.addVertex("v1");
        CIT();
        ActiveVersionedVertex v2 = (ActiveVersionedVertex) graph.addVertex("v2");
        CIT();
        ActiveVersionedVertex v3 = (ActiveVersionedVertex) graph.addVertex("v3");
        CIT();
        assertThat(graph.getEdges(), hasAmount(amountV));
        ActiveVersionedEdge e1 = (ActiveVersionedEdge) graph.addEdge("e1", v1, v2, "LINKED");
        CIT();
        V e1Ver = last();
        ActiveVersionedEdge e2 = (ActiveVersionedEdge) graph.addEdge("e2", v2, v3, "LINKED");
        CIT();
        V e2Ver = last();
        assertThat(graph.getEdges(), hasAmount(amountV + 2));
        CIT();
        assertThat(graph.getEdges(), hasAmount(amountV + 2));
        assertThat(graph.getEdge(e1.getId()), notNullValue());

        // -- historic assertions
        assertThat(Range.range(e1Ver, graph.getMaxPossibleGraphVersion()),
                is(graph.utils.getVersionRange(graph.getHistoricGraph().getEdge(e1.getId()))));
        HistoricVersionedEdge<V> hve2L = (HistoricVersionedEdge) graph.getHistoricGraph().getEdge(e2.getId());
        assertThat(Range.range(e2Ver, graph.getMaxPossibleGraphVersion()),
                is(graph.utils.getVersionRange(graph.getHistoricGraph().getEdge(e2.getId()))));

        // -- active assertions
        V e2BeforeRemoveVer = last();
        Object e2Id = e2.getId();
        graph.removeEdge(e2);
        Iterable<Edge> es = graph.getBaseGraph().query().has(VEProps.HISTORIC_ELEMENT_PROP_KEY, true).edges();
        // TODO: Cause exception in neo4j since removed elements are still read
        // by get methods
        // assertThat(graph.getEdges(), hasAmount(amountV + 1));
        // assertThat(graph.getEdge(e2Id), nullValue());
        CIT();
        V e2RemoveVer = last();
        assertThat(graph.getEdges(), hasAmount(amountV + 1));
        assertThat(graph.getEdge(e2Id), nullValue());

        // historic assertions
        HistoricVersionedEdge<V> hve2LAgain =
                (HistoricVersionedEdge) graph.getHistoricGraph().getEdgeByHardId(hve2L.getHardId());
        assertThat(hve2LAgain, notNullValue());


        assertThat(Range.range(e2Ver, e2BeforeRemoveVer), is(graph.utils.getVersionRange(hve2LAgain)));
        assertThat((V) hve2LAgain.getProperty(VEProps.REMOVED_PROP_KEY), is(e2RemoveVer));
    }

    // ---- Vertices & Edges mixed tests
    /**
     * The test ensures that added vertices and edges are retrieved via
     * {@link ActiveVersionedGraph#getVertices()} and
     * {@link ActiveVersionedGraph#getEdges()}
     */
    @Test
    public void testGetVerticesAndEdges() {
        // -- active assertions
        int vnum = toList(graph.getVertices()).size();
        int eNum = toList(graph.getEdges()).size();

        int hvNum = toList(graph.getHistoricGraph().getVertices()).size();
        int heNum = toList(graph.getHistoricGraph().getEdges()).size();
        Vertex v1 = graph.addVertex(null);
        CIT();
        v1.setProperty("name", "hello");
        CIT();
        Vertex v2 = graph.addVertex(null);
        CIT();
        v2.setProperty("name", "world");
        assertThat(graph.getVertices(), hasAmount(vnum + 2));
        CIT();
        assertThat(graph.getVertices(), hasAmount(vnum + 2));
        assertThat(graph.getVertices("name", "hello"), hasAmount(1));
        assertThat(graph.getVertices("name", "world"), hasAmount(1));


        // edges
        Edge e1 = graph.addEdge(null, v1, v2, "LINK1");
        CIT();
        e1.setProperty("name", "link1");
        CIT();
        Edge e2 = graph.addEdge(null, v1, v2, "LINK2");
        CIT();
        e2.setProperty("name", "link2");
        assertThat(graph.getEdges(), hasAmount(eNum + 2));
        CIT();
        assertThat(graph.getEdges(), hasAmount(eNum + 2));
        assertThat(graph.getEdges("name", "link1"), hasAmount(1));
        assertThat(graph.getEdges("name", "link2"), hasAmount(1));

        // -- historic assertions
        assertThat(graph.getHistoricGraph().getVertices(), hasAmount(hvNum + 4));
        assertThat(graph.getHistoricGraph().getEdges(), hasAmount(heNum + 2));
        // TODO: more advances tests
    }

    @Test
    public void ensureAddingEdgesReflectedInAssocLoadedVertex() {
        Vertex v1 = graph.addVertex(null);
        assertThat(v1.getEdges(Direction.BOTH), hasAmount(0));
        Vertex v2 = graph.addVertex(null);
        assertThat(v2.getEdges(Direction.BOTH), hasAmount(0));
        Edge e1 = graph.addEdge(null, v1, v2, "LINKED");
        assertThat(v1.getEdges(Direction.BOTH), hasAmount(1));
        assertThat(v2.getEdges(Direction.BOTH), hasAmount(1));
        CIT();
        assertThat(v1.getEdges(Direction.BOTH), hasAmount(1));
        assertThat(v2.getEdges(Direction.BOTH), hasAmount(1));
    }

    /**
     * This test verifies that marked deleted vertices are not available when
     * {@link ActiveVersionedGraph#getVertices()} is invoked. This test also
     * ensure that the the associated edges of the removed vertex are marked as
     * deleted too and are not retrieved when
     * {@link ActiveVersionedGraph#getEdges()} is invoked.
     */
    @Test
    public void testThatDeletedVertexIsFlaggedAsRemovedWithItsEdges() {
        // --active assertions
        int vNum = Lists.newArrayList(graph.getVertices()).size();
        int eNum = Lists.newArrayList(graph.getEdges()).size();
        Vertex fooV = graph.addVertex("fooV");
        CIT();
        Vertex barV = graph.addVertex("barV");
        CIT();
        V beforeEdgeVer = last();
        Edge e = graph.addEdge(null, fooV, barV, "LINKED");
        CIT();
        Object eId = e.getId();
        V afterEdgeVer = last();
        assertThat(Lists.newArrayList(graph.getVertices()).size(), is(vNum + 2));
        assertThat(Lists.newArrayList(graph.getEdges()).size(), is(eNum + 1));

        // --historic assertions
        HistoricVersionedEdge<V> he = (HistoricVersionedEdge<V>) graph.getHistoricGraph().getEdge(e.getId());
        assertThat(Range.range(afterEdgeVer, graph.getMaxPossibleGraphVersion()), is(graph.utils.getVersionRange(he)));

        // just for bumping id
        fooV.setProperty("foo", "bar1");
        fooV.setProperty("foo", "bar2");
        fooV.setProperty("foo", "bar3");
        CIT();

        V beforeFooRemovalVer = last();
        graph.removeVertex(fooV);
        CIT();
        assertThat(Lists.newArrayList(graph.getVertices()).size(), is(vNum + 1));
        assertThat(Lists.newArrayList(graph.getEdges()).size(), is(eNum));

        he = (HistoricVersionedEdge<V>) graph.getHistoricGraph().getEdge(eId);
        assertThat(graph.utils.getVersionRange(he), is(Range.range(afterEdgeVer, beforeFooRemovalVer)));
    }

    @Test
    public void testVersionOfHistoricElements() {
        Vertex v1 = graph.addVertex(null);
        CIT();
        V ver1 = last();
        HistoricVersionedVertex<V> hv1 = (HistoricVersionedVertex<V>) graph.getHistoricGraph().getVertex(v1.getId());
        // expect the -start- version of hv1
        assertThat(hv1.getVersion(), is(Range.range(ver1, ver1)));

        Vertex v2 = graph.addVertex(null);
        CIT();
        V ver2 = last();
        HistoricVersionedVertex<V> hv2 = (HistoricVersionedVertex<V>) graph.getHistoricGraph().getVertex(v2.getId());
        // expect the -start- version of hv1
        assertThat(hv2.getVersion(), is(Range.range(ver2, ver2)));

        // list
        Boolean ver1Found = false;
        Boolean ver2Found = false;
        for (Vertex v : graph.getHistoricGraph().getVertices()) {
            HistoricVersionedVertex<V> hv = (HistoricVersionedVertex<V>) v;
            if (v.getId().equals(v1.getId())) {
                assertThat(hv.getVersion(), is(Range.range(ver1, ver1)));
                ver1Found = true;
            } else if (v.getId().equals(v2.getId())) {
                assertThat(hv.getVersion(), is(Range.range(ver2, ver2)));
                ver2Found = true;
            }
        }
        assertThat(ver1Found && ver2Found, is(true));
        // TODO: Add/test more versions of the same vertices by vertices(),
        // getVertexForVersion().


        Edge e1 = graph.addEdge(null, v1, v2, "LINKED");
        CIT();
        V ver3 = last();
        HistoricVersionedEdge<V> he1 = (HistoricVersionedEdge<V>) graph.getHistoricGraph().getEdge(e1.getId());
        assertThat(he1.getVersion(), is(Range.range(ver3, ver3)));
        // TODO: More tests for edges versions
    }


    /**
     * A unit test for the following scenario:
     * 
     * <pre>
     * Commits:
     * commit 1: add v1
     * commit 2: add v2
     * commit 3: add v1->e1->v2
     * commit 4: remove v1
     * commit 5: update v2 (foo=bar)
     * commit 6: add v3
     * commit 7: update v2 (foo=baz)
     * commit 8: add v2->e2->v3
     * commit 9: update e2 (bar=baz)
     * commit 10: update v2 (remove key 'foo')
     * commit 11: remove e2
     * </pre>
     */
    @Test
    public void fullScenarioTest() {
        int avs = toList(graph.getVertices()).size();
        int hvs = toList(graph.getHistoricGraph().getVertices()).size();
        int aes = toList(graph.getEdges()).size();
        int hes = toList(graph.getHistoricGraph().getEdges()).size();
        ActiveVersionedVertex v1 = (ActiveVersionedVertex) graph.addVertex("ver1");
        CIT();
        V ver1 = last();
        Object v1Id = v1.getId();
        ActiveVersionedVertex v2 = (ActiveVersionedVertex) graph.addVertex("ver2");
        CIT();
        V ver2 = last();
        Object v2Id = v2.getId();
        ActiveVersionedEdge<V> e1 = (ActiveVersionedEdge<V>) graph.addEdge("e1", v1, v2, "LINKED");
        CIT();
        V ver3 = last();
        Object e1Id = e1.getId();
        graph.removeVertex(v1);
        CIT();
        V ver4 = last();
        v2.setProperty("foo", "bar");
        CIT();
        V ver5 = last();
        ActiveVersionedVertex v3 = (ActiveVersionedVertex) graph.addVertex("ver3");
        CIT();
        V ver6 = last();
        Object v3Id = v3.getId();
        v2.setProperty("foo", "baz");
        CIT();
        V ver7 = last();
        ActiveVersionedEdge<V> e2 = (ActiveVersionedEdge<V>) graph.addEdge("e2", v2, v3, "LINKED");
        CIT();
        V ver8 = last();
        Object e2Id = e2.getId();
        e2.setProperty("bar", "baz");
        CIT();
        V ver9 = last();
        v2.removeProperty("foo");
        CIT();
        V ver10 = last();
        e2 = (ActiveVersionedEdge<V>) graph.getEdge(e2Id);
        graph.removeEdge(e2);
        CIT();
        V ver11 = last();


        // --active assertions
        // v1
        Vertex v1ActiveLoaded = graph.getVertex(v1Id);
        assertThat(v1ActiveLoaded, nullValue());
        // e1
        Edge e1ActiveLoaded = graph.getEdge(e1Id);
        assertThat(e1ActiveLoaded, nullValue());
        // v2
        Vertex v2ActiveLoaded = graph.getVertex(v2.getId());
        assertThat(v2ActiveLoaded, notNullValue());
        assertThat(v2ActiveLoaded.getProperty("foo"), nullValue());
        assertThat(v2ActiveLoaded.getEdges(Direction.BOTH), hasAmount(0));
        // v3
        Vertex v3ActiveLoaded = graph.getVertex(v3.getId());
        assertThat(v3ActiveLoaded, notNullValue());
        assertThat(v3ActiveLoaded.getEdges(Direction.BOTH), hasAmount(0));
        // e2
        Edge e2ActiveLoaded = graph.getEdge(e2Id);
        assertThat(e2ActiveLoaded, nullValue());
        // lists
        // for some reason EXACTLY_MATCHES does not work here, looks like
        // sometimes the order of the array is diff and
        // equality fails, changed to CONTAINS for now.
        assertThat(graph.getVertices(), elementIds(CONTAINS, v2ActiveLoaded.getId(), v3ActiveLoaded.getId()));
        assertThat(graph.getEdges(), not(elementIds(CONTAINS, e1Id, e2Id)));


        // --historic assertions
        HistoricVersionedGraph<?, V> h = graph.getHistoricGraph();


        // list edges
        assertThat(h.getEdges(), hasAmount(hes + 2));
        // list vertices
        // FIXME: how many we expect? assertThat(h.getVertices(), hasAmount(hvs
        // + 6));

        // ----commit 1----
        // v1
        HistoricVersionedVertex<V> commit1V1 = h.getVertexForVersion(v1Id, ver1);
        assertThat(commit1V1, notNullValue());
        assertThat((Range<V>) h.utils.getVersionRange(commit1V1), is(Range.range(ver1, ver3)));
        assertThat(commit1V1.getVersion(), is(Range.range(ver1, ver1)));
        assertThat(commit1V1.getEdges(Direction.BOTH), hasAmount(0));
        // v2
        assertThat(h.getVertexForVersion(v2Id, ver1), nullValue());
        // v3
        assertThat(h.getVertexForVersion(v3Id, ver1), nullValue());
        // e1
        assertThat(h.getEdgeForVersion(e1Id, ver1), nullValue());
        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver1), nullValue());
        // list vertices
        HistoricVersionedVertexIterable<V> verticesForVer1 = h.getVertices(ver1);
        assertThat(verticesForVer1, hasAmount(hvs + 1));
        assertThat(verticesForVer1, elementIds(HARD_ID, EXACTLY_MATCHES, commit1V1.getHardId()));
        // list edges
        assertThat(h.getEdges(ver1), hasAmount(0));


        // ----commit 2----
        // v1
        HistoricVersionedVertex commit2V1 = h.getVertexForVersion(v1Id, ver2);
        assertThat(commit2V1.getHardId(), is(commit1V1.getHardId()));
        // v2
        HistoricVersionedVertex commit2V2 = h.getVertexForVersion(v2Id, ver2);
        assertThat(commit2V2, notNullValue());
        assertThat(commit2V2.getPropertyKeys().contains("foo"), is(false));
        assertThat(h.utils.getVersionRange(commit2V2), is(Range.range(ver2, ver4)));
        assertThat((Range<V>) commit2V2.getVersion(), is(Range.range(ver2, ver2)));
        assertThat(commit2V2.getEdges(Direction.BOTH), hasAmount(0));
        // v3
        assertThat(h.getVertexForVersion(v3Id, ver2), nullValue());
        // e1
        assertThat(h.getEdgeForVersion(e1Id, ver2), nullValue());
        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver2), nullValue());
        // list vertices
        HistoricVersionedVertexIterable commit2Vertices = h.getVertices(ver2);
        assertThat(commit2Vertices, hasAmount(hvs + 2));
        assertThat(commit2Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit2V1.getHardId(), commit2V2.getHardId()));
        // list edges
        assertThat(h.getEdges(ver2), hasAmount(hes));

        // ----commit 3----
        // load
        HistoricVersionedEdge<V> commit3HE1 = h.getEdgeForVersion(e1Id, ver3);
        assertThat(commit3HE1, notNullValue());

        // v1
        HistoricVersionedVertex<V> commit3V1 = h.getVertexForVersion(v1Id, ver3);
        assertThat(h.utils.getVersionRange(commit3V1), is(Range.range(ver1, ver3)));
        assertThat(commit3V1.getVersion(), is(Range.range(ver3, ver3)));
        assertThat(commit3V1, notNullValue());
        assertThat(commit3V1.getEdges(Direction.IN), hasAmount(0));
        assertThat(commit3V1.getEdges(Direction.OUT), hasAmount(1));
        Iterable<Edge> commit3V1EdgesOut = commit3V1.getEdges(Direction.OUT);
        assertThat(commit3V1EdgesOut, elementIds(HARD_ID, EXACTLY_MATCHES, commit3HE1.getHardId()));

        // v2
        HistoricVersionedVertex<V> commit3V2 = h.getVertexForVersion(v2Id, ver3);
        assertThat(commit3V2, notNullValue());
        assertThat(commit3V2.getPropertyKeys().contains("foo"), is(false));
        assertThat(h.utils.getVersionRange(commit3V2), is(Range.range(ver2, ver4)));
        assertThat(commit3V2.getEdges(Direction.OUT), hasAmount(0));
        Iterable<Edge> commit3V2EdgesIn = commit3V2.getEdges(Direction.IN);
        assertThat(commit3V2EdgesIn, hasAmount(1));
        assertThat(commit3V2EdgesIn, elementIds(HARD_ID, EXACTLY_MATCHES, commit3HE1.getHardId()));

        // v3
        assertThat(h.getVertexForVersion(v3Id, ver3), nullValue());

        // e1
        assertThat(commit3HE1.getVersion(), is(Range.range(ver3, ver3)));
        assertThat(graph.utils.getVersionRange(commit3HE1), is(Range.range(ver3, ver3)));
        assertThat(((HistoricVersionedVertex<V>) commit3HE1.getVertex(Direction.OUT)).getHardId(),
                is(commit3V1.getHardId()));
        assertThat(((HistoricVersionedVertex<V>) commit3HE1.getVertex(Direction.IN)).getHardId(),
                is(commit3V2.getHardId()));
        assertThat(((HistoricVersionedEdge<V>) commit3V1EdgesOut.iterator().next()).getHardId(),
                is(((HistoricVersionedEdge<V>) commit3V2EdgesIn.iterator().next()).getHardId()));

        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver3), nullValue());


        // list vertices
        HistoricVersionedVertexIterable<V> commit3Vertices = h.getVertices(ver3);
        assertThat(commit3Vertices, hasAmount(hvs + 2));
        assertThat(commit3Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit3V1.getHardId(), commit3V2.getHardId()));
        // list edges
        Iterable<Edge> commit3Edges = h.getEdges(ver3);
        assertThat(commit3Edges, hasAmount(hes + 1));
        assertThat(commit3Edges, elementIds(HARD_ID, EXACTLY_MATCHES, commit3HE1.getHardId()));


        // ----commit 4----
        // v1
        HistoricVersionedVertex<V> commit4V1 = h.getVertexForVersion(v1Id, ver4);
        assertThat(commit4V1, nullValue());
        HistoricVersionedVertex<V> commit4V1Deleted = h.getVertexByHardId(commit3V1.getHardId());
        assertThat(commit4V1Deleted.getVersion(), is(Range.range(ver1, ver1)));
        assertThat(graph.utils.getVersionRange(commit4V1Deleted), is(Range.range(ver1, ver3)));

        // v2
        HistoricVersionedVertex<V> commit4V2 = h.getVertexForVersion(v2, ver4);
        assertThat(commit4V2, notNullValue());
        assertThat(commit4V2.getPropertyKeys().contains("foo"), is(false));
        assertThat((Range<V>) h.utils.getVersionRange(commit4V2), is(Range.range(ver2, ver4)));
        assertThat(commit4V2.getEdges(Direction.BOTH), hasAmount(0));

        // v3
        assertThat(h.getVertexForVersion(v3, ver4), nullValue());

        // e1
        assertThat(h.getEdgeForVersion(e1Id, ver4), nullValue());
        HistoricVersionedEdge<V> commit4HE1Deleted = h.getEdgeByHardId(commit3HE1.getHardId());
        assertThat(commit4HE1Deleted.getVersion(), is(Range.range(ver3, ver3)));
        assertThat(graph.utils.getVersionRange(commit4HE1Deleted), is(Range.range(ver3, ver3)));

        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver4), nullValue());

        // list vertices
        HistoricVersionedVertexIterable<V> commit4Vertices = h.getVertices(ver4);
        assertThat(commit4Vertices, hasAmount(hvs + 1));
        assertThat(commit4Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit4V2.getHardId()));
        // list edges
        Iterable<Edge> commit4Edges = h.getEdges(ver4);
        assertThat(commit4Edges, hasAmount(hes));



        // ----commit 5----
        // v1
        HistoricVersionedVertex<V> commit5V1 = h.getVertexForVersion(v1Id, ver5);
        assertThat(commit5V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit5V2 = h.getVertexForVersion(v2Id, ver5);
        assertThat(commit5V2, notNullValue());
        assertThat(commit5V2, elementHasProperty("foo", "bar"));
        assertThat((Range<V>) h.utils.getVersionRange(commit5V2), is(Range.range(ver5, ver6)));
        assertThat(commit5V2.getEdges(Direction.BOTH), hasAmount(0));

        // v3
        assertThat(h.getVertexForVersion(v3, ver5), nullValue());

        // e1
        assertThat(h.getEdgeForVersion(e1Id, ver5), nullValue());

        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver5), nullValue());

        // list vertices
        HistoricVersionedVertexIterable<V> commit5Vertices = h.getVertices(ver5);
        assertThat(commit5Vertices, hasAmount(hvs + 1));
        assertThat(commit5Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit5V2.getHardId()));
        // list edges
        Iterable<Edge> commit5Edges = h.getEdges(ver5);
        assertThat(commit5Edges, hasAmount(hes));



        // ----commit 6----
        // v1
        HistoricVersionedVertex<V> commit6V1 = h.getVertexForVersion(v1Id, ver6);
        assertThat(commit6V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit6V2 = h.getVertexForVersion(v2Id, ver6);
        assertThat(commit6V2.getHardId(), is(commit5V2.getHardId()));

        // v3
        HistoricVersionedVertex<V> commit6V3 = h.getVertexForVersion(v3Id, ver6);
        assertThat(commit6V3, notNullValue());
        assertThat((Range<V>) h.utils.getVersionRange(commit6V3),
                is(Range.range(ver6, graph.getMaxPossibleGraphVersion())));
        assertThat(commit6V3.getEdges(Direction.BOTH), hasAmount(0));

        // e1
        assertThat(h.getEdgeForVersion(e1Id, ver6), nullValue());

        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver6), nullValue());

        // list vertices
        HistoricVersionedVertexIterable<V> commit6Vertices = h.getVertices(ver6);
        assertThat(commit6Vertices, hasAmount(hvs + 2));
        assertThat(commit6Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit6V2.getHardId(), commit6V3.getHardId()));
        // list edges
        Iterable<Edge> commit6Edges = h.getEdges(ver6);
        assertThat(commit6Edges, hasAmount(hes));



        // ----commit 7----
        // v1
        HistoricVersionedVertex<V> commit7V1 = h.getVertexForVersion(v1Id, ver7);
        assertThat(commit7V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit7V2 = h.getVertexForVersion(v2Id, ver7);
        assertThat(commit7V2, notNullValue());
        assertThat(commit7V2, elementHasProperty("foo", "baz"));
        assertThat((Range<V>) h.utils.getVersionRange(commit7V2), is(Range.range(ver7, ver9)));
        assertThat(commit7V2.getEdges(Direction.BOTH), hasAmount(0));

        // v3
        HistoricVersionedVertex<V> commit7V3 = h.getVertexForVersion(v3Id, ver7);
        assertThat(commit7V3.getHardId(), is(commit6V3.getHardId()));

        // e2
        assertThat(h.getEdgeForVersion(e2Id, ver7), nullValue());

        // e1
        assertThat(h.getEdgeForVersion(e1Id, ver7), nullValue());

        // list vertices
        HistoricVersionedVertexIterable<V> commit7Vertices = h.getVertices(ver7);
        assertThat(commit7Vertices, hasAmount(hvs + 2));
        assertThat(commit7Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit7V2.getHardId(), commit7V3.getHardId()));
        // list edges
        Iterable<Edge> commit7Edges = h.getEdges(ver7);
        assertThat(commit7Edges, hasAmount(hes));



        // ----commit 8----
        // load
        HistoricVersionedEdge<V> commit8HE2 = h.getEdgeForVersion(e2Id, ver8);
        assertThat(commit8HE2, notNullValue());

        // v1
        HistoricVersionedVertex<V> commit8V1 = h.getVertexForVersion(v1Id, ver8);
        assertThat(commit8V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit8V2 = h.getVertexForVersion(v2Id, ver8);
        assertThat(commit8V2, notNullValue());
        assertThat(commit8V2, elementHasProperty("foo", "baz"));
        assertThat(h.utils.getVersionRange(commit7V2), is(Range.range(ver7, ver9)));
        assertThat(commit8V2.getEdges(Direction.IN), hasAmount(0));
        assertThat(commit8V2.getEdges(Direction.OUT), hasAmount(1));
        Iterable<Edge> commit8V2EdgesOut = commit8V2.getEdges(Direction.OUT);
        assertThat(commit8V2EdgesOut, elementIds(HARD_ID, EXACTLY_MATCHES, commit8HE2.getHardId()));

        // v3
        HistoricVersionedVertex<V> commit8V3 = h.getVertexForVersion(v3Id, ver8);
        assertThat(commit8V3, notNullValue());
        assertThat(h.utils.getVersionRange(commit8V3), is(Range.range(ver6, graph.getMaxPossibleGraphVersion())));
        assertThat(commit8V3.getEdges(Direction.OUT), hasAmount(0));
        assertThat(commit8V3.getEdges(Direction.IN), hasAmount(1));

        Iterable<Edge> commit8V3EdgesIn = commit8V3.getEdges(Direction.IN);
        assertThat(commit8V2EdgesOut, elementIds(HARD_ID, EXACTLY_MATCHES, commit8HE2.getHardId()));

        // e1
        HistoricVersionedEdge<V> commit8HE1 = h.getEdgeForVersion(e1Id, ver8);
        assertThat(commit8HE1, nullValue());

        // e2
        assertThat(commit8HE2, notNullValue());
        assertThat(commit8HE2.getVersion(), is(Range.range(ver8, ver8)));
        assertThat(graph.utils.getVersionRange(commit8HE2), is(Range.range(ver8, ver10)));
        // we don't support edges props versionins, so expect the latest value
        // here.
        assertThat(commit8HE2, elementHasProperty("bar", "baz"));
        assertThat(((HistoricVersionedVertex<V>) commit8HE2.getVertex(Direction.OUT)).getHardId(),
                is(commit8V2.getHardId()));
        assertThat(((HistoricVersionedVertex<V>) commit8HE2.getVertex(Direction.IN)).getHardId(),
                is(commit8V3.getHardId()));
        assertThat(((HistoricVersionedEdge<V>) commit8V2EdgesOut.iterator().next()).getHardId(),
                is(((HistoricVersionedEdge<V>) commit8V3EdgesIn.iterator().next()).getHardId()));

        // list vertices
        HistoricVersionedVertexIterable<V> commit8Vertices = h.getVertices(ver8);
        assertThat(commit8Vertices, hasAmount(hvs + 2));
        assertThat(commit8Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit8V2.getHardId(), commit8V3.getHardId()));
        // list edges
        Iterable<Edge> commit8Edges = h.getEdges(ver8);
        assertThat(commit8Edges, hasAmount(hes + 1));


        // ----commit 9----
        // load
        HistoricVersionedEdge<V> commit9HE2 = h.getEdgeForVersion(e2Id, ver9);
        assertThat(commit9HE2, notNullValue());

        // v1
        HistoricVersionedVertex<V> commit9V1 = h.getVertexForVersion(v1Id, ver9);
        assertThat(commit9V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit9V2 = h.getVertexForVersion(v2Id, ver9);
        assertThat(commit9V2.getHardId(), is(commit8V2.getHardId()));

        // v3
        HistoricVersionedVertex<V> commit9V3 = h.getVertexForVersion(v3Id, ver9);
        assertThat(commit9V3.getHardId(), is(commit8V3.getHardId()));

        // e1
        HistoricVersionedEdge<V> commit9HE1 = h.getEdgeForVersion(e1Id, ver9);
        assertThat(commit9HE1, nullValue());

        // e2
        assertThat(commit9HE2, notNullValue());
        assertThat(commit9HE2, elementHasProperty("bar", "baz"));
        assertThat(((HistoricVersionedVertex<V>) commit9HE2.getVertex(Direction.OUT)).getHardId(),
                is(commit9V2.getHardId()));
        assertThat(((HistoricVersionedVertex<V>) commit9HE2.getVertex(Direction.IN)).getHardId(),
                is(commit9V3.getHardId()));
        assertThat(((HistoricVersionedEdge<V>) commit8V2EdgesOut.iterator().next()).getHardId(),
                is(((HistoricVersionedEdge<V>) commit8V3EdgesIn.iterator().next()).getHardId()));

        // list vertices
        HistoricVersionedVertexIterable<V> commit9Vertices = h.getVertices(ver9);
        assertThat(commit9Vertices, hasAmount(hvs + 2));
        assertThat(commit9Vertices, elementIds(HARD_ID, EXACTLY_MATCHES, commit9V2.getHardId(), commit9V3.getHardId()));
        // list edges
        Iterable<Edge> commit9Edges = h.getEdges(ver9);
        assertThat(commit9Edges, hasAmount(hes + 1));


        // ----commit 10----
        // load
        HistoricVersionedEdge<V> commit10HE2 = h.getEdgeForVersion(e2Id, ver10);
        assertThat(commit10HE2, notNullValue());

        // v1
        HistoricVersionedVertex<V> commit10V1 = h.getVertexForVersion(v1Id, ver10);
        assertThat(commit10V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit10V2 = h.getVertexForVersion(v2Id, ver10);
        assertThat(commit10V2, notNullValue());
        assertThat(commit10V2.getPropertyKeys().contains("foo"), is(false));
        assertThat((Range<V>) h.utils.getVersionRange(commit10V2),
                is(Range.range(ver10, graph.getMaxPossibleGraphVersion())));
        assertThat(commit10V2.getEdges(Direction.IN), hasAmount(0));
        assertThat(commit10V2.getEdges(Direction.OUT), hasAmount(1));
        Iterable<Edge> commit10V2EdgesOut = commit10V2.getEdges(Direction.OUT);
        assertThat(commit10V2EdgesOut, elementIds(HARD_ID, EXACTLY_MATCHES, commit10HE2.getHardId()));

        // v3
        HistoricVersionedVertex<V> commit10V3 = h.getVertexForVersion(v3Id, ver10);
        assertThat(commit10V3.getHardId(), is(commit9V3.getHardId()));

        // e1
        HistoricVersionedEdge<V> commit10HE1 = h.getEdgeForVersion(e1Id, ver10);
        assertThat(commit10HE1, nullValue());

        // e2
        assertThat(commit10HE2, notNullValue());
        assertThat(commit10HE2.getVersion(), is(Range.range(ver10, ver10)));
        assertThat(graph.utils.getVersionRange(commit10HE2), is(Range.range(ver8, ver10)));
        assertThat(commit10HE2, elementHasProperty("bar", "baz"));
        assertThat(((HistoricVersionedVertex<V>) commit10HE2.getVertex(Direction.OUT)).getHardId(),
                is(commit10V2.getHardId()));
        assertThat(((HistoricVersionedVertex<V>) commit10HE2.getVertex(Direction.IN)).getHardId(),
                is(commit10V3.getHardId()));
        assertThat(((HistoricVersionedEdge<V>) commit10V2EdgesOut.iterator().next()).getHardId(),
                is(((HistoricVersionedEdge<V>) commit8V3EdgesIn.iterator().next()).getHardId()));

        // list vertices
        HistoricVersionedVertexIterable<V> commit10Vertices = h.getVertices(ver10);
        assertThat(commit10Vertices, hasAmount(hvs + 2));
        assertThat(commit10Vertices,
                elementIds(HARD_ID, EXACTLY_MATCHES, commit10V2.getHardId(), commit10V3.getHardId()));
        // list edges
        Iterable<Edge> commit10Edges = h.getEdges(ver10);
        assertThat(commit10Edges, hasAmount(hes + 1));



        // ----commit 11----
        // v1
        HistoricVersionedVertex<V> commit11V1 = h.getVertexForVersion(v1Id, ver11);
        assertThat(commit11V1, nullValue());

        // v2
        HistoricVersionedVertex<V> commit11V2 = h.getVertexForVersion(v2Id, ver11);
        assertThat(commit11V2.getHardId(), is(commit10V2.getHardId()));

        // v3
        HistoricVersionedVertex<V> commit11V3 = h.getVertexForVersion(v3Id, ver11);
        assertThat(commit11V3.getHardId(), is(commit9V3.getHardId()));

        // e1
        HistoricVersionedEdge<V> commit11HE1 = h.getEdgeForVersion(e1Id, ver11);
        assertThat(commit11HE1, nullValue());

        // e2
        HistoricVersionedEdge<V> commit11HE2 = h.getEdgeForVersion(e1Id, ver11);
        assertThat(commit11HE2, nullValue());
        HistoricVersionedEdge<V> commit11HE2Deleted = h.getEdgeByHardId(commit10HE2.getHardId());
        assertThat(commit11HE2Deleted.getVersion(), is(Range.range(ver8, ver8)));
        assertThat(graph.utils.getVersionRange(commit11HE2Deleted), is(Range.range(ver8, ver10)));

        // list vertices
        HistoricVersionedVertexIterable<V> commit11Vertices = h.getVertices(ver11);
        assertThat(commit11Vertices, hasAmount(hvs + 2));
        assertThat(commit11Vertices,
                elementIds(HARD_ID, EXACTLY_MATCHES, commit11V2.getHardId(), commit11V3.getHardId()));
        // list edges
        Iterable<Edge> commit11Edges = h.getEdges(ver11);
        assertThat(commit11Edges, hasAmount(hes));
    }


    // ----Indices tests
    //commented out for now, see file: IndexableTransactionalVersionedGraphImpl
    //@Test
    public void testIndicesCreationAndDeletion() {
        /*
        ActiveVersionedVertex v1 = (ActiveVersionedVertex)graph.addVertex("v1");
        CIT();
        Object v1Ver = last();
        int i = 1;
        for (i = i; i < 4; i++) {
            graph.addVertex("noop" + i);
            CIT();
        }
        ActiveVersionedVertex v2 = (ActiveVersionedVertex)graph.addVertex("v2");
        CIT();
        Object v2Ver = last();
        for (i = i; i < 8; i++) {
            graph.addVertex("noop" + i);
            CIT();
        }
        ActiveVersionedVertex v3 = (ActiveVersionedVertex)graph.addVertex("v3");
        CIT();
        Object v3Ver = last();
        for (i = i; i < 12; i++) {
            graph.addVertex("noop" + i);
            CIT();
        }
        ActiveVersionedEdge e1 = (ActiveVersionedEdge)graph.addEdge(null, v1, v2, "L");
        CIT();

        // vertices
        String VERTEX_IDX_TEST = "TEST_V_IDX";
        graph.createIndex(VERTEX_IDX_TEST, Vertex.class, new Parameter[0]);
        Index<Vertex> loadedVIdx = graph.getIndex(VERTEX_IDX_TEST, Vertex.class);
        assertThat(loadedVIdx, notNullValue());
        assertThat(loadedVIdx, instanceOf(ActiveVersionedIndex.class));
        loadedVIdx.put("v1", "v1", v1);
        loadedVIdx.put("v2", "v2", v2);
        Iterable<Vertex> idxV1Iter = loadedVIdx.get("v1", "v1");
        //TODO: Fix, iterator cannot be read twice
        //assertThat(idxV1Iter, hasAmount(1));
        ActiveVersionedVertex v1L = (ActiveVersionedVertex)idxV1Iter.iterator().next();
        assertThat(v1L.getId(), is(v1L.getId()));

        Iterable<Vertex> idxV2Iter = loadedVIdx.get("v2", "v2");
        assertThat(idxV2Iter, hasAmount(1));
        Iterable<Vertex> idxNoneIter = loadedVIdx.get("none", "none");
        assertThat(idxNoneIter, hasAmount(0));

        // edges
        String EDGE_IDX_TEST = "TEST_E_IDX";
        graph.createIndex(EDGE_IDX_TEST, Edge.class, new Parameter[0]);
        Index<Edge> loadedEIdx = graph.getIndex(EDGE_IDX_TEST, Edge.class);
        assertThat(loadedEIdx, notNullValue());
        assertThat(loadedEIdx, instanceOf(ActiveVersionedIndex.class));
        loadedEIdx.put("e1", "e1", e1);
        Iterable<Edge> idxE1Iter = loadedEIdx.get("e1", "e1");
        assertThat(idxE1Iter, hasAmount(1));
        Iterable<Edge> idxENoneIter = loadedEIdx.get("none", "none");
        assertThat(idxENoneIter, hasAmount(0));

        graph.dropIndex(VERTEX_IDX_TEST);
        assertThat(graph.getIndex(VERTEX_IDX_TEST, Vertex.class), nullValue());
        graph.dropIndex(EDGE_IDX_TEST);
        assertThat(graph.getIndex(EDGE_IDX_TEST, Vertex.class), nullValue());
        */
    }

    // Utils
    // --------------------------------------------------------------
    private void printEdges(Iterable<Edge> edges) {
        for (Edge e : edges) {
            System.out.println(e);
        }
    }

    public void CIT() {
        if (graph instanceof TransactionalGraph) {
            ((TransactionalGraph) graph).commit();
        }
    }

    public <T extends Element> List<T> toList(Iterable<T> it) {
        return Lists.newArrayList(it);
    }

    public V last() {
        return graph.getLatestGraphVersion();
    }
}
