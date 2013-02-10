/**
 * Copyright (c) 2012-2013 "Indexia Technologies, ltd."
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
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;

import static co.indexia.antiquity.graph.VersionContextGraph.vc;

import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.test.ImpermanentGraphDatabase;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link VersionContextGraph}.
 *
 * <p>
 * Note: This test is bound specifically to the Long graph identifier type
 * </p>
 */
public class VersionContextGraphTest {
    static private TransactionalVersionedGraph<Neo4jGraph, Long> graph;
    static int initialVerticesAmount;
    static Vertex vertex1;
    static String vertex1Id;
    static Long ver1;
    static String vertex2Id;
    static Vertex vertex2;
    static Edge e1;
    static Long ver2;
    static VersionContextGraph<Long> vc1;
    static VersionContextGraph<Long> vc2;

    @BeforeClass
    public static void setUp() {
        Neo4jGraph base = new Neo4jGraph(new ImpermanentGraphDatabase());

        graph = new TransactionalVersionedGraph<Neo4jGraph, Long>(base, new LongGraphIdentifierBehavior());

        initialVerticesAmount = Lists.newArrayList(graph.getVertices()).size();
        vertex1 = graph.addVertex("foo");
        vertex1Id = (String) vertex1.getId();
        vertex1.setProperty("fooKey1", "foo1");
        vertex1.setProperty("fooKey2", "foo2");
        graph.commit();
        ver1 = graph.getLatestGraphVersion();
        vertex2 = graph.addVertex("bar");
        vertex2Id = (String) vertex2.getId();
        vertex2.setProperty("barKey1", "bar1");
        vertex2.setProperty("barKey2", "bar2");
        vertex1.setProperty("fooKey1", "foo1New");
        e1 = graph.addEdge(null, vertex1, vertex2, "LINK");
        graph.commit();
        ver2 = graph.getLatestGraphVersion();

        vc1 = vc(graph, ver1);
        vc2 = vc(graph, ver2);
    }

    @Test
    public void contextCreationTest() {
        Long v104 = 104L;
        VersionContextGraph<Long> vc = vc(graph, v104);
        assertNotNull(vc);
        assertEquals(v104, vc.getVersion());
    }

    @Test
    public void getVertexByIdTest() {
        VersionContextGraph<Long> vc = vc(graph, ver1);
        VersionContextGraph<Long> vc2 = vc(graph, ver2);

        assertThat((String) vc.getVertex(vertex1Id).getProperty("fooKey1"), is("foo1"));
        assertThat((String) vc2.getVertex(vertex1Id).getProperty("fooKey1"), is("foo1New"));

        assertThat(vc2.getVertex(vertex1Id).getId(), is(vertex1.getId()));
        // This is should be a historical vertex
        assertThat(vc1.getVertex(vertex1Id), not(vertex1));

        try {
            vc1.getVertex(vertex2Id);
            assertFalse(true);
        } catch (NotFoundException e) {

        }
    }

    @Test
    public void gettingAllVerticesTest() {
        assertThat(Lists.newArrayList(vc1.getVertices()).size(), is(initialVerticesAmount + 1));
        assertThat(Lists.newArrayList(vc2.getVertices()).size(), is(initialVerticesAmount + 2));

        boolean foo1Valid = false;
        for (Vertex v : vc1.getVertices()) {
            if (v.getPropertyKeys().contains("fooKey1")) {
                foo1Valid = v.getProperty("fooKey1").equals("foo1");
            }
        }
        assertTrue(foo1Valid);

        boolean foo1NewValid = false;
        for (Vertex v : vc2.getVertices()) {
            if (v.getPropertyKeys().contains("fooKey1")) {
                foo1NewValid = v.getProperty("fooKey1").equals("foo1New");
            }
        }
        assertTrue(foo1NewValid);
    }

    @Test
    public void gettingAllEdgesTest() {
        assertThat(vc1.getEdges().iterator().hasNext(), is(false));
        assertThat(vc2.getEdges().iterator().hasNext(), is(true));
        assertThat(vc2.getEdges().iterator().hasNext(), is(true));
        assertThat(vc2.getEdges().iterator().next().getId(), is(e1.getId()));
    }

    @Test
    public void hasRevisionTest() {
        assertThat(vc1.hasRevision(vertex1), is(true));
        assertThat(vc1.hasRevision(vertex2), is(false));
    }

    @Test
    public void getPropertyTest() {
        assertThat((String) vc1.getProperty(vertex1, "fooKey1"), is("foo1"));
        assertThat((String) vc2.getProperty(vertex1, "fooKey1"), is("foo1New"));
    }
}
