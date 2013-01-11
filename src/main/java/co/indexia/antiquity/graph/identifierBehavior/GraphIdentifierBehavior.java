package co.indexia.antiquity.graph.identifierBehavior;

import co.indexia.antiquity.graph.VersionedGraph;

/**
 * A {@link VersionedGraph} identifier behavior interface.
 * 
 * @see VersionedGraph
 */
public interface GraphIdentifierBehavior<V extends Comparable<V>> {
	/**
	 * Set the {@link VersionedGraph} instance.
	 * 
	 * @param versionedGraph
	 *            The {@link VersionedGraph} instance to set
	 */
	public void setGraph(VersionedGraph<?, V> versionedGraph);

	/**
	 * Get the latest(current) graph version.
	 * 
	 * <p>
	 * Graph latest version is stored in the graph version configuration vertex.
	 * <p>
	 * 
	 * @see VersionedGraph#getVersionConfVertex()
	 * 
	 * @return The latest graph version.
	 */
	public V getLatestGraphVersion();

	/**
	 * Get the next graph version.
	 * 
	 * @param currentVersion
	 *            The current version of the graph.
	 */
	public V getNextGraphVersion(V currentVersion);

	/**
	 * Get the maximum possible graph version.
	 * 
	 * @return The maximum possible graph version
	 */
	public V getMaxPossibleGraphVersion();
}
