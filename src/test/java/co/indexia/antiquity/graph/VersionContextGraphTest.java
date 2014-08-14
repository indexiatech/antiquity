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

import static co.indexia.antiquity.graph.VersionContextGraph.vc;
import static co.indexia.antiquity.graph.matchers.HasAmount.hasAmount;
import static co.indexia.antiquity.graph.matchers.HasElementIds.elementIds;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import co.indexia.antiquity.graph.matchers.HasElementIds;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.test.ImpermanentGraphDatabase;

/**
 * Test the {@link VersionContextGraph}.
 * 
 * <p>
 * Note: This test is bound specifically to the Long graph identifier type
 * </p>
 */
public class VersionContextGraphTest {
    static private TransactionalVersionedGraph<?, Long> graph;
    static private HistoricVersionedGraph<?, Long> h;
    static int initialVerticesAmount;
    static ActiveVersionedVertex vertex1;
    static String vertex1Id;
    static Long ver1;
    static String vertex2Id;
    static ActiveVersionedVertex vertex2;
    static Long ver2;
    static Edge e1;
    static Long ver3;
    static VersionContextGraph<Long> vc1;
    static VersionContextGraph<Long> vc2;
    static VersionContextGraph<Long> vc3;

    @BeforeClass
    public static void setUp() {
        graph =
                (TransactionalVersionedGraph<?, Long>) new ActiveVersionedGraph.ActiveVersionedTransactionalGraphBuilder<Neo4j2Graph, Long>(
                        new Neo4j2Graph(new ImpermanentGraphDatabase()), new LongGraphIdentifierBehavior()).init(true)
                        .build();
        h = graph.getHistoricGraph();

        // vertex1
        vertex1 = (ActiveVersionedVertex) graph.addVertex("foo");
        vertex1Id = (String) vertex1.getId();
        vertex1.setProperty("fooKey1", "foo1");
        vertex1.setProperty("fooKey2", "foo2");
        graph.commit();
        ver1 = graph.getLatestGraphVersion();
        // vertex2
        vertex2 = (ActiveVersionedVertex) graph.addVertex("bar");
        vertex2Id = (String) vertex2.getId();
        vertex2.setProperty("barKey1", "bar1");
        vertex2.setProperty("barKey2", "bar2");
        vertex1.setProperty("fooKey1", "foo1New");
        graph.commit();
        ver2 = graph.getLatestGraphVersion();
        // edge1
        e1 = graph.addEdge(null, vertex1, vertex2, "LINK");
        graph.commit();
        ver3 = graph.getLatestGraphVersion();
        vc1 = vc(h, ver1);
        assertThat(vc1, notNullValue());
        vc2 = vc(h, ver2);
        assertThat(vc2, notNullValue());
        vc3 = vc(h, ver3);
        assertThat(vc3, notNullValue());
    }

    @Test
    public void contextCreationTest() {
        Long v104 = 104L;
        VersionContextGraph<Long> vc = vc(graph.getHistoricGraph(), v104);
        assertThat(vc, notNullValue());
        assertThat(v104, is(vc.getVersion()));

        assertThat(vc1.getVersion(), is(ver1));
        assertThat(vc2.getVersion(), is(ver2));
    }

    @Test
    public void getVertexByIdTest() {
        VersionContextGraph<Long> vc = vc(h, ver1);
        VersionContextGraph<Long> vc2 = vc(h, ver2);

        assertThat((String) vc.getVertex(vertex1Id).getProperty("fooKey1"), is("foo1"));
        assertThat((String) vc2.getVertex(vertex1Id).getProperty("fooKey1"), is("foo1New"));

        assertThat(vc1.getVertex(vertex1Id), instanceOf(HistoricVersionedVertex.class));
        assertThat(vc1.getVertex(vertex2Id), nullValue());
        assertThat(vc2.getVertex(vertex1Id).getId(), is(vertex1.getId()));
    }

    @Test
    public void gettingAllVerticesTest() {
        assertThat(vc1.getVertices(), elementIds(HasElementIds.ID.ID, HasElementIds.TYPE.EXACTLY_MATCHES, vertex1Id));
        assertThat(vc2.getVertices(),
                elementIds(HasElementIds.ID.ID, HasElementIds.TYPE.EXACTLY_MATCHES, vertex1Id, vertex2Id));
    }

    @Test
    public void gettingAllEdgesTest() {
        assertThat(vc1.getEdges(), hasAmount(0));
        assertThat(vc2.getEdges(), hasAmount(0));
        assertThat(vc3.getEdges(), hasAmount(1));
        assertThat(vc3.getEdges(), elementIds(HasElementIds.ID.ID, HasElementIds.TYPE.EXACTLY_MATCHES, e1.getId()));
    }

    @Test
    public void hasRevisionTest() {
        assertThat(vc1.hasRevision(vertex1), is(true));
        assertThat(vc1.hasRevision(vertex2), is(false));
        assertThat(vc2.hasRevision(vertex1), is(true));
        assertThat(vc2.hasRevision(vertex2), is(true));
    }

    @Test
    public void getPropertyTest() {
        assertThat((String) vc1.getProperty(vertex1, "fooKey1"), is("foo1"));
        assertThat((String) vc2.getProperty(vertex1, "fooKey1"), is("foo1New"));
        assertThat((String) vc2.getProperty(vertex2, "barKey2"), is("bar2"));
    }
}
