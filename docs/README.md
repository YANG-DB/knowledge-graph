# Knowledge Graph

1) Introduction and general information [General information](info/General.md)
2) Catalog, the use case for data management [Catalog](info/Catalog.md)
3) Architecture Overview [Architecture](info/Architecture.md)


4) **Components**:
   1) The [Ontology](info/components/Ontology.md) 
   2) The intermediate [Query Language](info/components/Query-language.md) 
   3) The **_RESTFULL_** API structure [API](info/components/Api.md) 
   4) Query Flow from Http to Opensearch [Flow](info/components/UnderstandingQueryFlow.md) 
   5) The [Abstract Syntax Graph](info/components/ASG-AbstractSyntaxGraph.md) 
   6) The [ Cursor ](info/components/Cursor.md) 
   7) Execution Planner [ EPB ](info/components/Execution-planner.md) 
   8) Cost Based estimator [ Estimator ](info/components/Cost-estimator.md) 
   9) [ Traversal Translator ](info/components/TraversalTranslator.md) 
 

5) **Unipop**:
   1) Tinkerpop language and toolkit tutorial [Gremlin](https://kelvinlawrence.net/book/Gremlin-Graph-Guide.html)
   1) [Unipop](info/components/Unipop.md)
   1) The Gremlin [Step Controller](info/components/UnipopStepController.md)


6) **Configuration**:
   1) Configuration folder [Configuration](info/components/Configuration.md)
   2) Index physical schema config [Index Provider](info/components/Index-provider.md)


7) **Miscellaneous**
   1) General [Strategies](info/components/StrategiesMechanisms.md)

8) **Functionality**
   1) [Parameterize Constraints Query](info/functionality/ParameterizedConstraints.md)
   2) [Query Projections](info/functionality/QueryResultProjection.md)

9) **Planned Roadmap**:
   1) Ingestion, Fusion and Normalization [Ingestion](info/roadmap/IngestionNormalization.md)
   2) User Experience & ML future [UX Future](info/roadmap/OpensearchGraph.md)
   3) [Knowledge Deck](info/KnowledgeGraphDeck.md)
   4) [Development future threads](info/roadmap/DevelopmentThreads.md)

---- 

## Tutorials
8) **Dragons Tutorial**:
     1) Installation the Dragons domain [Dragons](tutorial/sample/dragons/installation.md)
     2) Creating the ontology and schema [Init Schema](tutorial/sample/dragons/create-ontology.md)
     3) UpLoading data into the graph [Data](tutorial/sample/dragons/load-data.md)
     4) Query the graph [Query](tutorial/sample/dragons/query-the-data.md)
     5) Projection a Query results [Projection](tutorial/sample/dragons/projection-and-count.md)
     6) Additional Queries [More Queries](tutorial/sample/dragons/queries/Queries.md)
 

9) **Observability Tutorial**:
     1) Observability work-plan [planning](info/roadmap/ObservabilityIntegration.md) 
     2) Getting started with [Observability](tutorial/sample/observability/GettingStarted.md) 
     3) Data Loading [Data](tutorial/sample/observability/DataLoading.md) 
     3) Running [Queries](tutorial/sample/observability/Queries.md) 