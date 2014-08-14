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

import com.tinkerpop.blueprints.impls.neo4j2.Neo4j2Graph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import org.neo4j.test.ImpermanentGraphDatabase;

/**
 * Neo4j2 tests
 */
public class Neo4j2TxLongVersionedGraphTest extends TransactionalLongVersionedGraphTest {
    @Override
    protected ActiveVersionedGraph<?, Long> generateGraph() {
        return new ActiveVersionedGraph.ActiveVersionedTransactionalGraphBuilder<Neo4j2Graph, Long>(new Neo4j2Graph(
                new ImpermanentGraphDatabase()), new LongGraphIdentifierBehavior()).init(true).conf(null).build();
    }
}
