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
