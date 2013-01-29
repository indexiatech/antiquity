/**
 * Copyright (c) 2012-2013 "Indexia Technologies, ltd."
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
package co.indexia.antiquity.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Index;
import com.tinkerpop.blueprints.IndexableGraph;
import com.tinkerpop.blueprints.TransactionalGraph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.wrappers.event.EventEdge;
import com.tinkerpop.blueprints.util.wrappers.event.EventElement;
import com.tinkerpop.blueprints.util.wrappers.event.EventGraph;
import com.tinkerpop.blueprints.util.wrappers.event.EventVertex;
import com.tinkerpop.blueprints.util.wrappers.event.listener.GraphChangedListener;
import co.indexia.antiquity.graph.identifierBehavior.GraphIdentifierBehavior;
import co.indexia.antiquity.range.Range;
import co.indexia.antiquity.utils.ExceptionFactory;

/**
 * Versioned graph implementation,
 * 
 * <p>
 * The underline graph must support transactions.
 * </p>
 * 
 * @see Graph
 * @see TransactionalGraph
 */
public abstract class VersionedGraph<T extends IndexableGraph, V extends Comparable<V>> extends EventGraph<T> implements GraphChangedListener {
	Logger log = LoggerFactory.getLogger(VersionedGraph.class);

	private final static Set<String> versionedVertexInternalProperties;

	// Versioned Vertex internal properties
	// --------------------------------------------------------------
	/**
	 * A marker property key which indicates that the {@link Element} is removed.
	 */
	public static final String REMOVED_PROP_KEY = "__REMOVED__";
	/**
	 * A property key which holds the minimum valid version of this element.
	 */
	public static final String VALID_MIN_VERSION_PROP_KEY = "__VALID_MIN_VERSION__";
	/**
	 * A property key which holds the maximum valid version of this element.
	 */
	public static final String VALID_MAX_VERSION_PROP_KEY = "__VALID_MAX_VERSION__";

	/**
	 * An element property key which indicates whether the element is for historical purposes or not.
	 * 
	 * Historical elements are elements which were created for audit purposes and are not the active/alive data.
	 */
	public static final String HISTORIC_ELEMENT_PROP_KEY = "__HISTORIC__";

	/**
	 * The key name of the private hash calculation.
	 */
	public static final String PRIVATE_HASH_PROP_KEY = "__PRIVATE_HASH__";

	/**
	 * The key name of the natural identifier of a vertex.
	 */
	public static final String VERTEX_ID_PROP_KEY = "__ID__";

	// General Internal Properties
	// --------------------------------------------------------------
	/**
	 * The identifier of the graph configuration vertex
	 */
	public static final String GRAPH_CONF_VERTEX_ID = "VERSIONED_GRAPH_CONF_VERTEX";

	/**
	 * The name of the index which contains the vertex identifiers.
	 */
	private static final String GRAPH_VERTEX_IDENTIFIERS_INDEX_NAME = "IDENTIFIER_IDX";

	/**
	 * The property key which stores the last graph version
	 */
	public static final String LATEST_GRAPH_VERSION_PROP_KEY = "__LATEST_GRAPH_VERSION__";

	/**
	 * The label name of the edge which creates the chain of a vertex revisions
	 */
	public static final String PREV_VERSION_CHAIN_EDGE_TYPE = "__PREV_VERSION__";

	/**
	 * The identifier behavior associated with this graph
	 */
	protected final GraphIdentifierBehavior<V> identifierBehavior;

	static {
		versionedVertexInternalProperties =
				ImmutableSet.of(REMOVED_PROP_KEY,
						VALID_MIN_VERSION_PROP_KEY,
						VALID_MAX_VERSION_PROP_KEY,
						HISTORIC_ELEMENT_PROP_KEY,
						PRIVATE_HASH_PROP_KEY);
	}

	/**
	 * The graph configuration
	 */
	protected final Configuration conf;

