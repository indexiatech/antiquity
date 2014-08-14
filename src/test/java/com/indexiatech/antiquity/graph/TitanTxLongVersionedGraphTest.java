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

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import co.indexia.antiquity.graph.identifierBehavior.LongGraphIdentifierBehavior;
import org.apache.commons.configuration.BaseConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Titan tests
 */
public class TitanTxLongVersionedGraphTest extends TransactionalLongVersionedGraphTest {
    @Override
    protected ActiveVersionedGraph<?, Long> generateGraph() {
        File f = new File("/tmp/testgraph");
        if (f.exists()) {
            if (f.isDirectory()) {
                try {
                    FileUtils.deleteDirectory(f);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            } else {
                f.delete();
            }

        }

        Configuration c = new BaseConfiguration();
        c.addProperty("storage.directory","/tmp/testgraph");
        TitanGraph g = TitanFactory.open(c);

        return new ActiveVersionedGraph.ActiveVersionedTransactionalGraphBuilder<TitanGraph, Long>(g, new LongGraphIdentifierBehavior())
                .init(true).conf(null).build();
    }
}
