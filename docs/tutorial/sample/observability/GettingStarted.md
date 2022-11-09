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


## Data Loading
Once our indices are created we can proceed and start loading sample datasets to our indices. 

We will load 3 type of datasets:
 - Spans
 - Logs
 - Services

Please follow the instruction in the [data-loading](DataLoading.md) document

