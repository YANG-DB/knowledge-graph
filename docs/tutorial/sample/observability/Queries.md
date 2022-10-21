# Query Samples 
Presented here are different queries that demonstrate usage of different function and graph queries.

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

``````
Match (s:Span) where s.durationInNanos > 3000000  AND s.events.attributes.`exception@type`="ProgrammingError" return *
``````
Expected result 1 span documents