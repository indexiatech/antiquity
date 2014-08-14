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

import com.tinkerpop.blueprints.Features;

/**
 * Configuration provides a list of properties which defines the behavior of the
 * associated {@link ActiveVersionedGraph}.
 */
public class Configuration {
    /**
     * If true a private hash is calculated per vertex creation / modification
     */
    public final Boolean privateVertexHashEnabled;

    /**
     * If true natural IDs will be used instead of internal graph IDs, This is
     * useful since some of the graphs ignores supplied IDs.
     * 
     * @see Features#ignoresSuppliedIds
     */
    public final Boolean useNaturalIds;

    /**
     * If true, natural IDs will be used only if the underline graph ignores
     * supplied IDs.
     */
    public final Boolean useNaturalIdsOnlyIfSuppliedIdsAreIgnored;

    /**
     * If true an empty transaction won't be versioned.
     * 
     * This is only relevant to transactional graphs
     */
    public final Boolean doNotVersionEmptyTransactions;

    /**
     * Create an instance of this class with the specified builder.
     * 
     * @param builder The builder to build the configuration with.
     */
    private Configuration(ConfBuilder builder) {
        this.privateVertexHashEnabled = builder.privateVertexHashEnabled;
        this.useNaturalIds = builder.useNaturalIds;
        this.useNaturalIdsOnlyIfSuppliedIdsAreIgnored = builder.useNaturalIdsOnlyIfSuppliedIdsAreIgnored;
        this.doNotVersionEmptyTransactions = builder.doNotVersionEmptyTransactions;
    }

    /**
     * Private vertex hash calculation.
     * 
     * The hash is calculated only for the vertex properties without taking its
     * edges into account.
     * 
     * @return true if private hash should be calculated for vertices
     */
    public Boolean getPrivateVertexHashEnabled() {
        return privateVertexHashEnabled;
    }

    /**
     * Whether or not graph maintains natural IDs
     * 
     * @return true if graph maintains natural IDs
     */
    public Boolean getUseNaturalIds() {
        return useNaturalIds;
    }

    /**
     * Whether or nto graph maintains natural IDs in case underline graph
     * ignores supplied IDs.
     * 
     * @return true if graph maintains natural IDs in case underline graph
     *         ignores supplied IDs.
     */
    public Boolean getUseNaturalIdsOnlyIfSuppliedIdsAreIgnored() {
        return useNaturalIdsOnlyIfSuppliedIdsAreIgnored;
    }

    /**
     * Skip versioning of empty transactions.
     * 
     * @return true if empty transactions should not be versioned.
     */
    public Boolean getDoNotVersionEmptyTransactions() {
        return doNotVersionEmptyTransactions;
    }

    /**
     * Configuration Builder.
     */
    public static class ConfBuilder {
        private Boolean privateVertexHashEnabled = true;
        private Boolean useNaturalIds = false;
        private Boolean useNaturalIdsOnlyIfSuppliedIdsAreIgnored = true;
        private Boolean doNotVersionEmptyTransactions = true;

        public ConfBuilder privateVertexHashEnabled(Boolean privateVertexHashEnabled) {
            this.privateVertexHashEnabled = privateVertexHashEnabled;
            return this;
        }

        public ConfBuilder useNaturalIds(Boolean useNaturalIds) {
            this.useNaturalIds = useNaturalIds;
            return this;
        }

        public ConfBuilder useNaturalIdsOnlyIfSuppliedIdsAreIgnored(Boolean useNaturalIdsOnlyIfSuppliedIdsAreIgnored) {
            this.useNaturalIdsOnlyIfSuppliedIdsAreIgnored = useNaturalIdsOnlyIfSuppliedIdsAreIgnored;
            return this;
        }

        public ConfBuilder doNotVersionEmptyTransactions(Boolean doNotVersionEmptyTransactions) {
            this.doNotVersionEmptyTransactions = doNotVersionEmptyTransactions;
            return this;
        }

        public Configuration build() {
            return new Configuration(this);
        }
    }
}
