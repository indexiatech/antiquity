package co.indexia.antiquity.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.test.ImpermanentGraphDatabase;

import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.neo4j.Neo4jGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import co.indexia.antiquity.range.Range;

public class TransactionalVersionedGraphTest {
	private TransactionalVersionedGraph<Neo4jGraph, Long> graph;

	@Before
	public void setUp() {
		Neo4jGraph baseGraph = new Neo4jGraph(new ImpermanentGraphDatabase());
		graph = new TransactionalVersionedGraph<Neo4jGraph, Long>(baseGraph, new LongGraphIdentifierBehavior());
	}

	@After
	public void tearDown() {
		graph.shutdown();
	}

	@Test
	public void fewAddAndRemovalOfVerticesInTheSameTransactionShouldHaveTheSameVersion() {
		Vertex initial = graph.addVertex("vInit");
		graph.commit();
		Long ver1 = graph.getLatestGraphVersion();
		assertNotNull(ver1);

		Vertex v1 = graph.addVertex("v1");
		Vertex v2 = graph.addVertex("v2");
		Vertex v3 = graph.addVertex("v3");
		graph.removeVertex(initial);
		System.out.println(ElementUtils.getElementPropsAsString(((VersionedVertex<Long>) v1).getBaseVertex(), true));
		graph.commit();
		Long ver2 = graph.getLatestGraphVersion();

		assertEquals(Range.range(ver2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(v1));
		assertEquals(Range.range(ver2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(v2));
		assertEquals(Range.range(ver2, graph.getMaxPossibleGraphVersion()), graph.getVersionRange(v3));
		assertEquals(Range.range(ver1, ver1), graph.getVersionRange(initial));
	}

	@Test
	public void createVerticesAndSetPropertiesShouldHaveTheSameVersion() {
		Vertex v1 = graph.addVertex("v1");
		Vertex v2 = graph.addVertex("v2");
		Vertex v3 = graph.addVertex("v3");
		v1.setProperty("key1", "foo1");
		v2.setProperty("key2", "foo2");
		v3.setProperty("key3", "foo3");
		graph.commit();
		Long ver = graph.getLatestGraphVersion();

		// TODO: Consider setting forVersion automatically
		((VersionedVertex<Long>) v1).setForVersion(ver);
		((VersionedVertex<Long>) v2).setForVersion(ver);
		((VersionedVertex<Long>) v3).setForVersion(ver);

		Assert.assertEquals("foo1", v1.getProperty("key1"));
		Assert.assertEquals("foo2", v2.getProperty("key2"));
		Assert.assertEquals("foo3", v3.getProperty("key3"));
	}
}
