# ASG - Abstract Syntax Graph

The purpose of this component is to accept a textual query and transform it into a Graph of objects which 
represents the query in the query domain model.

In every database engine there is a similar component which is responsible for the same activities.

 - **Accepting** a textual representation of the query

 - **Verify** its correctness according to that language rules and syntax

 - **Transforming** it into the query domain objects to create a hierarchy representation of that query
   - Each query element in the tree represents a language operator / operand / function or other part of the query domain elements.

 - **Optimizing** the query hierarchy objects model in the following manner
   - Reduce verbosity and ambiguity
   - Deduplicate similar constraints into  
   - Rearrange parts of the hierarchy objects model according to some strategy
   - Infer information from prior knowledge of the domain the query is operating in (Data Schema )
   
## Internal Structure
As shown above the 4 steps of the ASG flow are 

1) accept the textual representation of the query
2) verify the correctness of the query (both structural and content)
3) generate the abstract syntax graph of objects
4) optimize the objects graph according to different strategies

### Query Validation
The [Query Validator](../../../opengraph-asg/src/main/java/org/opensearch/graph/asg/validation/AsgQueryValidator.java) return a validation assert if the query is not valid (or OK is valid).

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

#### Validation Rules

* **AsgConstraintExpressionValidatorStrategy** -
This validator verify that a constraint is properly defined - it must have corresponding operands to match the predicate operator
For example range must have two sides operands, a greater than must have a non-empty operand  - this is a **structure** check.


* **AsgCycleValidatorStrategy** -
This validator verify that a query doesn't define an internal reference cycle   - this is a **structure** check.

* **AsgEntityDuplicateEnumValidatorStrategy** -
This validator verify that a query doesn't define the same entity enumerators to different query parts - this is a **structure** check.


* **AsgEntityDuplicateETagValidatorStrategy** -
This validator verify that a query doesn't define the same entity tag to different query parts - this is a **structure** check.


* **AsgEntityPropertiesValidatorStrategy** -
This validator verify that the entity properties in the query do match according to the ontology - this is a **content** check. 


* **AsgOntologyEntityValidatorStrategy** -
This validator verify that the entities in the query do match the ontology - this is a **content** check. 


* **AsgOntologyRelValidatorStrategy** -
This validator verify that the relations in the query do match the ontology including both sides of the relationship - this is a **content** check. 


* **AsgRelPropertiesValidatorStrategy** -
This validator verify that the relation properties in the query do match according to the ontology - this is a **content** check. 

 
* **AsgStartEntityValidatorStrategy** -
This validator verify that ontology has only one start step and it is the first one - this is a **structure** check. 

  
* **AsgStepsValidatorStrategy** -
This validator verify that the query has a correct structure of Entity->Relation->Entity steps pattern - this is a **structure** check. 

 
* **AsgWhereByConstraintValidatorStrategy** -
This validator verify that WhereBy clause is properly defined - with only single whereBy constraint and correct "_By_" ontology reference - this is bot a **structure** & **content** checks. 


### Query Optimization
This part will generate the ASG Query based on the registered strategies.

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
                new RedundantPropGroupAsgStrategy(),
                new DefaultSelectionAsgStrategy(this.ontologyProvider)
        );
    }
