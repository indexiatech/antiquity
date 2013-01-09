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

import com.tinkerpop.blueprints.Graph;

/**
 * An incremental {@link Long} non-transactional versioned graph.
 * 
 * @param <T>
 *            The type of the underline graph
 * 
 * @see NonTransactionalVersionedGraph
 * @see Long
 */
public class LongVersionedGraph<T extends Graph> extends NonTransactionalVersionedGraph<T, Long> {

	public LongVersionedGraph(T baseGraph) {
		super(baseGraph);
	}

	@Override
	protected Long getLatestGraphVersionImpl() {
		Long lastVer = (Long) getVersionConfVertex().getProperty(LATEST_GRAPH_VERSION_PROP_KEY);
		if (lastVer == null)
			return 0L;
		else
			return lastVer;

	}

	@Override
	protected Long getNextGraphVersionImpl() {
		long latestVersion = getLatestGraphVersion();
		long nextVersion = latestVersion + 1;

		return nextVersion;
	}

	@Override
	protected Long getMaxPossibleGraphVersion() {
		return Long.MAX_VALUE;
	}

	@Override
	protected void setLatestGraphVersion(Long newVersionToBeCommitted) {
		getVersionConfVertex().setProperty(LATEST_GRAPH_VERSION_PROP_KEY, newVersionToBeCommitted);
	}
}
