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