```
These strategies are registered in an [AsgStrategyRegistrar](../../../virtualize/virtual-asg/src/main/java/org/opensearch/graph/asg/strategy/M2AsgStrategyRegistrar.java)
Each strategy addresses a different aspect and concern

#### Strategies

 - **DefaultETagAsgStrategy** 
Verify all elements have a tag - if not create one for them 

 - **AsgNamedParametersStrategy**
Handle parameterized constraint (ParameterizedConstraint) in which it replaces each parameterized value with the appropriate matched pattern

 - **UntypedInferTypeLeftSideRelationAsgStrategy**
This strategy infers the actual concrete type according to the ontology and if this type is not explicitly defined in the actual query - it adds it explicitly 
so that the execution planner can have better cost approximations for the specific types it should consider for pricing of the query. 

 - **RelationPatternRangeAsgStrategy**
This strategy finds relation pattern of the type (a:A)-[r:R 1..3]-(b:B) which it states how many steps the query is allowed to traverse.
It than replaces this with explicit patterns in which each pattern represents one of the amount of hops allowed to search
 - First a single hop : (a:A)-[r:R]-(b:B)
 - Second two hops : (a:A)-[r1:R]-(b1:B)-[r2:R]-(b2:B)
 - Third three hops : (a:A)-[r1:R]-(b1:B)-[r2:R]-(b2:B)-[r3:R]-(b3:B)
The entire traversal tree is as follows:
 - First single hop OR Second two hops OR Third three hops... 

 - **UntypedRelationInferTypeAsgStrategy**
This strategy replaces the "_all" (none specific relations) statement with each existing type - according to allowed types as they are present in the ancestor type element
This allows better understanding of the requested entities and byi adding these types explicitly the execution planner can have better cost approximations for the specific types it should consider for pricing of the query. 

 - **EPropGroupingAsgStrategy**
This strategy groups together different properties that actually belong to a specific entity into a single group so that this group will be pushed down together to the engine  

 - **HQuantPropertiesGroupingAsgStrategy**
This strategy groups together different properties that actually belong to a specific entity into a single group so that this group will be pushed down together to the engine  

 - **Quant1PropertiesGroupingAsgStrategy**
This strategy groups together different properties that actually belong to a specific entity into a single group so that this group will be pushed down together to the engine  

 - **RelPropGroupingAsgStrategy**
This strategy groups together different properties that actually belong to a specific Relation into a single group so that this group will be pushed down together to the engine  

 - **ConstraintTypeTransformationAsgStrategy**
This strategy replaces string literals with enumeration ordinal values in case the string literal belongs to an enumeration field.

 - **ConstraintIterableTransformationAsgStrategy**
This strategy Transforms multi-value predicate from an array or iterable into a list
   
 - **RedundantLikeConstraintAsgStrategy**
This strategy transforms redundant appearances of a like predicates and unifies it into a single like predicate:
 - p1:name like jerry* 
 - p2:name like *mcqueen
 would results into: like jerry*mcqueen

 - **RedundantLikeAnyConstraintAsgStrategy**
This strategy Transforms likeAny constraint into like with a list of operands as the expression value

 - **AggFilterTransformationAsgStrategy**
 - TODO - Not Implemented Yet
This strategy transforms any found aggregation expression into an aggregation constraint expression
   
 - **LikeToEqTransformationAsgStrategy**
This strategy transforms a like predicate with an exact operand into an equals predicate

 - **MultiConstraintTransformationAsgStrategy**
This strategy transforms any entity property which are defined as exact match in the mapping schema into such explicit property

Will be resolved into any one of the following:
 - SchematicEProp
 - SchematicNestedEProp
 - SchematicRankedEProp

 - **LikeConstraintTransformationAsgStrategy**
This strategy transforms like predicates on fields which have a physical ngram mapping into an explicit predicate
 - SchematicEProp

 - **LikeAnyConstraintTransformationAsgStrategy**
This strategy transforms 'likeAny' predicates on fields which have a physical ngram mapping into an explicit schematic predicate
   
 - **NestingPropertiesTransformationAsgStrategy**
This strategy transforms property nested field names (a.b.c.d) into an explicit nested property which reflects the correct physical path of each nested inner 
property according to its real mapped name.

 - **RedundantInSetConstraintAsgStrategy**
This strategy replaces the inSet predicate with an equal predicate in case the number of operand in the expression is one

 - **RedundantPropGroupAsgStrategy**
This strategy simplifies property groups and deletes intermediate empty inner groups in the group containment hierarchy

 - **DefaultSelectionAsgStrategy**
This strategy adds all default entity/relation properties to the query so that they will be part of the results