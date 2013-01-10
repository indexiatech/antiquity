package com.vertixtech.antiquity.graph.identifierBehavior;

import com.vertixtech.antiquity.graph.VersionedGraph;

/**
 * The base class of all {@link GraphIdentifierBehavior} interface.
 * 
 * @param <V>
 *            The graph identifier type.
 */
public abstract class BaseGraphIdentifierBehavior<V extends Comparable<V>> implements GraphIdentifierBehavior<V> {
	private VersionedGraph<?, V> graph;

	public BaseGraphIdentifierBehavior() {
	}

	/**
	 * Get the {@link VersionedGraph} instance associated with this behavior
	 * 
	 * @return The associated {@link VersionedGraph}
	 */
	protected VersionedGraph<?, V> getGraph() {
		return this.graph;
	}

	@Override
	public void setGraph(VersionedGraph<?, V> versionedGraph) {
		this.graph = versionedGraph;
	}
}
