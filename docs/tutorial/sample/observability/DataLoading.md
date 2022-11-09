# Data Fabrication
For creating this tutorial we used the following data playground from the open search observability dashboard 
sample applications.

 - https://opensearch.org/docs/1.3/observability-plugin/log-analytics/

The sample logs are added here under the data folder ready to be ingested into open search

We can use JQ json command manipulator to fix/update the sample data to fit our needs for the observability index

https://stedolan.github.io/jq/

Here is the relevant script:

* log data
````
 cat logs.json | jq -c '. += {"type":"Log"} | .geo +=  {"type":"Geo"} | .machine +=  {"type":"Machine"} | .event +=  {"type":"Event"} ' > typed_logs.json 
 cat typed_logs.json | jq -c '{"index": {}}, . ' > bulk_logs.json
 curl -XPOST "localhost:9200/observability-logs/_bulk?pretty&refresh" -H "Content-Type: application/json" --data-binary "@bulk_logs.json"
````

* span data
````
 cat spans.json | jq -c '.[] | ._source  ' > fix_spans.json
 cat fix_spans.json | jq -c '.+=  {“type”:”Span”} ' > typed_spans.json
 cat typed_spans.json | jq -c '{"index": {}}, . ' > bulk_spans.json
 curl -XPOST "localhost:9200/observability-spans/_bulk?pretty&refresh" -H "Content-Type: application/json" --data-binary "@bulk_spans.json"
````

* service data
````
 cat services.json | jq -c '.[] | ._source  ' > fix_services.json
 cat fix_services.json | jq -c '.+=  {“type”:”Service”} ' > typed_services.json
 cat typed_services.json | jq -c '{"index": {}}, . ' > bulk_services.json
 curl -XPOST "localhost:9200/observability-services/_bulk?pretty&refresh" -H "Content-Type: application/json" --data-binary "@bulk_services.json"
````
