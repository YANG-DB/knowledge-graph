# Strategies Mechanisms

The different layer of the application offers different point which are subject to transformations of elements.
This transformation includes:
 
 - Query Validation
 - Query Transformation
 - Plan Transformation
 - Graph execution Transformation
 - Query DSL Transformation


Let's review them:

## Query Transformation
The json query has to become an object graph so that it can be traversed and analyzed.
The responsibility of the Query transformer is that exactly.

[QueryToAsgTransformer](../../../opengraph-core/src/main/java/org/opensearch/graph/dispatcher/asg/QueryToAsgTransformer.java) is responsible for 
transforming the [Query](../../../opengraph-model/src/main/java/org/opensearch/graph/model/query/Query.java) into an [ASGQuery](../../../opengraph-model/src/main/java/org/opensearch/graph/model/asgQuery/AsgQuery.java)


The different strategies that take part in this transformation are:
```java
    public Iterable<AsgStrategy> register() {
        return Arrays.asList(
                new DefaultETagAsgStrategy(),
                new AsgNamedParametersStrategy(),
                new UntypedInferTypeLeftSideRelationAsgStrategy(),
                new RelationPatternRangeAsgStrategy(),
                new UntypedRelationInferTypeAsgStrategy(),
                new EPropGroupingAsgStrategy(),
                new HQuantPropertiesGroupingAsgStrategy(),
                new Quant1PropertiesGroupingAsgStrategy(),
                new RelPropGroupingAsgStrategy(),
                new ConstraintTypeTransformationAsgStrategy(),
                new ConstraintIterableTransformationAsgStrategy(),
                new RedundantLikeConstraintAsgStrategy(),
                new RedundantLikeAnyConstraintAsgStrategy(),
                new AggFilterTransformationAsgStrategy(),
                new LikeToEqTransformationAsgStrategy(),
                new MultiConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new LikeAnyConstraintTransformationAsgStrategy(this.ontologyProvider, this.schemaProviderFactory),
                new NestingPropertiesTransformationAsgStrategy(this.schemaProviderFactory),
                new RedundantInSetConstraintAsgStrategy(),
                new RedundantInRangeConstraintAsgStrategy(),
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
```
These strategies are registered in an [AsgStrategyRegistrar](../../../virtualize/virtual-asg/src/main/java/org/opensearch/graph/asg/strategy/M2AsgStrategyRegistrar.java)
Each strategy addresses a different aspect and concern


## Query Validation

Another transformation strategy is the [Query Validator](../../../opengraph-asg/src/main/java/org/opensearch/graph/asg/validation/AsgQueryValidator.java) This strategy doesn't produce a new entity but only return a validation assert if
the query is not valid (or OK is valid).

The responsibility of [AsgValidatorStrategyRegistrar](../../../opengraph-asg/src/main/java/org/opensearch/graph/asg/validation/AsgValidatorStrategyRegistrarImpl.java) is to define the relevant validation strategies.
During the validation process each is applied against the query.  

```java
    public Iterable<AsgValidatorStrategy> register() {
        return Collections.singletonList(new CompositeValidatorStrategy(
                new AsgConstraintExpressionValidatorStrategy(),
                new AsgCycleValidatorStrategy(),
                new AsgCompositeQueryValidatorStrategy(),
                new AsgEntityDuplicateEnumValidatorStrategy(),
                new AsgEntityDuplicateETagValidatorStrategy(),
                new AsgEntityPropertiesValidatorStrategy(),
                new AsgOntologyEntityValidatorStrategy(),
                new AsgOntologyRelValidatorStrategy(),
                new AsgRelPropertiesValidatorStrategy(),
                new AsgStartEntityValidatorStrategy(),
                new AsgWhereByConstraintValidatorStrategy(),
                new AsgStepsValidatorStrategy()
        ));
    }
```
## Plan Builder
The Plan Builder is also a strategy mechanism with a purpose of transforming the ASG Query into a Logical execution Plan.
The ASG-Query object is transformed into [Plan](../../../opengraph-model/src/main/java/org/opensearch/graph/model/execution/plan/composite/Plan.java).

The transformation is happening in a [PlanSearcher](../../../virtualize/virtual-epb/src/main/java/org/opensearch/graph/epb/plan/BottomUpPlanSearcher.java)

The list of strategies if defined in [PlanExtensionStrategy](../../../virtualize/virtual-epb/src/main/java/org/opensearch/graph/epb/plan/extenders/M2/M2PlanExtensionStrategy.java)
```java
    new ChainPlanExtensionStrategy<>(
        new CompositePlanExtensionStrategy<>(
            new InitialPlanGeneratorExtensionStrategy(),
            new JoinSeedExtensionStrategy(new InitialPlanGeneratorExtensionStrategy()),
            new JoinOngoingExtensionStrategy(
                getJoinInnerExpander(2)),
            new StepAncestorAdjacentStrategy(),
            new StepDescendantsAdjacentStrategy(),
            new ChainPlanExtensionStrategy<>(
                new CompositePlanExtensionStrategy<>(//new GotoExtensionStrategy(),
                new GotoJoinExtensionStrategy()),
                new CompositePlanExtensionStrategy<>(
                        new StepAncestorAdjacentStrategy(),
                        new StepDescendantsAdjacentStrategy()
                )
            )
        ),
        new RedundantFilterPlanExtensionStrategy(
            ontologyProvider,
            schemaProviderFactory)
        )

```
The Plan Searcher is working dynamically to build an execution plan using these strategies.

