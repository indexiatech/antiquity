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

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import com.vertixtech.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;

public class NonTransactionalLongTypeVersionedGraphTest extends GraphTest {
	private VersionedGraph<TinkerGraph, Long> graph;

	@Override
	public Graph generateGraph() {
		return generateGraph("graph");
	}

	@Override
	public Graph generateGraph(String graphDirectoryName) {
		return new NonTransactionalVersionedGraph<TinkerGraph, Long>(new TinkerGraph(),
				new LongGraphIdentifierBehavior());
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		graph = (NonTransactionalVersionedGraph<TinkerGraph, Long>) generateGraph();
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

	public void testVersionedGraphTestSuite() throws Exception {
		this.stopWatch();
		doTestSuite(new VersionedGraphTestSuite<Long>(this));
		printTestPerformance("GraphTestSuite", this.stopWatch());
	}
}
