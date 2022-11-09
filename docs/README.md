# Knowledge Graph

[Overview & Roadmap](info/roadmap/OpensearchGraph.md)

1) Introduction and general information [General information](info/General.md)


2) Catalog, the use case for data management [Catalog](info/Catalog.md)


3) Architecture Overview [Architecture](info/Architecture.md)


4) Representing relations inside Opensearch [Relations](info/components/OpensearchRelationships.md)


5) **Components**:
   - The [Ontology](info/components/Ontology.md) 
   - The intermediate [Query Language](info/components/Query-language.md) 
   - The **_RESTFULL_** API structure [API](info/components/Api.md) 
   - Query Flow from Http to Opensearch [Flow](info/components/UnderstandingQueryFlow.md) 
   - The [Abstract Syntax Graph](info/components/ASG-AbstractSyntaxGraph.md) 
   - The [ Cursor ](info/components/Cursor.md) 
   - Execution Planner [ EPB ](info/components/Execution-planner.md) 
   - Cost Based estimator [ Estimator ](info/components/Cost-estimator.md) 
   - [ Traversal Translator ](info/components/TraversalTranslator.md) 


6) **Unipop**:
   - Tinkerpop language and toolkit tutorial [Gremlin](https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html)
   - [Unipop](info/components/Unipop.md)
   - The Gremlin [Step Controller](info/components/UnipopStepController.md)


7) **Configuration**:
   - Configuration folder [Configuration](info/components/Configuration.md)
   - Index physical schema config [Index Provider](info/components/Index-provider.md)


8) **Miscellaneous**:
   - General [Strategies](info/components/StrategiesMechanisms.md)


9) **Functionality**:
   - [Parameterize Constraints Query](info/functionality/ParameterizedConstraints.md)
   - [Query Projections](info/functionality/QueryResultProjection.md)


10) **Planned Roadmap**:
- Ingestion, Fusion and Normalization [Ingestion](info/roadmap/IngestionNormalization.md)
- [Knowledge Deck](info/KnowledgeGraphDeck.md)
- [Development future threads](info/roadmap/DevelopmentThreads.md)

---- 

## Getting Started
- Setting up the development environment 
- Using containers for development and tests 

---- 

## Tutorials
**Dragons Tutorial**:
 1) Installation the Dragons domain [Dragons](tutorial/sample/dragons/installation.md)
 2) Creating the ontology and schema [Init Schema](tutorial/sample/dragons/create-ontology.md)
 3) UpLoading data into the graph [Data](tutorial/sample/dragons/load-data.md)
 4) Query the graph [Query](tutorial/sample/dragons/query-the-data.md)
 5) Projection a Query results [Projection](tutorial/sample/dragons/projection-and-count.md)
 6) Additional Queries [More Queries](tutorial/sample/dragons/queries/Queries.md)
 

**Observability Tutorial**:
 1) Observability work-plan [planning](info/roadmap/ObservabilityIntegration.md) 
 2) Getting started with [Observability](tutorial/sample/observability/GettingStarted.md) 
 3) Data Loading [Data](tutorial/sample/observability/DataLoading.md) 
 4) Running [Queries](tutorial/sample/observability/Queries.md) 