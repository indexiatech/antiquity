package co.indexia.antiquity.graph.identifierBehavior;

import co.indexia.antiquity.graph.VersionedGraph;

/**
 * A graph identifier behavior implementation based on an incremental {@link Long} type.
 */
public class LongGraphIdentifierBehavior extends BaseGraphIdentifierBehavior<Long> {

	public LongGraphIdentifierBehavior() {
	}

	@Override
	public Long getMaxPossibleGraphVersion() {
		return Long.MAX_VALUE;
	}

	@Override
	public Long getLatestGraphVersion() {
		Long lastVer =
				(Long) getGraph().getVersionConfVertex().getProperty(VersionedGraph.LATEST_GRAPH_VERSION_PROP_KEY);
		if (lastVer == null)
			return 0L;
		else
			return lastVer;
	}

	@Override
	public Long getNextGraphVersion(Long currentVersion) {
		long nextVersion = currentVersion + 1;

		return nextVersion;
	}
}