## Traversal Translator
The Traversal Translator is responsible for transforming a Plan into a [GraphTraversal](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/process/traversal/dsl/graph/SearchGraphTraversal.java)

The [TraversalTranslator](../../../virtualize/virtual-traversal/src/main/java/org/opensearch/graph/gta/translation/discrete/M2PlanTraversalTranslator.java) is the actual transformer.  
This transformation is triggered from the [Cursor Driver](../../../virtualize/virtual-core/src/main/java/org/opensearch/graph/core/driver/StandardCursorDriver.java) as part of the creation of the cursor resource.

As before, the list of strategies that is being used to transform the plan is stored in [PlanOpTranslationStrategy](../../../virtualize/virtual-traversal/src/main/java/org/opensearch/graph/gta/strategy/promise/M2PlanOpTranslationStrategy.java)
```java
            new EntityOpTranslationStrategy(EntityTranslationOptions.none),
                new GoToEntityOpTranslationStrategy(),
                new RelationOpTranslationStrategy(),
                new CompositePlanOpTranslationStrategy(
                        new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                        new EntitySelectionTranslationStrategy()),
                new RelationFilterOpTranslationStrategy());
```

## Unipop traversal Translator
In Unipop (similar to Tinkerpop) a traversal is always accompanied by transformation strategies - these strategies dictate the rules
of transforming the Gremlin traversal.

During creation of UniGraph, it is provided with a set of strategies [StrategyProvider](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/process/traversal/strategy/StandardStrategyProvider.java)

```java
        traversalStrategies.addStrategies(
                new CompositeStrategy(Stream.of(
                        new UniGraphStartStepStrategy(),
                        new UniGraphStartCountStepStrategy(),
                        new UniGraphVertexStepStrategy(),
                        new UniGraphStartEdgeCountStepStrategy(),
                        new GraphEdgeStepsStrategy(),
                        new UniGraphPropertiesStrategy(),
                        new UniGraphCoalesceStepStrategy(),
                        new UniGraphWhereStepStrategy(),
                        new UniGraphUnionStepNewStrategy(),
                        new UniGraphRepeatStepStrategy(),
                        new UniGraphOrderStrategy(),
                        new UniGraphOptionalStepStrategy()).toJavaList()
                ));

```

Each strategy addresses a different UniGraph Step.

## Search Appender
The [SearchAppender](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/controller/common/appender/SearchAppender.java) is responsible for
translating the Traversal Step context [BulkContext](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/controller/common/context/BulkContext.java) into a SearchBuilder object.

The [SearchBuilder](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/controller/search/SearchBuilder.java) holds the QueryBuilder - we will review this soon.

This translation is happening inside the Step's controller (driver to the engine)

```java
        CompositeSearchAppender<CompositeControllerContext> searchAppender =
                new CompositeSearchAppender<>(CompositeSearchAppender.Mode.all,
                        wrap(new ElementIndexSearchAppender()),
                        wrap(new SizeSearchAppender(this.configuration)),
                        wrap(new ConstraintSearchAppender()),
                        wrap(new FilterSourceSearchAppender()),
                        wrap(new FilterSourceRoutingSearchAppender()),
                        wrap(new ElementRoutingSearchAppender()),
                        wrap(new MustFetchSourceSearchAppender(GlobalConstants.TYPE)),
                        wrap(new NormalizeRoutingSearchAppender(50)),
                        wrap(new NormalizeIndexSearchAppender(100)));

```


## DSL Query Builder

The DSL Query Builder is another type of transformer that transforms the gremlin traversal into opensearch query.
The [QueryTranslator](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/controller/search/translation/M1QueryTranslator.java) holds the translator strategies.

```java
    new HiddenQueryTranslator(
        new CompareQueryTranslator(true),
        new ExclusiveChainTranslator(
            new ContainsGeoBoundsQueryTranslator("geoValue"),
            new ContainsGeoDistanceQueryTranslator("geoValue"),
            new ContainsQueryTranslator()
        ),
        new ExistsQueryTranslator(),
        new CountFilterQueryTranslator(),
        new TextQueryTranslator(),
        new AndPQueryTranslator(
            new CompareQueryTranslator(true),
            new ExclusiveChainTranslator(
            new ContainsGeoBoundsQueryTranslator("geoValue"),
            new ContainsQueryTranslator()),
            new ExistsQueryTranslator(),
            new CountFilterQueryTranslator(),
            new TextQueryTranslator()
        ),
        new OrPQueryTranslator(
            new CompareQueryTranslator(false),
            new ExclusiveChainTranslator(
                new ContainsGeoBoundsQueryTranslator("geoValue"),
                new ContainsGeoDistanceQueryTranslator("geoValue"),
            new ContainsQueryTranslator()),
            new ExistsQueryTranslator(),
            new CountFilterQueryTranslator(),
            new TextQueryTranslator()
            )
    )
```
The [TraversalQueryTranslator](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/controller/utils/traversal/TraversalQueryTranslator.java) iterates over the traversal steps and translates them
into Opensearch Query DSL using Unipop's [QueryBuilder](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/controller/search/QueryBuilder.java)

Each Translator is either a composite translator or derived from [PredicateQueryTranslator]()
QueryBuilder is used to create the Query for each gremlin step. The step's controller is used to dispatch the query to the search engine.

Once the QueryBuilder has finished the query creation - the controller creates a [SearchHitScrollIterable](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/converter/SearchHitScrollIterable.java)
which will execute the query against the engine.

