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
package co.indexia.antiquity.graph;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.GraphQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrappedGraphQuery;
import co.indexia.antiquity.range.Range;

/**
 * An historic {@link GraphQuery} wrapper.
 */
public class HistoricGraphQuery<V extends Comparable<V>> extends WrappedGraphQuery {
    /**
     * The version to filter by, this property is propagated to the iterables
     */
    private V version = null;

    /**
     * Associated graph instance.
     */
    private final HistoricVersionedGraph<?, V> hg;

    /**
     * If true internal elements will be included in the query result, this
     * property is propagated to the iterables.
     */
    private Boolean withInternals = false;

    /**
     * Create instance
     * 
     * @param hg associated graph
     * @param query query instance to be wrapped
     */
    public HistoricGraphQuery(HistoricVersionedGraph<?, V> hg, GraphQuery query) {
        super(query.has(VEProps.HISTORIC_ELEMENT_PROP_KEY, true));
        this.hg = hg;
    }

    @Override
    public Iterable<Edge> edges() {
        return new HistoricVersionedEdgeIterable<V>(this.query.edges(), hg, getVersion(), withInternals);
    }

    @Override
    public Iterable<Vertex> vertices() {
        return new HistoricVersionedVertexIterable<V>(this.query.vertices(), hg, getVersion());
    }

    private Range<V> getVersion() {
        return version == null ? null : Range.range(version, version);
    }

    public HistoricGraphQuery<V> forVersion(V version) {
        this.version = version;
        return this;
    }

    public HistoricGraphQuery<V> withInternals(Boolean withInternals) {
        this.withInternals = withInternals;
        return this;
    }
}