	/**
	 * Create an instance of this class.
	 * 
	 * @param baseGraph
	 *            The base grap to wrap with versioning support.
	 * @param identifierBehavior
	 *            The graph identifier behavior implementation.
	 */
	public VersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior) {
		this(baseGraph, identifierBehavior, null);
	}

	/**
	 * Create an instance of {@link VersionedGraph} with the specified underline {@link Graph}.
	 * 
	 * @param baseGraph
	 *            The underline base graph
	 * @param identifierBehavior
	 *            The graph identifier behavior implementation
	 * @param conf
	 *            The configuration instance of this instance.
	 */
	public VersionedGraph(T baseGraph, GraphIdentifierBehavior<V> identifierBehavior, Configuration conf) {
		super(baseGraph);
		addListener(this);
		this.identifierBehavior = identifierBehavior;
		identifierBehavior.setGraph(this);
		if (conf == null)
			this.conf = new Configuration();
		else
			this.conf = conf;

		// TODO: A better approach to do that
		// Create the conf vertex if it does not exist
		if (getVersionConfVertex() == null) {
			Vertex v = baseGraph.addVertex(GRAPH_CONF_VERTEX_ID);
			v.setProperty(GRAPH_CONF_VERTEX_ID, "GRAPH_CONF_VERTEX_ID");
			if (baseGraph instanceof TransactionalGraph)
				((TransactionalGraph) baseGraph).commit();
		}
	}

	// Operation Overrides
	// --------------------------------------------------------------
	@Override
	public Vertex addVertex(final Object id) {
		final Vertex vertex = this.baseGraph.addVertex(id);
		if (vertex == null) {
			return null;
		} else {
			VersionedVertex vv = new VersionedVertex<V>(vertex,
					this.graphChangedListeners,
					this.trigger,
					this,
					getLatestGraphVersion());
			vv.setTrans(true);
			this.onVertexAdded(vv);

			return vv;
		}
	}

	@Override
	public Vertex getVertex(final Object id) {
		return getVertex(id, getLatestGraphVersion());
	}

	@Override
	public Iterable<Vertex> getVertices() {
		return getVertices(getLatestGraphVersion());
	}

	@Override
	public Iterable<Vertex> getVertices(final String key, final Object value) {
		return getVertices(key, value, getLatestGraphVersion());
	}

	@Override
	public Iterable<Edge> getEdges() {
		return getEdges(getLatestGraphVersion());
	}

	@Override
	public Iterable<Edge> getEdges(final String key, final Object value) {
		return getEdges(key, value, getLatestGraphVersion());
	}

	@Override
	public void removeVertex(final Vertex vertex) {
		raiseExceptionIfNotVersionedElement(vertex);

		this.onVertexRemoved(vertex);
	}

	@Override
	public void removeEdge(final Edge edge) {
		raiseExceptionIfNotVersionedElement(edge);

		this.onEdgeRemoved(edge);
	}

	// Enhanced methods for the standard blueprint graph API
	// ------------------------------------------------------
	/**
	 * <p>
	 * Get a vertex by id for the specified version.
	 * </p>
	 * 
	 * <p>
	 * A vertex revision must be available for the specified version, otherwise a null will be returned.
	 * </p>
	 * 
	 * @param id
	 *            The unique id of the vertex
	 * @param version
	 *            The version to get the vertex for
	 * @return The found vertex, or null if not found
	 */
	public Vertex getVertex(final Object id, V version) {
		final Vertex vertex = this.baseGraph.getVertex(id);
		if (vertex == null) {
			return null;
		} else {
			if (isHistoricalOrInternal(vertex)) {
				return vertex;
			} else {
				return new VersionedVertex<V>(vertex, this.graphChangedListeners, this.trigger, this, version);
			}
		}
	}

	/**
	 * Get all vertices for the specified version.
	 * 
	 * @param version
	 *            The version to get the vertices for
	 * @return An {@link Iterable} of the found vertices for the specified version.
	 */
	public Iterable<Vertex> getVertices(V version) {
		return new VersionedVertexIterable<V>(this.baseGraph.getVertices(),
				this.graphChangedListeners,
				this.trigger,
				this,
				version);
	}

	/**
	 * Return an iterable to all the vertices in the graph that have a particular key/value property for the specified
	 * version.
	 * 
	 * @param key
	 *            The key of the property to filter vertices by
	 * @param value
	 *            The value of the property to filter vertices by
	 * @param version
	 *            version The version to get the vertices for
	 * @return An {@link Iterable} of the found vertices for the specified criteria.
	 */
	public Iterable<Vertex> getVertices(final String key, final Object value, V version) {
		return new VersionedVertexIterable<V>(this.baseGraph.getVertices(key, value),
				this.graphChangedListeners,
				this.trigger,
				this,
				version);
	}

	/**
	 * Return an iterable to all the edges in the graph for the specified version
	 * 
	 * @param version
	 *            The version to get the edges for
	 * @return An {@link Iterable} of the found edges for the specified version.
	 */
	public Iterable<Edge> getEdges(V version) {
		return new VersionedEdgeIterable<V>(this.baseGraph.getEdges(),
				this.graphChangedListeners,
				this.trigger,
				this,
				version, false);
	}

	/**
	 * Return an iterable to all the edges in the graph that have a particular key/value property for the specified
	 * version.
	 * 
	 * @param key
	 *            The key of the property to filter edges by
	 * @param value
	 *            The value of the property to filter edges by
	 * @param version
	 *            The version to get the edges for
	 * @return An {@link Iterable} of the found edges for the specified criteria
	 */
	public Iterable<Edge> getEdges(final String key, final Object value, V version) {
		return new VersionedEdgeIterable<V>(this.baseGraph.getEdges(key, value),
				this.graphChangedListeners,
				this.trigger,
				this,
				version, false);
	}

	// Get/Set Version Methods
	// --------------------------------------------------------------
	/**
	 * Set a version range of the specified element.
	 * 
	 * @param versionedElement
	 *            The element to set the version range.
	 * @param range
	 *            The version {@link Range}
	 */
	public void setVersion(Element versionedElement, Range<V> range)
	{
		setStartVersion(versionedElement, range.min());
		setEndVersion(versionedElement, range.max());
	}

	/**
	 * Set the start version range of the specified element.
	 * 
	 * @param versionedElement
	 *            The element to set the start version range.
	 * @param startVersion
	 *            The start version to set
	 */
	public void setStartVersion(Element versionedElement, V startVersion)
	{
		setVersion(StartOrEnd.START, versionedElement, startVersion);
	}

	/**
	 * Set the end version range of the specified element.
	 * 
	 * @param versionedElement
	 *            The element to set the end version range.
	 * @param endVersion
	 *            The end version to set
	 */
	public void setEndVersion(Element versionedElement, V endVersion) {
		setVersion(StartOrEnd.END, versionedElement, endVersion);
	}

	public void setPrivateHash(Vertex vertex) {
		String newHash = ElementUtils.calculateElementPrivateHash(vertex, getInternalProperties());

		if (vertex instanceof VersionedVertex) {
			VersionedVertex<V> versionedVertex = ((VersionedVertex<V>) vertex);

			String oldHash =
					(String) versionedVertex.getBaseVertex()
							.getProperty(VersionedGraph.PRIVATE_HASH_PROP_KEY);

			if (Strings.isNullOrEmpty(oldHash) || (!oldHash.equals(newHash))) {
				((VersionedVertex<V>) vertex).getBaseVertex()
						.setProperty(VersionedGraph.PRIVATE_HASH_PROP_KEY, newHash);
			} else {
				log.debug("Calculated hash of vertex[{}] is equal to the existing hash, nothing to set.");
			}

		} else {
			vertex.setProperty(VersionedGraph.PRIVATE_HASH_PROP_KEY, newHash);
		}
	}

	/**
	 * Get the private hash of the specified vertex, return null if no private hash is set.
	 * 
	 * @param vertex
	 *            The vertex instance to get the private hash for
	 * @return The private hash as a string
	 * @throws IllegalArgumentException
	 *             If the specified vertex is not a {@link VersionedVertex}.
	 */
	public String getPrivateHash(Vertex vertex) {
		if (vertex instanceof VersionedVertex) {
			Object hash = vertex.getProperty(VersionedGraph.PRIVATE_HASH_PROP_KEY);
			return hash != null ? (String) hash : null;
		}
		else {
			throw new IllegalArgumentException("The specified vertex is not a versioned graph.");
		}
	}

	/**
	 * An enum which indicates the start or the end edges of a range.
	 */
	enum StartOrEnd {
		START,
		END
	}

	/**
	 * Set the start or end version of the element
	 * 
	 * @param startOrEnd
	 *            Whether to set the start or the end of the version range.
	 * @param versionedElement
	 *            The graph {@link Element} to set the version for
	 * @param version
	 *            The version to set
	 */
	public void setVersion(StartOrEnd startOrEnd, Element versionedElement, V version) {
		// TODO: A more appropriate way to handle element types
		Element e = getNonEventElement(versionedElement);

		if (startOrEnd == StartOrEnd.START)
			e.setProperty(VALID_MIN_VERSION_PROP_KEY, version);
		else
			e.setProperty(VALID_MAX_VERSION_PROP_KEY, version);
	}

	/**
	 * Get the start version of the specified element
	 * 
	 * @param versionedElement
	 *            The element to get the start version.
	 * @return The start version of the specified element.
	 */
	@SuppressWarnings("unchecked")
	public V getStartVersion(Element versionedElement)
	{
		return (V) getNonEventElement(versionedElement).getProperty(VALID_MIN_VERSION_PROP_KEY);
	}

	/**
	 * Get the end version of the specified element
	 * 
	 * @param versionedElement
	 *            The element to get the end version.
	 * @return The end version of the specified element.
	 */
	@SuppressWarnings("unchecked")
	public V getEndVersion(Element versionedElement)
	{
		return (V) getNonEventElement(versionedElement).getProperty(VALID_MAX_VERSION_PROP_KEY);
	}

	/**
	 * Get the version range of the specified element.
	 * 
	 * @param versionedElement
	 *            The element to get the version range for.
	 * @return a {@link Range} of version of the specified element.
	 */
	public Range<V> getVersionRange(Element versionedElement) {
		return Range.range(getStartVersion(versionedElement), getEndVersion(versionedElement));
	}

	/**
	 * Determine whether the specified version is the start version of the specified element.
	 * 
	 * @param version
	 *            The version to determine as the start of the version range.
	 * @param versionedElement
	 *            The element to check
	 * @return true if the specified version is the start version of the specified element.
	 */
	public boolean isStartVersion(V version, Element versionedElement) {
		return version.equals(getStartVersion(versionedElement));
	}

	/**
	 * Determine whether the specified version is the end version of the specified element.
	 * 
	 * @param version
	 *            The version to determine as the end of the version range.
	 * @param versionedElement
	 *            The element to check
	 * @return true if the specified version is the end version of the specified element.
	 */
	public boolean isEndVersion(V version, Element versionedElement) {
		return version.equals(getEndVersion(versionedElement));
	}

	// Graph Version Identifier Methods
	// --------------------------------------------------------------
	public V getLatestGraphVersion() {
		return identifierBehavior.getLatestGraphVersion();
	}

	/**
	 * Get the maximum possible graph version.
	 * 
	 * @see GraphIdentifierBehavior#getMaxPossibleGraphVersion()
	 * @return The maximum possible graph version
	 */
	public V getMaxPossibleGraphVersion() {
		return identifierBehavior.getMaxPossibleGraphVersion();
	}

	/**
	 * Get the next graph version.
	 * 
	 * @see #allocateNextGraphVersion(Comparable)
	 * @param allocate
	 *            Whether to allocate the next version or not.
	 * @return The next version of the graph
	 */
	protected V getNextGraphVersion(boolean allocate) {
		V nextGraphVersion = identifierBehavior.getNextGraphVersion(getLatestGraphVersion());
		if (allocate)
			allocateNextGraphVersion(nextGraphVersion);

		// TODO: Unlock the configuration vertex if allocate=false, otherwise unlock should occur during transaction
		// commit
		return nextGraphVersion;
	}

	/**
	 * Allocate (persist) the specified next version in the graph in the configuration vertex.
	 * 
	 * @param nextVersion
	 *            The next version to allocate.
	 */
	protected void allocateNextGraphVersion(V nextVersion) {
		// TODO: Unlock the configuration vertex
		getVersionConfVertex().setProperty(LATEST_GRAPH_VERSION_PROP_KEY, nextVersion);
	}

	/**
	 * Get the version configuration {@link Vertex}.
	 * 
	 * <p>
	 * Configuration vertex is queried very often and recommended to be cached.
	 * </p>
	 * 
	 * @return The configuration vertex of the versioned graph.
	 */
	public Vertex getVersionConfVertex() {
		Vertex v = getBaseGraph().getVertex(GRAPH_CONF_VERTEX_ID);

		if (v == null) {
			Iterable<Vertex> vs = getBaseGraph().getVertices(GRAPH_CONF_VERTEX_ID, "GRAPH_CONF_VERTEX_ID");

			if (vs.iterator().hasNext()) {
				v = vs.iterator().next();
				return v;
			}
			else {
				return null;
			}
		} else {
			return v;
		}

		// if (v == null)
		// throw new RuntimeException("Could not find the graph configuration vertex");
	}

	// Indices
	// --------------------------------------------------------------
	public Index<Vertex> getVertexIdentifiersIndex() {
		return getOrCreateIndex(GRAPH_VERTEX_IDENTIFIERS_INDEX_NAME, Vertex.class);
	}

	private <E extends Element> Index<E> getOrCreateIndex(String indexName, Class<E> clazz) {
		Index<E> idx = getBaseGraph().getIndex(indexName, clazz);

		if (idx == null)
			idx = getBaseGraph().createIndex(indexName, clazz);

		return idx;
	}

	// Methods used by events responses
	// --------------------------------------------------------------
	/**
	 * Version vertices in the graph.
	 * 
	 * @param version
	 *            The graph version that created the specified vertices
	 * @param vertices
	 *            The vertices to be versioned.
	 */
	protected void versionAddedVertices(V version, Iterable<Vertex> vertices) {
		Range<V> range = Range.range(version, identifierBehavior.getMaxPossibleGraphVersion());

		for (Vertex v : vertices) {
			getNonEventElement(v).setProperty(HISTORIC_ELEMENT_PROP_KEY, false);
			setVersion(v, range);
			VersionedVertex<V> vv = ((VersionedVertex<V>) v);
			vv.setForVersion(version);
			vv.setTrans(false);
			// for non transactional graphs, set the _ID
			if (v.getProperty(VERTEX_ID_PROP_KEY) == null && (!(this instanceof TransactionalGraph))) {
				vv.getBaseVertex().setProperty(VERTEX_ID_PROP_KEY, v.getId());
			}

			if (conf.getAutoIndexVertexIdProperty()) {
				if (vv.getBaseVertex().getProperty(VERTEX_ID_PROP_KEY) != null) {
					getVertexIdentifiersIndex().put(VERTEX_ID_PROP_KEY,
							vv.getBaseVertex().getProperty(VERTEX_ID_PROP_KEY),
							vv.getBaseVertex());
				} else {
					throw new IllegalStateException(String.format("Vertex [%s] does not contain property %s and auto _ID index is enabled"
							,
							v,
							VERTEX_ID_PROP_KEY));
				}
			}
			if (conf.getPrivateVertexHashEnabled()) {
				setPrivateHash(v);
			}
		}
	}

	/**
	 * Version edges in the graph.
	 * 
	 * @param version
	 *            The graph version that created the specified edges
	 * @param edges
	 *            The edges to be versioned.
	 */
	protected void versionAddedEdges(V version, Iterable<Edge> edges) {
		Range<V> range = Range.range(version, identifierBehavior.getMaxPossibleGraphVersion());

		for (Edge e : edges) {
			setVersion(e, range);
		}
	}

	protected void versionRemovedVertices(V nextVer, V maxVer, Iterable<Vertex> vertices) {
		for (Vertex v : vertices) {
			VersionedVertex<V> vv = (VersionedVertex<V>) v;
			vv.setForVersion(maxVer);

			// Remove vertex edges
			versionRemovedEdges(nextVer, maxVer, v.getEdges(Direction.BOTH));

			getNonEventElement(v).setProperty(REMOVED_PROP_KEY, nextVer);
			getNonEventElement(v).setProperty(VALID_MAX_VERSION_PROP_KEY, maxVer);

			if (conf.getAutoIndexVertexIdProperty() && v.getProperty(VERTEX_ID_PROP_KEY) != null) {
				Iterable<Vertex> it =
						getVertexIdentifiersIndex().get(VERTEX_ID_PROP_KEY, v.getProperty(VERTEX_ID_PROP_KEY));
				if (it.iterator().hasNext()) {
					Vertex vertexInIdx = it.iterator().next();
					getVertexIdentifiersIndex().remove(VERTEX_ID_PROP_KEY,
							v.getProperty(VERTEX_ID_PROP_KEY),
							vertexInIdx);
				}
			}
		}
	}

	protected void versionRemovedEdges(V nextVer, V maxVer, Iterable<Edge> edges) {
		for (Edge e : edges) {
			getNonEventElement(e).setProperty(REMOVED_PROP_KEY, nextVer);
			getNonEventElement(e).setProperty(VALID_MAX_VERSION_PROP_KEY, maxVer);
		}
	}

	/**
	 * Create a historical vertex which contains the modified vertex content before it was modified.
	 * 
	 * TODO return vertex should be immutable.
	 * 
	 * @param modifiedVertex
	 *            The modified {@link VersionedVertex}.
	 * @param oldValues
	 *            The old properties values of the modified versioned vertex.
	 * @return {@link Vertex} containing the old data before the vertex was modified.
	 */
	private Vertex createHistoricalVertex(VersionedVertex<V> modifiedVertex, Map<String, Object> oldValues) {
		// TODO: Auto identifier?
		Vertex hv = getBaseGraph().addVertex(modifiedVertex.getId() + "-" + getLatestGraphVersion());
		hv.setProperty(HISTORIC_ELEMENT_PROP_KEY, true);

		// ElementHelper.copyProperties(modifiedVertex, hv);
		// Iterate on the base prop keys as we'r currently working on an active vertex
		for (final String key : modifiedVertex.getBaseElement().getPropertyKeys()) {
			if (isVersionedVertexInternalProperty(key))
				continue;
			hv.setProperty(key, modifiedVertex.getBaseElement().getProperty(key));
		}

		for (Map.Entry<String, Object> prop : oldValues.entrySet())
		{
			String key = prop.getKey();
			Object value = prop.getValue();
			if (value == null) {
				hv.removeProperty(key);
			}
			else {
				hv.setProperty(key, value);
			}
		}

		return hv;
	}

	/**
	 * Add the historical vertex in the vertex versions chain
	 * 
	 * @param latestGraphVersion
	 *            The latest graph version
	 * @param newVersion
	 *            The new version to be committed
	 * @param modifiedVertex
	 *            The modified vertex
	 * @param newHistoricalVertex
	 *            The historical vertex of the modified vertex
	 */
	private void addHistoricalVertexInChain(V latestGraphVersion,
			V newVersion,
			VersionedVertex<V> modifiedVertex,
			Vertex newHistoricalVertex) {
		Iterable<Edge> currEdges = modifiedVertex.getBaseVertex().getEdges(Direction.OUT, PREV_VERSION_CHAIN_EDGE_TYPE);

		Iterator<Edge> currEdgesIterator = currEdges.iterator();

		if (currEdges.iterator().hasNext()) {
			Edge currEdge = currEdgesIterator.next();
			if (currEdgesIterator.hasNext())
				throw new IllegalStateException("Multiple versioned edges in vertex chain exist");

			// TODO: Edge id?
			getBaseGraph().addEdge(null,
					newHistoricalVertex,
					currEdge.getVertex(Direction.IN),
					PREV_VERSION_CHAIN_EDGE_TYPE);

			getBaseGraph().removeEdge((currEdge instanceof EventEdge) ? ((EventEdge) currEdge).getBaseEdge() : currEdge);
		}

		// TODO: Edge id?
		getBaseGraph().addEdge(null,
				((EventVertex) modifiedVertex).getBaseVertex(),
				newHistoricalVertex,
				PREV_VERSION_CHAIN_EDGE_TYPE);
		setStartVersion(newHistoricalVertex, getStartVersion(modifiedVertex));
		setEndVersion(newHistoricalVertex, latestGraphVersion);
	}

	/**
	 * Version the specified modified vertex.
	 * 
	 * TODO return vertex should be immutable.
	 * 
	 * @param latestGraphVersion
	 *            The latest graph version
	 * @param newVersion
	 *            The new version to be committed
	 * @param vertex
	 *            The modified vertex
	 * @param oldValues
	 *            The old properties values of the modified vertex
	 * 
	 * @return The historical created vertex
	 */
	protected Vertex versionModifiedVertex(V latestGraphVersion,
			V newVersion,
			VersionedVertex<V> vertex,
			Map<String, Object> oldValues) {

		Vertex historicalV = createHistoricalVertex(vertex, oldValues);
		addHistoricalVertexInChain(latestGraphVersion, newVersion, vertex, historicalV);
		setStartVersion(vertex, newVersion);
		vertex.setForVersion(newVersion);
		if (conf.getPrivateVertexHashEnabled())
			setPrivateHash(vertex);

		return historicalV;
	}

	/**
	 * Get the relevant vertex revision from the history for the specified vertex and version. TODO return vertex should
	 * be immutable.
	 * 
	 * @param vertex
	 *            The vertex to find the appropriate version for
	 * @param version
	 *            The version to find the revision for
	 * @return A vertex revision for the specified version
	 * @throws NotFoundException
	 *             In case no vertex revision was found for the specified vertex and version
	 */
	public Vertex getVertexForVersion(Vertex vertex, V version) {
		// TODO: Find a better approach to force versioning for vertex versions.
		if (vertex instanceof VersionedVertex)
			((VersionedVertex<V>) vertex).setForVersion(version);

		Range<V> verRange = getVersionRange(vertex);

		log.debug("Finding vertex[{}] in revision history for version [{}].", vertex, version);
		log.debug("Is vertex [{}] with range [{}] contains version [{}]?", vertex, verRange, version);

		if (!verRange.contains(version)) {
			log.debug("No, seeking for previous vertex version");
			// Iterate over the base (non filtered) edges to seek for the revision chain edge
			Iterable<Edge> prevVerEdges =
					getNonEventElement(vertex).getEdges(Direction.OUT, PREV_VERSION_CHAIN_EDGE_TYPE);

			if (!prevVerEdges.iterator().hasNext()) {
				throw ExceptionFactory.notFoundException(String.format("Cannot find vertex %s in revision history for version [%s].",
						vertex,
						version));
			}

			Edge edge = prevVerEdges.iterator().next();
			return getVertexForVersion(edge.getVertex(Direction.IN), version);
		}

		log.debug("Found vertex[{}] in revision history for version [{}].", vertex, version);
		return vertex;
	}

	/**
	 * Returns the property value of {@link VersionedVertex} for the specified key. Please note that this method will
	 * return the value of the property for the specific version set in the {@link VersionedVertex}.
	 * 
	 * A null will be returned if property doesn't exist for the specified {@link VersionedVertex} in its set version.
	 * 
	 * @see VersionedVertex#getForVersion()
	 * @param vertex
	 *            The {@link VersionedVertex} to get the property value of.
	 * @param key
	 *            The proeprty key
	 * @return The value of the specified property key for the specified @{link {@link VersionedVertex} or null if not
	 *         found.
	 */
	public Object getProperty(VersionedVertex<V> vertex, String key) {
		try {
			log.debug("Getting property [{}] for vertex [{}] for version [{}]", key, vertex, vertex.getForVersion());
			Vertex v = getVertexForVersion(vertex, vertex.getForVersion());
			// Return the value from the base element
			return getNonEventElement(v).getProperty(key);
		} catch (NotFoundException e) {
			return null;
		}
	}

	public Set<String> getPropertyKeys(VersionedVertex<V> vertex) {
		try {
			Vertex v = getVertexForVersion(vertex, vertex.getForVersion());
			// Return the keys from the base element
			return getNonEventElement(v).getPropertyKeys();
		} catch (NotFoundException e) {
			return null;
		}
	}

	// Internal Utils
	// --------------------------------------------------------------
	/**
	 * If the specified element supports Events, then return its base element.
	 * 
	 * @param element
	 *            The element to check whether it supports Events or not.
	 * @return If the specified element supports Events, then return its base element.
	 */
	@SuppressWarnings("unchecked")
	private <G extends Element> G getNonEventElement(G element) {
		if (element instanceof EventElement) {
			return (G) ((EventElement) element).getBaseElement();
		} else {
			return element;
		}
	}

	/**
	 * Raise an exception of the specified {@link Element} does not support events.
	 * 
	 * @param element
	 *            The element to be checked
	 */
	private void raiseExceptionIfNotVersionedElement(Element element) {
		if (!(element instanceof EventElement)) {
			throw new IllegalArgumentException(String.format("Element [%s] does not support events.", element));
		}
	}

	/**
	 * Returns whether the specified property key is used internally for versioned vertices or not.
	 * 
	 * @param key
	 *            The property key to determine
	 * @return true if property is for internal usage only
	 */
	public boolean isVersionedVertexInternalProperty(String key) {
		return versionedVertexInternalProperties.contains(key);
	}

	/**
	 * Return the key names of internal properties used by the {@code}VersionedGraph to version the {@link Graph}
	 * {@link Element}s.
	 * 
	 * @return An immutable set containing the internal property keys
	 */
	public static Set<String> getInternalProperties() {
		return VersionedGraph.versionedVertexInternalProperties;
	}

	/**
	 * Identify whether the specified {@link Element} is an historical/internal
	 * 
	 * @param e
	 *            The element to check
	 * @return true if the specified element is historical/internal
	 */
	public boolean isHistoricalOrInternal(Element e) {
		if (e instanceof Vertex)
			return (e.getPropertyKeys().contains(HISTORIC_ELEMENT_PROP_KEY));
		else if (e instanceof Edge)
			return ((Edge) e).getLabel().equals(PREV_VERSION_CHAIN_EDGE_TYPE);

		throw new IllegalArgumentException("The specified element is unidentified.");

	}

	/**
	 * Identifies whether the specified {@link Vertex} is versioned.
	 * 
	 * A versioned vertex contains for sure the 'HISTORIC_ELEMENT_PROP_KEY' property key which defines whether it is
	 * historic or not.
	 * 
	 * @param vertex
	 *            The vertex to test
	 * @return true if the specified vertex is versioned.
	 */
	public boolean isVersionedVertex(Vertex vertex) {
		return (vertex.getPropertyKeys().contains(HISTORIC_ELEMENT_PROP_KEY));
	}

	/**
	 * Identifies whether the specified {@link Edge} is versioned.
	 * 
	 * @param edge
	 *            The edge to test
	 * @return true if the specified edge is versioned.
	 */
	public boolean isVersionedEdge(Edge edge) {
		return ((edge.getPropertyKeys().contains(HISTORIC_ELEMENT_PROP_KEY) || edge.getLabel()
				.equals(PREV_VERSION_CHAIN_EDGE_TYPE)));
	}

	/**
	 * Get the vertex (history) chain of the specified vertex.
	 * 
	 * @param chain
	 *            An empty list that will contain the history chain
	 * @param v
	 *            The vertex to get the chain for
	 */
	public static void getVertexChain(ArrayList<Vertex> chain, Vertex v) {
		chain.add(v);

		// if it's a versionedVertex then don't filter internal edges
		Iterable<Edge> edges = null;
		if (v instanceof VersionedVertex<?>) {
			edges = ((VersionedVertex) v).getEdges(Direction.OUT, true, VersionedGraph.PREV_VERSION_CHAIN_EDGE_TYPE);
		} else {
			edges = v.getEdges(Direction.OUT, VersionedGraph.PREV_VERSION_CHAIN_EDGE_TYPE);
		}

		if (edges.iterator().hasNext()) {
			Vertex next = edges.iterator().next().getVertex(Direction.IN);
			getVertexChain(chain, next);
		}
	}
}
