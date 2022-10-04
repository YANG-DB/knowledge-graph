# Observability
This tutorial will help you get started with observability data investigation using graph queries.


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
``````
Match (s:Span) where s.events.name="exception" return *
``````
Expected result 4 span documents

``````
Match (s:Span) where s.events.attributes.`exception@type`="ProgrammingError" return *
``````
