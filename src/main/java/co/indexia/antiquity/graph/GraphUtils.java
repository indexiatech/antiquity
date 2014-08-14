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

import java.util.ArrayList;
import java.util.List;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * General read-only utils for {@link com.tinkerpop.blueprints.Graph}s
 */
public class GraphUtils {
    public static GraphUtils INSTANCE = new GraphUtils();

    private GraphUtils() {

    }

    /**
     * Get a list of IDs for the specified vertices iterable.
     * 
     * @param vertices The vertices
     * @return A list of IDs.
     */
    public static List<Object> getVerticesIds(Iterable<Vertex> vertices) {
        List<Object> ids = new ArrayList<Object>();

        for (Vertex e : vertices) {
            ids.add(e.getId());
        }

        return ids;
    }

    /**
     * Get graph vertices as a string
     * 
     * @param graph the graph
     * @param type graph type to print vertices for
     * @param withInternals if true internal elements will be printed as well.
     * @return A string of vertices data.
     */
    public static String getGraphVerticesAsString(ActiveVersionedGraph graph, VEProps.GRAPH_TYPE type,
            boolean withInternals) {
        Iterable<Vertex> vertices;

        if (type == VEProps.GRAPH_TYPE.ACTIVE) {
            vertices = graph.getVertices();
        } else {
            vertices = graph.getHistoricGraph().getVertices();
        }

        return getVerticesAsString(vertices, withInternals);
    }

    public static String getVerticesAsString(Iterable<Vertex> vertices, boolean withInternals) {
        StringBuilder graphStr = new StringBuilder();
        VersionedElementUtils utils = new VersionedElementUtils();

        for (Vertex v : vertices) {
            if (!withInternals && utils.isInternal(v)) {
                continue;
            }

            graphStr.append("Vertex [").append(v.getId()).append("]\n");
            graphStr.append("\tProps:\n");

            Vertex vr = v;
            if (withInternals && v instanceof HistoricVersionedVertex) {
                vr = ((HistoricVersionedVertex) v).getBaseElement();
            }

            for (String key : vr.getPropertyKeys()) {
                graphStr.append("\t\tProp [").append(key).append("] with value [").append(v.getProperty(key))
                        .append("]\n");
            }

            graphStr.append("\n\tEdges:\n");

            Iterable<Edge> edges;
            if (v instanceof HistoricVersionedVertex) {
                edges = ((HistoricVersionedVertex) v).getEdges(Direction.BOTH, withInternals);
            } else {
                edges = v.getEdges(Direction.BOTH);
            }

            for (Edge e : edges) {
                graphStr.append("\t\tEdge [").append(e.getLabel()).append("]\n");
            }

            graphStr.append("\n");
        }

        return graphStr.toString();
    }
}
