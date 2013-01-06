/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
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
package com.vertixtech.antiquity.graph;

import java.lang.reflect.Method;
import java.util.ArrayList;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventIndexableGraph;
import com.vertixtech.antiquity.range.Range;

public class VersionedGraphTest extends GraphTest {
	private VersionedGraph<TinkerGraph, Long> graph;
	
	public Graph generateGraph() {
		return generateGraph("");
	}

	public Graph generateGraph(final String graphDirectoryName) {
		return new EventIndexableGraph<TinkerGraph>(new TinkerGraph());
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		graph = new LongVersionedGraph<TinkerGraph>(new TinkerGraph());
		
	}

	@Override
	public void doTestSuite(final TestSuite testSuite) throws Exception {
		for (Method method : testSuite.getClass().getDeclaredMethods()) {
			if (method.getName().startsWith("test")) {
				System.out.println("Testing " + method.getName() + "...");
				method.invoke(testSuite);
			}
		}
	}
	
	public void testGraphConfVertexShouldExist() {
		assertTrue(graph.getVersionConfVertex() != null);
	}
	
	public void testAddingNewVersionedVertices() {
		Long initialVer = graph.getLatestGraphVersion();
		Vertex fooV = graph.addVertex("fooV");
		Long fooVVer1 = graph.getLatestGraphVersion();
		assertEquals(Range.range(fooVVer1, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(fooV));
		fooV.setProperty("prop", "foo");
		System.out.println(getGraphNodesString());
		Long fooVVer2 = graph.getLatestGraphVersion();
		Vertex barV = graph.addVertex("barV");
		Long barVVer1 = graph.getLatestGraphVersion();
		barV.setProperty("prop", "bar");
		Long barVVer2 = graph.getLatestGraphVersion();
		
		Vertex fooVLoaded = graph.getVertex(fooV.getId());
		Vertex barVLoaded = graph.getVertex(barV.getId());
		assertEquals(fooV, fooVLoaded);
		assertEquals(barV, barVLoaded);
		
		assertEquals(Range.range(fooVVer2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(fooVLoaded));
		assertEquals(Range.range(barVVer2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(barVLoaded));
		
	}
	
	public void testAddingNewVersionedEdges() {
		Vertex fooV = graph.addVertex("fooV");
		Vertex barV = graph.addVertex("barV");
		Edge e = graph.addEdge("fooBarE", fooV, barV, "LINKED");
		
		assertEquals(e, graph.getEdge(e.getId()));
	}
	
	public void testRemovingVersionedVertices() {
		Vertex fooV = graph.addVertex("fooV");
		Long fooVer = graph.getLatestGraphVersion();
		Vertex barV = graph.addVertex("barV");
		Long barVer = graph.getLatestGraphVersion();
		graph.removeVertex(fooV);
		Long barPostFooRemovalVer = graph.getLatestGraphVersion();
		assertEquals(Range.range(fooVer, barVer), graph.getVersionRange(graph.getVertex(fooV.getId())));
		assertEquals(Range.range(barVer, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(graph.getVertex(barV.getId())));
		graph.removeVertex(barV);
		assertEquals(Range.range(barVer, barPostFooRemovalVer), graph.getVersionRange(graph.getVertex(barV.getId())));
	}
	
	public void testVersionedVertexProperties() {
		Vertex v = graph.addVertex("fooV");
		Long emptyVersion = graph.getLatestGraphVersion();
		v.setProperty("prop", "foo");
		Long fooVersion = graph.getLatestGraphVersion();
		v.setProperty("prop", "bar");
		Long barVersion = graph.getLatestGraphVersion();
		v.setProperty("prop", "baz");
		Long bazVersion = graph.getLatestGraphVersion();
		v.setProperty("prop", "qux");
		Long quxVersion = graph.getLatestGraphVersion();
		
		assertEquals("foo", graph.getVertexForVersion(v, fooVersion).getProperty("prop")); 
		assertEquals("bar", graph.getVertexForVersion(v, barVersion).getProperty("prop"));
		assertEquals("baz", graph.getVertexForVersion(v, bazVersion).getProperty("prop"));
		assertEquals("qux", graph.getVertexForVersion(v, quxVersion).getProperty("prop"));
		
		ArrayList<Vertex> chain = new ArrayList<Vertex>();
		getVertexChain(chain, v);
		assertEquals(5, chain.size());
		
		assertEquals(chain.get(0), v);
		assertEquals(chain.get(1), graph.getVertexForVersion(v, bazVersion));
		assertEquals(chain.get(2), graph.getVertexForVersion(v, barVersion));
		assertEquals(chain.get(3), graph.getVertexForVersion(v, fooVersion));
		assertEquals(chain.get(4), graph.getVertexForVersion(v, emptyVersion));
	}
	
	private String getGraphNodesString() {
		StringBuffer graphStr = new StringBuffer();
		for (Vertex v : graph.getVertices()) {
			graphStr.append("Vertex [" +  v.getId() + "]");
			
			for (String key : v.getPropertyKeys()) {
				graphStr.append("\n");
				graphStr.append("\tProp [").append(key).append("] with value [").append(v.getProperty(key)).append("]");
			}
			
			graphStr.append("\n");
		}
		
		return graphStr.toString();
	}
	
	private void printEdges(Iterable<Edge> edges) {
		for (Edge e : edges) {
			System.out.println(e);
		}
	}
	
	private ArrayList<Vertex> getVertexChain(ArrayList<Vertex> chain, Vertex v) {
		chain.add(v);
		
		Iterable<Edge> edges = v.getEdges(Direction.OUT, VersionedGraph.PREV_VERSION_CHAIN_EDGE_TYPE);
		if (edges.iterator().hasNext()) {
			Vertex next = edges.iterator().next().getVertex(Direction.IN);
			getVertexChain(chain, next);
		}
		
		return chain;
	}
}
