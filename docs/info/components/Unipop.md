# Unipop Graph Traversing 
Unipop is a data Federation and Virtualization engine that models your data as a "virtual" graph, exposing a querying API using the Gremlin GQL (Sql and SPARQL are also available.)

This means you get the benefits of a graph data model without migrating/replicating/restructuring your data, whether its stored in a RDBMS, NoSql Store, or any other data source (see "Customize and Extend" below.)

Graphs provide a very "natural" way to analyze your data. The simple Vertex/Edge structure makes it very easy to model complex and varied data, and then analyze it by exploring the connections/relationships in it.

This is especially relevant for a data Federation / Virtualization platform, which integrates a large variety of different data sources, structures, and schemas.

## Tinkerpop
The Tinkerpop framework [Tinkerpop](https://github.com/apache/tinkerpop) also provides us with other useful features "out of the box":

 - Traversal Strategies - an extensible query optimization mechanism. Unipop utilizes this to implement different performance optimizations.
 - Console & Server - production grade tooling.
 - Language Drivers - JavaScript, TypeScript, PHP, Python, Java, Scala, .Net, Go.
 - Extensible Query Languages - Gremlin, SQL, SPARQL
 - DSL support
 - Testing Framework

## Gremlin Traversal Language

Gremlin is an imperative graph traversal language 

[Gremlin](https://tinkerpop.apache.org/gremlin.html) is the graph traversal language of Apache TinkerPop.
Gremlin is an imperative, functional, data-flow language that enables users to succinctly express complex traversals on (or queries of) their application's property graph.


Every Gremlin traversal is composed of a sequence of (potentially nested) steps.
A step performs an atomic operation on the data stream, Every step is either a map-step (transforming the objects in the stream),
a filter-step (removing objects from the stream), or a sideEffect-step (computing statistics about the stream).

The Gremlin step library extends on these 3-fundamental operations to provide users a rich collection of steps that they can compose in order to ask any conceivable question they may have of their data for Gremlin is Turing Complete.

### Example:

_** What is the distribution of job titles amongst Gremlin's collaborators?**_
```java
g.V().has("name","gremlin").as("a").
    out("created").in("created").
    where(neq("a")).
    groupCount().by("title")
```

## Unipop in Opensearch Graph Component

Unipop is used as the underlying traversing engine that operates against the opensearch database. 

It is used by [Virtual-Unipop](../../../virtualize/virtual-unipop) to generate a specialized traversal component that abstracts the storage engine with its proprietary DSL query langauge.
Therefore allowing them to be agnostic to how the data is stored and how to query the data, and reduce this complexity to be pushed downwards to the traversal layer.

