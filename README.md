Antiquity
=========
[![Build Status](https://travis-ci.org/asaf/antiquity.png?branch=master)](https://travis-ci.org/asaf/antiquity)

![diagram](http://dl.dropbox.com/u/21514850/antiquity/Antiquity-vertices-in-1-diagram.png)

Antiquity - A versioned graph.

Antiquity is a [Blueprints](http://blueprints.tinkerpop.com) extension that enhances any Blueprints graph with full versioning and history support,
This can work with any underline graph DB that Blueprints supports such as _Neo4j, OrientDB, Titan, etc..._

Creating a versioned graph
==========================

Creating a versioned graph basically cosntructed from two main items:

1. The underline blueprint graph implementation.
1. The versioned graph that wraps the underline graph.

```java
TinkerGraph graph = new TinkerGraph();
        Configuration conf = new Configuration.ConfBuilder().build();
        ActiveVersionedGraph<TinkerGraph, Long> vg = new ActiveVersionedGraph.ActiveVersionedNonTransactionalGraphBuilder<TinkerGraph, Long>(
                graph, new LongGraphIdentifierBehavior()).init(true).conf(conf).build();
//Do something with vg (versioned graph) here (read blow)
```

Writing/Reading the graph
=========================

Here is an example how to create a vertex with few updates where each update is stored as a version.

```java
Vertex v = vg.addVertex("item1");
v.setProperty("key", "foo");
long verFoo = vg.getLatestGraphVersion();
v.setProperty("key", "bar");
long verBar = vg.getLatestGraphVersion();
//Working with vg is just like working with the graph itself, it only contains the latest data.
System.out.println(vg.getVertex("item1").getProperty("key")); //prints bar

//You can query how a vertex looked like in previous states by using the historic graph API
Vertex item1InVer1 = vg.getHistoricGraph().getVertexForVersion(v.getId(), verFoo);
System.out.println(item1InVer1.getProperty("key")); //prints foo
Vertex item1InVer2 = vg.getHistoricGraph().getVertexForVersion(v.getId(), verBar);
System.out.println(item1InVer2.getProperty("key")); //prints bar
```
