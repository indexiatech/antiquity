/**
 * Copyright (c) 2012-2013 "Indexia Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package co.indexia.antiquity.graph;

import java.lang.reflect.Method;
import java.util.ArrayList;

import junit.framework.Assert;

import org.neo4j.test.ImpermanentGraphDatabase;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import co.indexia.antiquity.range.Range;

public class TransactionalLongVersionedGraphTest extends VersionedGraphTest {
	public TransactionalVersionedGraph<Neo4jGraph, Long> graph;

	@Override
	public Graph generateGraph() {
		return generateGraph("graph");
	}

	@Override
	public Graph generateGraph(final String graphDirectoryName) {
		Neo4jGraph baseGraph = new Neo4jGraph(new ImpermanentGraphDatabase());
		return new TransactionalVersionedGraph<Neo4jGraph, Long>(baseGraph, new LongGraphIdentifierBehavior());
	}

	@Override
	public Graph generateGraph(final String graphDirectoryName, Configuration conf) {
		Neo4jGraph baseGraph = new Neo4jGraph(new ImpermanentGraphDatabase());
		return new TransactionalVersionedGraph<Neo4jGraph, Long>(baseGraph, new LongGraphIdentifierBehavior(), conf);
	}

	@Override
	public void doTestSuite(TestSuite testSuite) throws Exception {
		for (Method method : testSuite.getClass().getDeclaredMethods()) {
			if (method.getName().startsWith("test")) {
				System.out.println("Testing " + method.getName() + "...");
				method.invoke(testSuite);
			}
		}
	}

	@Override
	public void setUp() throws Exception {
		graph = (TransactionalVersionedGraph<Neo4jGraph, Long>) generateGraph(null);
	}

	@Override
	public void tearDown() throws Exception {
		graph.shutdown();
	}

	public void commit() {
		graph.commit();
	}

	public void testVersionedGraphTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new VersionedGraphTestSuite<Long>(this));
		printTestPerformance("GraphTestSuite", this.stopWatch());
	}

	public void testFewAddAndRemovalOfVerticesInTheSameTransactionShouldHaveTheSameVersion() {
		Vertex initial = graph.addVertex("vInit");
		((TransactionalGraph) graph).commit();
		Long ver1 = graph.getLatestGraphVersion();
		assertNotNull(ver1);

		Vertex v1 = graph.addVertex("v1");
		Vertex v2 = graph.addVertex("v2");
		Vertex v3 = graph.addVertex("v3");
		graph.removeVertex(initial);
		System.out.println(ElementUtils.getElementPropsAsString(((VersionedVertex<Long>) v1).getBaseVertex(), true));
		((TransactionalGraph) graph).commit();
		Long ver2 = graph.getLatestGraphVersion();

		assertEquals(Range.range(ver2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(v1));
		assertEquals(Range.range(ver2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(v2));
		assertEquals(Range.range(ver2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(v3));
		assertEquals(Range.range(ver1, ver1), graph.getVersionRange(initial));
	}

	public void testCreateVerticesAndSetPropertiesShouldHaveTheSameVersion() {
		Vertex v1 = graph.addVertex("v1");
		Vertex v2 = graph.addVertex("v2");
		Vertex v3 = graph.addVertex("v3");
		v1.setProperty("key1", "foo1");
		v2.setProperty("key2", "foo2");
		v3.setProperty("key3", "foo3");
		((TransactionalGraph) graph).commit();
		Long ver = graph.getLatestGraphVersion();

		// TODO: Consider setting forVersion automatically
		((VersionedVertex<Long>) v1).setForVersion(ver);
		((VersionedVertex<Long>) v2).setForVersion(ver);
		((VersionedVertex<Long>) v3).setForVersion(ver);

		Assert.assertEquals("foo1", v1.getProperty("key1"));
		Assert.assertEquals("foo2", v2.getProperty("key2"));
		Assert.assertEquals("foo3", v3.getProperty("key3"));
	}

	public void testQueringUncommittedChangesShouldCauseException() {
		Vertex fooV = graph.addVertex("fooV");
		try {
			fooV.getPropertyKeys().size();
			assertTrue(false);
		} catch (IllegalStateException e) {
			try {
				fooV.setProperty("key", "foo");
				fooV.getPropertyKeys().size();
				assertTrue(false);
			} catch (IllegalStateException ex) {

			}
		}

		graph.commit();
		assertTrue(fooV.getPropertyKeys().size() > 0);
		assertEquals("foo", fooV.getProperty("key"));
	}

	public void testCreateAndSetOnSameVertexShouldCreateOnlyVertexOnly() {
		VersionedVertex<Long> fooV = (VersionedVertex<Long>) graph.addVertex("fooV");
		// Base as currently transient vertex cannot be queried
		assertNull(fooV.getBaseElement().getProperty("key1"));
		assertNull(fooV.getBaseElement().getProperty("key2"));
		fooV.setProperty("key1", "foo");
		assertEquals("foo", fooV.getBaseElement().getProperty("key1"));
		fooV.setProperty("key2", "bar");
		assertEquals("foo", fooV.getBaseElement().getProperty("key1"));
		assertEquals("bar", fooV.getBaseElement().getProperty("key2"));
		graph.commit();
		ArrayList<Vertex> vertexChain = new ArrayList<Vertex>();
		VersionedGraph.getVertexChain(vertexChain, fooV);
		assertEquals(1, vertexChain.size());
	}
}
