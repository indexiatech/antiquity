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

import java.util.Set;

import com.google.common.base.Preconditions;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Features;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.KeyIndexableGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.WrapperGraph;
import co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;

/**
 * Base class for {@link ActiveVersionedGraph} and
 * {@link HistoricVersionedGraph}.
 */
public abstract class VersionedGraphBase<T extends KeyIndexableGraph, V extends Comparable<V>>
        implements KeyIndexableGraph, WrapperGraph<T> {
    /**
     * Supported graph features
     */
    private final Features features;

    /**
     * Wrapped graph {@link T} instance
     */
    T underlineGraph;

    /**
     * Graph configuration
     */
    protected final Configuration conf;

    /**
     * The identifier behavior associated with this graph
     */
    protected GraphIdentifierBehavior<V> identifierBehavior;
    protected final VersionedElementUtils<V> utils = new VersionedElementUtils();

    public VersionedGraphBase(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Configuration conf) {
        Preconditions.checkNotNull(baseGraph, "Base graph must be set.");
        Preconditions.checkNotNull(identifierBehavior, "Identifier behavior must be set.");

        this.underlineGraph = baseGraph;
        this.identifierBehavior = identifierBehavior;
        this.identifierBehavior.setGraph(this);

        if (conf == null) {
            this.conf = new Configuration.ConfBuilder().build();
        } else {
            this.conf = conf;
        }

        this.features = baseGraph.getFeatures().copyFeatures();
        features.isWrapper = true;

        if (this.conf.useNaturalIds
                || (this.conf.useNaturalIdsOnlyIfSuppliedIdsAreIgnored && baseGraph.getFeatures().ignoresSuppliedIds)) {
            features.ignoresSuppliedIds = true;
        }
    }

    /**
     * Ensure graph integrity, must be invoked by the graph's builder before
     * returning the graph instance to the user.
     * 
     * @see ActiveVersionedGraph.ActiveVersionedGraphBuilder
     */
    protected void validate() {
        // TODO: Is verifiying that conf vertex exist is sufficient?
        try {
            getRootVertex();
        } catch (IllegalStateException e) {
            throw new IllegalStateException(
                    "Versioned graph was not initialized properly, maybe forgot to call init() first?");
        }
    }

    @Override
    public T getBaseGraph() {
        return underlineGraph;
    }

    @Override
    public Features getFeatures() {
        return features;
    }

    @Override
    public void shutdown() {
        getBaseGraph().shutdown();
    }

    // Enhancements to standard graph methods
    // --------------------------------------------------------------
    public Vertex getSingleVertex(String key, Object value) {
        return ElementUtils.getSingleElement(this, key, value, Vertex.class);
    }

    // Internal configuration methods
    // --------------------------------------------------------------
    /**
     * Get the root graph {@link Vertex}.
     * 
     * <p>
     * TODO: Root vertex is queried very often and recommended to be cached.
     * </p>
     * 
     * @param type The type of the graph to get the configuration for
     * @return The configuration vertex of the versioned graph.
     * @throws IllegalStateException if root vertex could not be located.
     */
    public Vertex getRootVertex(VEProps.GRAPH_TYPE type) {
        Vertex rv;
        if (type == VEProps.GRAPH_TYPE.ACTIVE) {
            rv =
                    ElementUtils.getSingleElement(getBaseGraph(), VEProps.ROOT_GRAPH_VERTEX_ID,
                            VEProps.ACTIVE_ROOT_GRAPH_VERTEX_VALUE, Vertex.class);
        } else {
            rv =
                    ElementUtils.getSingleElement(getBaseGraph(), VEProps.ROOT_GRAPH_VERTEX_ID,
                            VEProps.HISTORIC_ROOT_GRAPH_VERTEX_VALUE, Vertex.class);
        }

        if (rv == null) {
            throw new IllegalStateException(String.format("Could not find %s graph root vertex", type.name()));
        }

        return utils.getNonEventableVertex(rv);
    }

    /**
     * Get the root vertex of the implemented graph.
     * 
     * @return The root vertex of the implemented graph.
     */
    protected abstract Vertex getRootVertex();

    // Graph identifier methods
    // --------------------------------------------------------------

    /**
     * Get the current latest historic graph version.
     * 
     * @return current latest graph version
     */
    public V getLatestGraphVersion() {
        return identifierBehavior.getLatestGraphVersion();
    }

    /**
     * Get the minimum possible graph version.
     * 
     * @see co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior#getMinPossibleGraphVersion()
     * @return The minimum possible graph version
     */
    public V getMinPossibleGraphVersion() {
        return identifierBehavior.getMinPossibleGraphVersion();
    }

    /**
     * Get the maximum possible graph version.
     * 
     * @see co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior#getMaxPossibleGraphVersion()
     * @return The maximum possible graph version
     */
    public V getMaxPossibleGraphVersion() {
        return identifierBehavior.getMaxPossibleGraphVersion();
    }


    // Elements keys methods
    // --------------------------------------------------------------

    /**
     * Whether or not graph is configured to maintain natural IDs.
     * 
     * @return true if graph is configured to maintain natural IDs.
     */
    public boolean isNaturalIds() {
        return (conf.getUseNaturalIds() || (conf.getUseNaturalIdsOnlyIfSuppliedIdsAreIgnored() && getBaseGraph()
                .getFeatures().ignoresSuppliedIds));
    }

    // Utils
    // --------------------------------------------------------------
    /**
     * Returns whether the specified property key is used internally for
     * versioned vertices or not.
     * 
     * @param key The property key to determine
     * @return true if property is for internal usage only
     */
    public static boolean isAntiquityKey(String key) {
        return VEProps.antiquityElementsKeys.contains(key);
    }

    /**
     * Return the key names of internal properties used by the {@code} antiquity
     * to version the {@link com.tinkerpop.blueprints.Graph} {@link Element}s.
     * 
     * @return An immutable set containing the internal property keys
     */
    public static Set<String> getAntiquityKeys() {
        return VEProps.antiquityElementsKeys;
    }
}
