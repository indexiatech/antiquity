package co.indexia.antiquity.graph;

import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventEdge;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;

/**
 * An {@link Edge} implementation that supports versioning capabilities.
 */
public class VersionedEdge<V extends Comparable<V>> extends EventEdge {
	private final VersionedGraph<?, V> graph;
	private final V version;

	protected VersionedEdge(Edge rawEdge,
			List<GraphChangedListener> graphChangedListeners,
			EventTrigger trigger,
			VersionedGraph<?, V> graph,
			V version) {
		super(rawEdge, graphChangedListeners, trigger);
		this.graph = graph;
		this.version = version;
	}

	@Override
	public Vertex getVertex(final Direction direction) throws IllegalArgumentException {
		return new VersionedVertex<V>(this.getBaseEdge().getVertex(direction),
				this.graphChangedListeners,
				this.trigger,
				graph,
				version);
	}
}
