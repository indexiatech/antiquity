/**
 * Copyright (c) 2012-2013 "Indexia Technologies, ltd."
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
     * @param versionedGraph The {@link VersionedGraph} instance to set
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
     * @param currentVersion The current version of the graph.
     */
    public V getNextGraphVersion(V currentVersion);

    /**
     * Get the maximum possible graph version.
     *
     * @return The maximum possible graph version
     */
    public V getMaxPossibleGraphVersion();
}
