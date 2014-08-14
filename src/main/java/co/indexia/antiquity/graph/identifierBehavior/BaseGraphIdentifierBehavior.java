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
 * Base class of the {@link GraphIdentifierBehavior} interface.
 * 
 * @param <V> The graph identifier type.
 */
public abstract class BaseGraphIdentifierBehavior<V extends Comparable<V>> implements GraphIdentifierBehavior<V> {
    protected VersionedGraphBase<?, V> graph;

    /**
     * Get the {@link co.indexia.antiquity.graph.VersionedGraphBase}
     * instance associated with this behavior
     * 
     * @return The associated
     *         {@link co.indexia.antiquity.graph.VersionedGraphBase}
     */
    protected VersionedGraphBase<?, V> getGraph() {
        return this.graph;
    }

    @Override
    public void setGraph(VersionedGraphBase<?, V> graph) {
        this.graph = graph;
    }
}
