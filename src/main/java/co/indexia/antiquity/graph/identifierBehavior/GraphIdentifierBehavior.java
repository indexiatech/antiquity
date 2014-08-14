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
package co.indexia.antiquity.graph.identifierBehavior;

import co.indexia.antiquity.graph.VersionedGraphBase;

/**
 * A {@link co.indexia.antiquity.graph.ActiveVersionedGraph} identifier
 * behavior interface.
 * 
 * Defines the behavior of the graph identifier, this interface should be
 * implemented per identifier type (e.g {@link Long}, {@link java.sql.Timestamp}
 * , etc.)
 * 
 * The identifier type must implements the {@link Comparable} interface for
 * versions comparison.
 * 
 * @see co.indexia.antiquity.graph.ActiveVersionedGraph
 */
public interface GraphIdentifierBehavior<V extends Comparable<V>> {
    /**
     * Get the latest(current) graph version.
     * 
     * <p>
     * Graph latest version is stored in the root historic vertex.
     * <p>
     * 
     * @see co.indexia.antiquity.graph.ActiveVersionedGraph#getRootVertex()
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
     * Get the minimum possible graph version.
     * 
     * @return The minimum possible graph version.
     */
    public V getMinPossibleGraphVersion();

    /**
     * Get the maximum possible graph version.
     * 
     * @return The maximum possible graph version
     */
    public V getMaxPossibleGraphVersion();

    /**
     * Set the {@link co.indexia.antiquity.graph.VersionedGraphBase}
     * instance associated with this behavior
     */
    public void setGraph(VersionedGraphBase<?, V> graph);
}
