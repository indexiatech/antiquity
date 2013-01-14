package com.vertixtech.antiquity.graph;

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
	private Set<Vertex> removedVertices;
	private Set<Edge> addedEdges;
	private Set<Edge> removedEdges;
	private Map<Vertex, Map<String, Object>> modifiedPropsPerVertex;
	private Map<Edge, Map<String, Object>> modifiedPropsPerEdge;

	public Set<Vertex> getAddedVertices() {
		if (addedVertices == null)
			addedVertices = new HashSet<Vertex>();

		return addedVertices;
	}

	public void setAddedVertices(Set<Vertex> addedVertices) {
		this.addedVertices = addedVertices;
	}

	public Set<Vertex> getRemovedVertices() {
		if (removedVertices == null)
			removedVertices = new HashSet<Vertex>();

		return removedVertices;
	}

	public void setRemovedVertices(Set<Vertex> removedVertices) {
		this.removedVertices = removedVertices;
	}

	public Set<Edge> getAddedEdges() {
		if (addedEdges == null)
			addedEdges = new HashSet<Edge>();

		return addedEdges;
	}

	public void setAddedEdges(Set<Edge> addedEdges) {
		this.addedEdges = addedEdges;
	}

	public Set<Edge> getRemovedEdges() {
		if (removedEdges == null)
			removedEdges = new HashSet<Edge>();

		return removedEdges;
	}

	public void setRemovedEdges(Set<Edge> removedEdges) {
		this.removedEdges = removedEdges;
	}

	public Map<Vertex, Map<String, Object>> getModifiedPropsPerVertex() {
		if (modifiedPropsPerVertex == null)
			modifiedPropsPerVertex = new HashMap<Vertex, Map<String, Object>>();

		return modifiedPropsPerVertex;
	}

	public void setModifiedPropsPerVertex(Map<Vertex, Map<String, Object>> modifiedPropsPerVertex) {
		this.modifiedPropsPerVertex = modifiedPropsPerVertex;
	}

	public Map<Edge, Map<String, Object>> getModifiedPropsPerEdge() {
		if (modifiedPropsPerEdge == null)
			modifiedPropsPerEdge = new HashMap<Edge, Map<String, Object>>();

		return modifiedPropsPerEdge;
	}

	public void setModifiedPropsPerEdge(Map<Edge, Map<String, Object>> modifiedPropsPerEdge) {
		this.modifiedPropsPerEdge = modifiedPropsPerEdge;
	}

	public void clear() {
		getAddedVertices().clear();
		getRemovedVertices().clear();
		getAddedEdges().clear();
		getRemovedEdges().clear();
		getModifiedPropsPerVertex().clear();
		getModifiedPropsPerEdge().clear();
	}

	public String getDataAsString() {
		StringBuffer str = new StringBuffer("Vertices to add:\n");

		for (Vertex v : getAddedVertices())
			str.append("\t").append(v).append("\n");

		str.append("Vertices to remove:\n");
		for (Vertex v : getRemovedVertices())
			str.append("\t").append(v).append("\n");

		return str.toString();
	}
}
