/**
 * Copyright (c) 2012-2013 "Vertix Technologies, ltd."
 *
 * This file is part of Antiquity.
 *
 * Antiquity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.vertixtech.antiquity.graph;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventTrigger;
import com.vertixtech.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;

/**
 * A transactional implementation of {@link VersionedGraph}.
 * 
 * @see TransactionalGraph
 * @see VersionedGraph
 * @param <T>
 *            The type of the base graph, must support transactions.
 * @param <V>
 *            The version identifier type
 */
public class TransactionalVersionedGraph<T extends TransactionalGraph & IndexableGraph, V extends Comparable<V>> extends VersionedGraph<T, V> implements TransactionalGraph {
	Logger log = LoggerFactory.getLogger(TransactionalVersionedGraph.class);

	private final ThreadLocal<TransactionData> transactionData = new ThreadLocal<TransactionData>() {
		@Override
		protected TransactionData initialValue() {
			return new TransactionData();
		}
	};

	public TransactionalVersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
		super(baseGraph, identifierBehavior, null);
	}

	public TransactionalVersionedGraph(T baseGraph,
			GraphIdentifierBehavior<V> identifierBehavior,
			Configuration configuration) {
		super(baseGraph, identifierBehavior, configuration);
		this.trigger = new EventTrigger(this, true);
	}

	@Override
	public void vertexAdded(Vertex vertex) {
		log.debug("==Vertex [{}] added==", vertex);
		transactionData.get().getAddedVertices().add(vertex);
	}

	@Override
	public void vertexPropertyChanged(Vertex vertex, String key, Object oldValue, Object setValue) {
		log.debug("==Vertex [{}] property[{}] was modified [{} -> {}]==",
				vertex,
				key,
				oldValue,
				setValue);

		putEntryOnMap(transactionData.get().getModifiedPropsPerVertex(), transactionData.get()
				.getModifiedPropsPerVertex()
				.get(vertex), vertex, key, oldValue);
	}

	@Override
	public void vertexPropertyRemoved(Vertex vertex, String key, Object removedValue) {
		log.debug("==Vertex property [{}] was removed [{}->{}]==", vertex, removedValue);

		putEntryOnMap(transactionData.get().getModifiedPropsPerVertex(), transactionData.get()
				.getModifiedPropsPerVertex()
				.get(vertex), vertex, key, removedValue);
	}

	@Override
	public void vertexRemoved(Vertex vertex) {
		log.debug("==Vertex [{}] removed==", vertex);
		transactionData.get().getRemovedVertices().add(vertex);
	}

	@Override
	public void edgeAdded(Edge edge) {
		log.debug("==Edge [{}] added==", edge);
		transactionData.get().getAddedEdges().add(edge);
	}

	@Override
	public void edgePropertyChanged(Edge edge, String key, Object oldValue, Object setValue) {
		// Currently modified edges values are not versioned
	}

	@Override
	public void edgePropertyRemoved(Edge edge, String key, Object removedValue) {
		// Currently modified edges values are not versioned
	}

	@Override
	public void edgeRemoved(Edge edge) {
		transactionData.get().getRemovedEdges().add(edge);
	}

	/**
	 * Handles the {@link TransactionData} associated with this graph.
	 * 
	 * @see TransactionData
	 * @param nextVersion
	 *            The next version of the transaction to be committed.
	 */
	private void handleTransactionData(V nextVersion) {
		versionAddedVertices(nextVersion, transactionData.get().getAddedVertices());
		versionRemovedVertices(nextVersion, getLatestGraphVersion(), transactionData.get()
				.getRemovedVertices());
		versionAddedEdges(nextVersion, transactionData.get().getAddedEdges());
		versionRemovedEdges(nextVersion, getLatestGraphVersion(), transactionData.get().getRemovedEdges());

		for (Map.Entry<Vertex, Map<String, Object>> oldPropsPerVertex : transactionData.get()
				.getModifiedPropsPerVertex()
				.entrySet()) {

			// if vertex is new then skip version the modification as it'll create an extra unneeded historical version
			if (!transactionData.get().getAddedVertices().contains(oldPropsPerVertex.getKey())) {
				versionModifiedVertex(getLatestGraphVersion(),
						nextVersion,
						(VersionedVertex) oldPropsPerVertex.getKey(),
						oldPropsPerVertex.getValue());
			}
		}
	}

	@Override
	@Deprecated
	public void stopTransaction(Conclusion conclusion) {
		throw new RuntimeException("This method is not supported, please use commit() / rollback() instead");
	}

	/**
	 * A commit only fires the event queue on successful operation. If the commit operation to the underlying graph
	 * fails, the event queue will not fire and the queue will not be reset.
	 */
	@Override
	public void commit() {
		boolean transactionFailure = false;
		V transactionVer = getNextGraphVersion(false);
		try {
			trigger.fireEventQueue();
			log.debug("Comitting transaction[{}]", transactionVer);
			handleTransactionData(transactionVer);
			trigger.resetEventQueue();
			transactionData.get().clear();
			this.baseGraph.commit();
		} catch (RuntimeException re) {
			transactionFailure = true;
			log.error("Failed to commit transaction[{}]", transactionVer);
			throw re;
		} finally {
			if (!transactionFailure) {
				log.debug("Transaction[{}] successfully committed.", transactionVer);
				allocateNextGraphVersion(transactionVer);
			}
			// TODO: Unlock the transaction version allocation
		}
	}

	/**
	 * A rollback only resets the event queue on successful operation. If the rollback operation to the underlying graph
	 * fails, the event queue will not be reset.
	 */
	@Override
	public void rollback() {
		boolean transactionFailure = false;
		try {
			this.baseGraph.rollback();
		} catch (RuntimeException re) {
			transactionFailure = true;
			throw re;
		} finally {
			if (!transactionFailure) {
				trigger.resetEventQueue();
				transactionData.get().clear();
			}
		}
	}

	/**
	 * <p>
	 * Put the specified property key/value in the specified map.
	 * </p>
	 * 
	 * <p>
	 * If map is null a new map will be created
	 * </p>
	 * 
	 * @param map
	 *            A map of properties to hold the specified key/value property
	 * @param key
	 *            The key of the property to store within the specified map
	 * @param value
	 *            The value of the property to store within the specified map
	 * @return The specified map after it contains the specified key/value
	 */
	private Map<String, Object> putEntryOnMap(Map<Vertex, Map<String, Object>> verticesMaps,
			Map<String, Object> map, Vertex vertex,
			String key,
			Object value) {

		if (map == null) {
			map = new HashMap<String, Object>();
			verticesMaps.put(vertex, map);
		}

		map.put(key, value);
		return map;
	}
}
