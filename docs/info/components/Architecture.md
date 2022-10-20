## OpenGraph Software Architecture

The graph db solution is a multi-modules software project build upon different architecture layers.

The fundamental idea behind this structure is the abstraction and compartmentalization of different software and functional
concerns.

The project is boundeled as a multi-modules maven project which includes 5 main core modules and 10 specific utilities modules dedicated 
to enable functionality over opensearch storage layer.

-------------
 
The software building block used in this project are dominating open source libraries:

#### Software General purpose libraries
 - Apache guava  - https://github.com/google/guava
 - Apache guice  - https://github.com/google/guice
 - Apache commons - https://commons.apache.org/ 
 - javaslang -      https://github.com/vavr-io/vavr
 
#### Graph libraries
 - tinkerpop - https://github.com/apache/tinkerpop
 - rdf4j -  https://github.com/eclipse/rdf4j
 - owlapi - https://github.com/owlcs/owlapi
 - openCypher - https://github.com/opencypher/openCypher 

#### Indexing store libraries
 - Lucene   - https://github.com/apache/lucene-solr
 - opensearch - https://github.com/elastic/opensearch
 
#### MVC Web libraries
 - jooby  - https://github.com/jooby-project/jooby
 - netty  - https://github.com/netty/netty
 
 
Additional Libraries can be found in the project's pom.xml

-------------

## Architecture
As in many complex software components, the engineering approach for development of this Graph engine is based on the following
concepts and patterns:

- Single Responsibility
- Separation of concerns
- Design to Interface
- Clear definition of domain boundaries
- Implement Model View Control paradigm [MVC](https://martinfowler.com/eaaDev/uiArchs.html)

These concepts and many others help reduce complexity and keep the code focus as possible on the real inherent complexities.

### Plugin Architecture
The project is developed with the Plugin-Architecture in mind, this concept is focused on the next elements
- Extendability - Easily extend functionality by simply adding a new plugin
- Configurability - Change basic behavior by replacing plugin or changing plugin loading configuration
- Dependency Injection - Allow for plugins to be depended on one another using Dependency Injection

These capabilities are a root concept of many open source extensible projects (opensearch for example) and makes use of
google guice dependency injections platform [Guice](https://github.com/google/guice)

### Modules and structure

The project is a monolith composed of multiple modules (components)
The project uses Maven project build,management and dependency system.

There are **3 conceptual layers** of modules that the project is composed of:
- **Core layer** - which the main generic entities and models are defined
- **Virtual layer** - which the specific functionality implementing and extending the core layer
- **Service layer** - which compose these two layers and defines the **Model View Control** access to the API

### Architecture Layers
The project structure is designed with the purpose of adding plugin as free as possible - hence the term open architecture.
For this to be possible an effort was made to modularize the functional parts of the software to allow clear and direct extensibility.

Essentially there are 3 layers that compose the general architecture:

### The Service Layer
The Service Layer is the module which is responsible for running the Graph Query Engine. 
- open-graph-services  :  containing the core services & controllers wrapping the Web / TCP endpoints and delivering deep traceability and logging

##### The Core Layer
The 5 core modules:
 - open-graph-model :  representing the data model elements (classes) used to query, profile, analyze, process and project the data     
 - open-graph-asg :    containing the Abstract Syntax's Graph that is transformed from the query and is validated and rewritten according to rule based semantics.      
 - open-graph-core  :  containing the core functionality common to all generic parts such as Query planning, Transformations, Schema provider, Drivers and more     
 - unipop-core  : tinkerpop drivers adaptation for execution of graph traversal over elasticserch (forked from https://github.com/unipop-graph/unipop)     
 - open-graph-statistics  :  statistics related components that are capable of creating a cost based index API that would help the query planning in determining optimal execution      

##### The Virtualization Specific Layer

##### The Domain Specific Layer
These modules are the basic "plug-able" parts of the software allowing all the existing core functionality against opensearch - the default storage & indexing layer

-  **open-graph-dv (Data Virtualization)**
    - open-graph-dv-asg :   the specific ASG behavior that is opensearch storage aware 
    - open-graph-dv-core :  the specific core behavior that is opensearch storage aware 
    - open-graph-dv-epb :   execution plan builder based on opensearch strong indexing and (statistics) counting capabilities 
    - open-graph-dv-gta :   graph traversal extender which translates the cost revised execution plan into a physical execution plan (based on tinkerpop graph traversal) 
    - open-graph-dv-unipop :  tinkerpop drivers adaptation for graph traversal over elasticserch with specific changes introduced for performance 
    - open-graph-dv-stat :  statistical calculation module enabling the usage of dynamic execution planning and modeling based on pre-calculated cardinality statistics

- **open-graph-domain (different graph distributions)**
    - open-graph-domain-cyber : a cyber typed assembly project pre-build with the logical & physical schema representing the cyber domain world (see https://oasis-open.github.io/cti-documentation/stix/intro.html)
    - open-graph-domain-dragons : a fantasy based on Games Of Thrown ontology with dragons, kingdoms and more...
    - open-graph-domain-knowledge : a special RDF typed flat ontology allowing the schema seamless evolution over ontological changes
 
The usage of the dependency injection framework guice togather with jooby (a modern, performant and easy to use web framework) makes it easy to load the modules in the correct order to allow plug-able architecture.

##### Modules loading sequence
The next modules list is stated in the core configuration file of OpenGraph: **application.conf**

We can see here the code snippet taken from the knowledge assembly distribution with its unique specific modules: 

```
    modules.activeProfile = [
      "org.opensearch.graph.services.modules.ServiceModule",
      "org.opensearch.graph.services.modules.LoggingJacksonModule",
      "org.opensearch.graph.dispatcher.modules.CoreDispatcherModule",
      "org.opensearch.graph.dispatcher.query.graphql.GraphQLModule",
      
      
      "org.opensearch.graph.dispatcher.modules.DescriptorsModule",
      "org.opensearch.graph.asg.translator.graphql.AsgGraphQLModule",
      "org.opensearch.graph.asg.translator.cypher.AsgCypherModule",
      "org.opensearch.graph.asg.M2AsgModule",
      "org.opensearch.graph.epb.plan.modules.EpbDfsCountBasedRedundantModule"
      "org.opensearch.graph.asgAsgValidationModule",
      "org.opensearch.graph.gta.module.GtaModule",
      "org.opensearch.graph.executor.ExecutorModule",
      "org.opensearch.graph.executor.modules.discrete.CursorsModule",
      "org.opensearch.graph.assembly.knowledge.KnowledgeModule",
    ]
```

We can observe that many modules are functional specific that offer a distinct funtional behavior and can be added/removed to add/remove functionality as needed.
  
**Specific Functional Modules**
  -   GraphQLModule - enabling graphql query support
  -   AsgGraphQLModule - enabling graphql ASG query translation & rewrite
  
  -   SparqlModule - enabling sparql query support
  -   AsgSparqlModule - enabling graphql ASG query translation & rewrite

  -   SqlModule - enabling sparql query support
  -   KnowledgeModule - specialized RDF like ontological structure & functionality support