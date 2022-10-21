## OpenGraph Software Architecture

The graph db solution is a multi-modules (maven based) software project build upon different architecture layers.

The fundamental idea behind this structure is the abstraction and compartmentalization of different software and functional
concerns.

The project is bounded as a multi-modules maven project which includes main core modules and additional specific utilities modules dedicated 
to enable functionality over opensearch storage layer.

-------------
 
The software building block in this project use many familiar and reliable open source libraries:

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
 
#### Web Container libraries
 - jooby  - https://github.com/jooby-project/jooby
 - netty  - https://github.com/netty/netty
 

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

The project is a monolith composed of multiple modules (components). The project uses Maven project build,management and dependency system.

There are **4 conceptual layers** of modules that the project is composed of:
- **Core layer** - which the main generic entities and models are defined
- **Virtual layer** - which the specific functionality implementing and extending the core layer
- **Service layer** - which compose these two layers and defines the **Model View Control** access to the API
- **Domain layer** -  which contains the domain specific modules including specific configuration and add-ons

The project structure is designed with the purpose of adding plugin as free as possible - hence the term pluggable architecture.
For this to be possible an effort was made to modularize the functional parts of the software to allow clear and direct extensibility.

### The Core Layer
Here the main generic entities and models are defined, the default behavior and functionality and the engine's core fundamental parts.

The core modules:
 - opengraph-model :  representing the data model elements (classes) used to query, profile, analyze, process and project the data.     
 - opengraph-asg :    containing the Abstract Syntax's Graph that is transformed from the query and is validated and rewritten according to rule based semantics.      
 - opengraph-core  :  containing the core functionality common to all generic parts such as Query planning, Transformations, Schema provider, Drivers and more     
 - unipop-core  : tinkerpop drivers adaptation for execution of graph traversal over opensearch (forked from https://github.com/unipop-graph/unipop)     
 - opengraph-statistics  :  statistics related components that are capable of creating a cost based index API that would help the query planning in determining optimal execution      

### The Virtualization Specific Layer

This layer contains specific functionality implementing and extending the core layer - specifically it's operating directly with opensearch and is coupled with it. 
 
- **virtual (Data Virtualization)**
    - virtual-asg :   the specific ASG behavior that is opensearch storage aware
    - virtual-core :  the specific core behavior that is opensearch storage aware
    - virtual-epb :   execution plan builder based on opensearch strong indexing and (statistics) counting capabilities
    - virtual-gta :   graph traversal extender which translates the cost revised execution plan into a physical execution plan (based on tinkerpop graph traversal)
    - virtual-unipop :  tinkerpop drivers adaptation for graph traversal over opensearch with specific changes introduced for performance

### The Service Layer

The Service Layer is the module which is responsible for running the Graph Query Engine.
- opengraph-services  :  containing the core services & controllers wrapping the Web / TCP endpoints and delivering deep traceability and logging

This component bundles the project's modules that are not domain specific, it also contains the runner component and configuration loader.
The service module uses [Jooby Web Server](https://github.com/jooby-project/jooby) web container to publish the RESTFULL API.

##### The Domain  Layer

These modules contain the domain related basic pluggable parts of the software allowing all the existing core functionality against opensearch - the default storage & indexing layer

- **opengraph-domain (different graph distributions)**
    - domain-**cyber** : a cyber typed assembly project pre-build with the logical & physical schema representing the cyber domain world (see https://oasis-open.github.io/cti-documentation/stix/intro.html)
    - domain-**dragons** : a fantasy based on Games Of Thrown ontology with dragons, kingdoms and more...
    - domain-**knowledge** : a special RDF typed flat ontology allowing the schema seamless evolution over ontological changes
    - domain-**observability** : an Open Telemetry based ontology including the trace, logs and metrics entities 
 
The usage of the dependency injection framework guice together with jooby (a modern, performant and easy to use web framework) makes it easy to load the modules in the correct order to allow pluggable architecture.

##### Modules loading sequence

The next modules list is part of the configuration file of OpenGraph: **application.conf** it shows the list of modules (plugins) that are loaded by the graph framework.

Contains:
  - core modules
  - virtual modules
  - service modules
  - domain modules

We can see here the code snippet taken from the **domain-knowledge** assembly distribution with its unique specific modules: 

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

We can observe that many modules are functional specific that offer a distinct funtional behavior and can be added/removed to allow functionality as needed.

### Plugin architecture for specific functionality  
**Specific Functional Modules**
  -   GraphQLModule - enabling graphql query support
  -   AsgGraphQLModule - enabling graphql ASG query translation & rewrite
  
  -   SparqlModule - enabling sparql query support
  -   AsgSparqlModule - enabling graphql ASG query translation & rewrite

  -   SqlModule - enabling sparql query support
  -   KnowledgeModule - specialized RDF like ontological structure & functionality support