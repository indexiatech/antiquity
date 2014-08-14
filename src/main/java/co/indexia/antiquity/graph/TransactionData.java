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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * A storage class for changes that occur within a transaction.
 */
public class TransactionData {
    private Set<Vertex> addedVertices;
    private Map<Vertex, Map<String, Object>> removedVertices;
    private Set<Edge> addedEdges;
    private Map<Edge, Map<String, Object>> removedEdges;
    private Map<Vertex, Map<String, Object>> modifiedPropsPerVertex;
    private Map<Edge, Map<String, Object>> modifiedPropsPerEdge;

    /**
     * Get the added vertices.
     * 
     * @return A set of added vertices.
     */
    public Set<Vertex> getAddedVertices() {
        if (addedVertices == null) {
            addedVertices = new HashSet<Vertex>();
        }

        return addedVertices;
    }

    /**
     * Get The removed vertices.
     * 
     * @return A set of removed vertices.
     */
    public Map<Vertex, Map<String, Object>> getRemovedVertices() {
        if (removedVertices == null) {
            removedVertices = new HashMap<Vertex, Map<String, Object>>();
        }

        return removedVertices;
    }

    /**
     * Get the added edges.
     * 
     * @return A set of added edges
     */
    public Set<Edge> getAddedEdges() {
        if (addedEdges == null) {
            addedEdges = new HashSet<Edge>();
        }

        return addedEdges;
    }

    /**
     * Get the modified properties per vertex.
     * 
     * @return A map contains the modified properties per vertex.
     */
    public Map<Vertex, Map<String, Object>> getModifiedPropsPerVertex() {
        if (modifiedPropsPerVertex == null) {
            modifiedPropsPerVertex = new HashMap<Vertex, Map<String, Object>>();
        }

        return modifiedPropsPerVertex;
    }

    /**
     * Get the removed edges
     * 
     * @return A set of removed edges
     */
    public Map<Edge, Map<String, Object>> getRemovedEdges() {
        if (removedEdges == null) {
            removedEdges = new HashMap<Edge, Map<String, Object>>();
        }

        return removedEdges;
    }

    /**
     * GEt the modified properties per edge
     * 
     * @return A map contains the modified properties per edge.
     */
    public Map<Edge, Map<String, Object>> getModifiedPropsPerEdge() {
        if (modifiedPropsPerEdge == null) {
            modifiedPropsPerEdge = new HashMap<Edge, Map<String, Object>>();
        }

        return modifiedPropsPerEdge;
    }

    /**
     * <p>
     * Clear the data of this class.
     * </p>
     * <p>
     * This method mainly invokes {@link Collection#clear()} on all collections.
     * </p>
     */
    public void clear() {
        getAddedVertices().clear();
        getRemovedVertices().clear();
        getAddedEdges().clear();
        getRemovedEdges().clear();
        getModifiedPropsPerVertex().clear();
        getModifiedPropsPerEdge().clear();
    }

    /**
     * Get the data that this instance contains as a string.
     * 
     * @return A string contains all data of this instance
     */
    public String getDataAsString() {
        StringBuilder str = new StringBuilder("Vertices to add:\n");

        for (Vertex v : getAddedVertices()) {
            str.append("\t").append(v).append("\n");
        }

        str.append("Vertices to remove:\n");
        for (Vertex v : getRemovedVertices().keySet()) {
            str.append("\t").append(v).append("\n");
        }

        return str.toString();
    }

    /**
     * Check whether this instance is empty of any data.
     * 
     * @return true if this instance has no data.
     */
    public boolean isEmpty() {
        return getAddedVertices().isEmpty() && getRemovedVertices().isEmpty() && getAddedEdges().isEmpty()
                && getRemovedEdges().isEmpty() && getModifiedPropsPerVertex().isEmpty()
                && getModifiedPropsPerEdge().isEmpty();
    }
}
