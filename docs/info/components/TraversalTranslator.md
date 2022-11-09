# Traversal Translator

This document will review the execution traversal builder role, functionality and usage.
We will review where in the query life cycle this part plays its role and how we can extend this
functionality with specific rules and strategies.

## Plan Translator

As mentioned in the execution plan builder document [Execution Plan Builder ](Execution-planner.md) the logical plan is being translated 
into a Unipop traverser.

The interface the takes care of this translation is [Plan Traversal Translator](../../../opengraph-core/src/main/java/org/opensearch/graph/dispatcher/gta/PlanTraversalTranslator.java)
It accepts two parameter:

 - The Logical Plan 
 - The Traverser Context 

Logical plan is combined of logical steps where each step is translated into the corresponding unipop gremlin steps.

Traverser is the contextual scope where the translation is happening, it contains 
 - An ontology accessor with information about the logical schema structure
 - A UniGraph provider (provides the Unipop Graph component on top of opensearch) [OpenSearch Uni Graph Provider](../../../virtualize/virtual-core/src/main/java/org/opensearch/graph/executor/ontology/promise/M1OpenSearchUniGraphProvider.java)

This translation phase is occurring during the Query Cursor Creation - [CursorDriver](../../../virtualize/virtual-core/src/main/java/org/opensearch/graph/core/driver/StandardCursorDriver.java)  
in the ````createTraversal(PlanWithCost<Plan, PlanDetailedCost> plan, Ontology ontology)```` method where it accepts the Plan and the Ontology
and produce a GraphTraversal.

The actual [GraphTraversal](../../../virtualize/virtual-unipop/src/main/java/org/opensearch/graph/unipop/process/traversal/dsl/graph/SearchGraphTraversal.java) is a Unipop Graph traversal 
More about Unipop & Tinkerpop please visit [Unipop](Unipop.md)

### Translation Strategies 
For Each logical step we apply a list of translation strategies which are defined in the [PlanOpTranslationStrategy](../../../virtualize/virtual-traversal/src/main/java/org/opensearch/graph/gta/strategy/discrete/M2PlanOpTranslationStrategy.java)

Let's review some of these strategies:

 - **EntityOpTranslationStrategy** : this strategy translated an EntityOp (entity operation ) into a Unipop set of steps representing this specific plan operation

 - **EntityFilterOpTranslationStrategy** : this strategy translated an EntityFilterOp (entity with filter operation ) into a Unipop set of steps representing this operation

 - **RelationOpTranslationStrategy** : this strategy translated an RelationOp (relation operation ) into a Unipop set of steps representing this operation

 - **RelationFilterOpTranslationStrategy** : this strategy translated an RelationFilterOp (relation filter operation ) into a Unipop set of steps representing this operation


All the strategies are registered in a dedicated translation strategies container [Plan Translator Strategy](../../../virtualize/virtual-traversal/src/main/java/org/opensearch/graph/gta/strategy/promise/M2PlanOpTranslationStrategy.java) 

```java
    new EntityOpTranslationStrategy(EntityTranslationOptions.none),
            new GoToEntityOpTranslationStrategy(),
            new RelationOpTranslationStrategy(),
            new CompositePlanOpTranslationStrategy(
                    new EntityFilterOpTranslationStrategy(EntityTranslationOptions.none),
                    new EntitySelectionTranslationStrategy()),
            new RelationFilterOpTranslationStrategy()
```

Each of the PlanOp Strategy Translator addresses a different Plan operation.