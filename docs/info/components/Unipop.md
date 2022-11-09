# Unipop Graph Traversing 

Unipop is a data Federation and Virtualization engine that models your data as a "virtual" graph, exposing a querying API using the Gremlin GQL (Sql and SPARQL are also available.)
This means you get the benefits of a graph data model without migrating/replicating/restructuring your data, whether its stored in a RDBMS, NoSql Store, or any other data source (ee "Customize and Extend" below.)
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

Gremlin is an imperative graph traversal language [Gremlin Guide](https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html)

[Gremlin](https://tinkerpop.apache.org/gremlin.html) is the graph traversal language of Apache TinkerPop.
Gremlin is an imperative, functional, data-flow language that enables users to succinctly express complex traversals on (or queries of) their application's property graph.

Every Gremlin traversal is composed of a sequence of (potentially nested) steps.
A step performs an atomic operation on the data stream, Every step is either a 
 - **map-step** transforming the objects in the stream
 - **filter-step** removing objects from the stream
 - **sideEffect-step** computing statistics about the stream

The Gremlin step library extends on these 3-fundamental operations to provide users a rich collection of steps that they can compose in order to ask any conceivable question they may have of their data for Gremlin is Turing Complete.


## Unipop in Opensearch Graph Component

Unipop is used as the underlying traversing engine that operates against the opensearch database. 
Unipop offers the next concept which allows interacting with any storage engine - lets view its components: 

### Unipop structure & components
 - Controller - The driver that interacts with the storage layer - Vertex, Edge, Filter 
 - Converter -  Converts the storage search results into Unipop's model (Vertices, Edges)
 - Appender -   Set of Strategies that go over the Unipop traversal and build an opensearch DSL query
 - Process   -  The Step processors that take a traversal and do the actual graph traversing on the traversal source


### GREMLIN STEPS (INSTRUCTION SET)
The following traversal is a Gremlin traversal in the Gremlin-Java8 dialect.
[Gremlin-Intro ](http://tinkerpop.apache.org/docs/current/reference/#intro)

```javascript
g.V().as("a").out("knows").as("b").select("a","b").by("name").by("age")
```

A string representation of the traversal above :

```javascript
[GraphStep([],vertex)@[a], VertexStep(OUT,[knows],vertex)@[b], SelectStep([a, b],[value(name), value(age)])]
```

The “steps” are the primitives of the Gremlin graph traversal machine. They are the parameterized instructions that the machine ultimately executes.

The Gremlin instruction set is approximately 30 steps. These steps are sufficient to provide general purpose computing and what is typically required to express the common motifs of any graph traversal query.

### GREMLIN VM
The Gremlin graph traversal machine can execute on a single machine or across a multi-machine compute cluster. Execution agnosticism allows Gremlin to run over both graph databases (OLTP) and graph processors (OLAP).

Gremlin steps are chained together to produce the actual traversal and are triggered by way of start steps on the GraphTraversalSource.

**Unipop Components**:

 - **Graph**: maintains a set of vertices and edges, and access to database functions such as transactions.
 - **Element**: maintains a collection of properties and a string label denoting the element type.
   - **Vertex**: extends Element and maintains a set of incoming and outgoing edges.
   - **Edge**: extends Element and maintains an incoming and outgoing vertex.

 - **Property<V>**: a string key associated with a V value.
 - **VertexProperty<V>**: a string key associated with a V value as well as a collection of Property<U> properties (vertices only)

**Traversal Components**:

- **TraversalSource**: 
A generator of traversals for a particular graph, domain specific language (DSL), and execution engine.
  
**Traversal<S,E>**:
A functional data flow process transforming objects of type S into object of type E.
    
**GraphTraversal**:
A traversal DSL that is oriented towards the semantics of the raw graph (i.e. vertices, edges, etc.).


[Unipop](../../../unipop-core)

is used by [Virtual-Unipop](../../../virtualize/virtual-unipop) to generate a specialized traversal component that abstracts the storage engine with its proprietary DSL query language.
Therefore, allowing them to be agnostic to how the data is stored and how to query the data, and reduce this complexity to be pushed downwards to the traversal layer.


## Unipop Graph Traversal & Other structural Uni-components

The Graph Traversal component is the traversing context that allows all the graph traversing API such as - 
````
 where(), to(), fold(), map() ...
````

In our graph adaptation we created [SearchGraphTraversal](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/process/traversal/dsl/graph/SearchGraphTraversal.java) counterpart

So that it will allow working with our [UniGraph](../../../unipop-core/src/main/java/org/unipop/structure/UniGraph.java) graph component.

## SearchUniGraph
The [SearchUniGraph](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/structure/SearchUniGraph.java) is an implementation to the UniGraph.

When Created it must accept the following parameter:
 - Configuration      - configuration that defines information such as bulk min / max size 
 - ControllerFactory  - a factory for the UniQueryController such as (**Vertex** controller, **Edge** controller, **VertexFilter** controller)
 - StrategyProvider   - gremlin strategies provider that dictate how the step processor must work 

----

Additional Uni-components that override the original Tinkerpop structural components are:
 
- **UniVertex**
- **UniEdge**
- **UniProperty**
- **UniFeature**

All these components reside on Unipop-Core module [Uni-components](../../../unipop-core/src/main/java/org/unipop/structure)

## Unipop Step Processing components

In addition to the structural unipop components, unipop also overrides the step components:

 - UniGraphVertexStep
 - UniGraphEdgeVertexStep
 - UniGraphOptionalStep

These are just few of the **UniGraph** steps that are used to handle the traversing of the graph elements.
Important to mention that all the **UniGraph** derive from a base [UniBulkStep](../../../unipop-core/src/main/java/org/unipop/process/UniBulkStep.java)

### Bulk Step Iterator
Buffering (or streaming) is a paging technic that allows each step to buffer results until a certain size is reached before continuing to the next step.

In a step, having a filter can produce less results than the bulk size – due to the input vertices being filtered out, In such cases we would like to continue
fetching results from the datastore until the bulk size is reached.

This base class **_UniBulkStep_** allows the bulk fetching of the graph elements from the underlying graph storage layer.


## Unipop Steps and Controllers

As mentioned earlier, the gremlin steps describe how the traversal will go over the graph. These are imperative instructions, in which each instruction
dictates some action against the actual graph.

In the [Traverser Translator](TraversalTranslator.md) we showed how the logical plan was translated into a gremlin traverse with its specific steps.

