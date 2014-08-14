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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;

import org.junit.Test;

public class NonTransactionalLongTypeVersionedGraphTest extends VersionedGraphTestSuite<Long> {
    @Override
    protected ActiveVersionedGraph<?, Long> generateGraph() {
        return generateGraph("graph");
    }

    protected ActiveVersionedGraph<?, Long> generateGraph(String graphDirectoryName) {
        return generateGraph(graphDirectoryName, null);
    }

    protected ActiveVersionedGraph<?, Long> generateGraph(String graphDirectoryName, Configuration conf) {
        // For natural IDs tests
        // Configuration conf1 = new
        // Configuration.ConfBuilder().useNaturalIds(true).build();
        return new ActiveVersionedGraph.ActiveVersionedNonTransactionalGraphBuilder<TinkerGraph, Long>(
                new TinkerGraph(), new LongGraphIdentifierBehavior()).init(true).conf(conf).build();
    }

    /**
     * Ensure that natural IDs are disabled. This is expected because graph conf
     * {@link Configuration#useNaturalIdsOnlyIfSuppliedIdsAreIgnored} is true by
     * default and the underline is tinker-graph and does not ignore supplied
     * keys
     */
    @Test
    public void testEnsureThatNativeIdsEnabled() {
        assertThat(graph.isNaturalIds(), is(Boolean.FALSE));
    }
}
