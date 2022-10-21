# Cursors

The cursors are the mechanism where the actual query is executed against the underlying storage database.
Cursors represent an execution process that is performed in a certain point in time.

A query may have multiple cursors that are representing the same query being run at different times.
A cursor holds an open buffer (scrolls in opensearch terminology) against the database and uses this buffer to 
consume results.

A cursor also has a RESTFULL representation in memory that allows fetching and streaming its content.

## Cursor Responsibilities

A cursor is actually a stream consumer that streams results from the database to the query consumer.
A cursor fetches a batch/stream of results which is manages as a page - a page is also a RESTFULL resource.

A page is immutable and holds a list of results that match the query. Each page contains a predefined number of results.
Once a page is created it is stored in memory with its results - take this into consideration when fetching large amount of data 

Customer can continue fetching more and more results (pages) against the cursor until the stream is entirely consumed.
One the cursor is completely consumed it can't be further streamed and is defined as closes. 

Closed cursors dictate the closing of all database buffer resources and impose no additional resource liability on the dataabse.


## Cursor Fetching Data

The cursor is responsible for fetching the data using the [TraversalCursorContext](../../../virtualize/virtual-core/src/main/java/org/opensearch/graph/executor/cursor/TraversalCursorContext.java)
This Traversal Context is composed of the next elements:

 - Client - an opensearch client that performs the actual DSL against the database,
 - GraphElementSchemaProvider  - a physical schema provider that knows the physical structure of the logical entities in the store [index-provider](Index-provider.md),
 - OntologyProvider - a logical ontology provider that knows the logical domain entities and relationships [Ontology](Ontology.md) ,
 - Traversal - a graph traversal component that allows traversing the physical graph using the Unipop framework [Unipop]()

The traversal is a mechanism the abstracts the technicality of how to featch and traverse the data in the specific form that is proprietary
to the database.

It exposes a general purpose generic graph traversal language that is decoupled from the store and allows theoretical replacement of the storage engine with
some other Graph traversal supported engine.

For additional information please view [Unipop](Unipop.md) 

## Cursor Types

Cursors may be used for different purposes and therefor must have different types to reflect such usages.

- Graph Cursor
- Table Cursor
- Traversal Cursor
- Count Cursor
- Projection Cursor

### Traversal Cursor
