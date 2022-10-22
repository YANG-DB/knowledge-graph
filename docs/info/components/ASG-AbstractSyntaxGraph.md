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
                new RedundantInRangeConstraintAsgStrategy(),
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



