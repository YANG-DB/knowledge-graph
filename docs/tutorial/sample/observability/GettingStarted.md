# Observability
This tutorial will help you get started with observability data investigation using graph queries.

## Index Template Creation
In order to support strongly type query capability we need to enforce a strongly structured index mapping.

We currently have two options doing so:
 - Auto generation from a logical schema ontology
 - Reverse construction of the index-provider configuration file according to the existing index mapping

In our example we are following the second option since current customer already have existing indices with pre-configured mapping
For simplicity we have provided the Observability index provider configuration file which is the product of this reverse constructions.

Please create the index templates using the attached mapping template which are in correlation with the index-provider configuration and reflects the
actual index structure accordingly.

Once the index templates mapping is created we can create the 3 relevant indices and start ingesting observability data.


## Data Fabrication
Once our indices are created we can proceed and start loading sample datasets to our indices. 

We will load 3 type of datasets:
 - Spans
 - Logs
 - Services

Please follow the instruction in the [data-fabrication](data-fabrication.md) document


## Sample Queries

### Logs
``````
Match (l:Log) where l.machine.os ="win 7" return * 
``````
Expected result 2814 logs documents

``````
Match (l:Log) where l.machine.os ="ios" return * 
``````
Expected result 2737 logs documents


### Spans
``````
Match (s:Span) where s.status.code=2 return * 
``````
Expected result 41 span documents

``````
Match (s:Span) where s.attributes.component="mysql" return *
``````
FIX ()
``````
Match (s:Span) where s.events.name="exception" return *
``````
Expected result 4 span documents

``````
Match (s:Span) where s.events.attributes.`exception@type`="ProgrammingError" return *
``````
Expected result 2 span documents