package com.vertixtech.antiquity.graph;

import java.util.ArrayList;
import java.util.List;

import com.tinkerpop.blueprints.Vertex;

public class GraphUtils {
    private GraphUtils() {

    }

    public static List<Object> getVertexIds(Iterable<Vertex> vertices) {
        List<Object> ids = new ArrayList<Object>();

        for (Vertex e : vertices) {
            ids.add(e.getId());
        }

        return ids;
    }
}
