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