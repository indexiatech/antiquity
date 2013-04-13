Antiquity
=========
[![Build Status](https://travis-ci.org/asaf/antiquity.png?branch=master)](https://travis-ci.org/asaf/antiquity)

![diagram](http://dl.dropbox.com/u/21514850/antiquity/Antiquity-vertices-in-1-diagram.png)

Antiquity - A versioned graph.

Antiquity is a versioned graph with full history support for graph elements.

Antiquity is not tight to any specific graph database, this is achieved by using 
Tinkerpop Blueprints project (http://blueprints.tinkerpop.com) which provides an abstraction layer for graph underlines 
such as Neo4j, OrienDB, Titan, etc.

Creating a versioned graph
==========================

Creating a versioned graph basically cosntructed from two main items:

1. The underline blueprint graph implementation (for simplicity, in memory TinkerGraph graph is used below)
1. The versioned graph that wraps the underline graph (for simplicty a non transactional versioned graph is used).

```java
//A blueprint graph
TinkerGraph baseGraph = new TinkerGraph();
//Create a non transactional versioned graph with long version identifier
NonTransactionalVersionedGraph<TinkerGraph, Long> graph = new NonTransactionalVersionedGraph<TinkerGraph, Long>(
  			baseGraph, new LongGraphIdentifierBehavior());
```

Writing/Reading the graph
=========================

Here is an example how to create a vertex with few updates where each update is stored as a version.

```java
Vertex v = graph.addVertex("versioned_vertex");
Long version1 = graph.getLatestGraphVersion();
v.setProperty("key", "foo");
Long version2 = graph.getLatestGraphVersion();
v.setProperty("key", "bar");
Long version3 = graph.getLatestGraphVersion();
//prints null
System.out.println(graph.getVertexForVersion(v, version1).getProperty("key"));
//prints foo
System.out.println(graph.getVertexForVersion(v, version2).getProperty("key"));
//prints bar
System.out.println(graph.getVertexForVersion(v, version3).getProperty("key"));
```

Working with the graph is simply done by the standard BluePrint's Graph interface.
The method graph.getVertexForVersion(Vertex v, Long ver) loads a vertex state for a specific version allowing
a full view of how the vertex (with its edges) looked like back then in a previous version.
