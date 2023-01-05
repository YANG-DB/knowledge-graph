# Observability Use Case
This document describes the needed steps to integrate open-search graph solution with the current Observability solution.
The purpose of the integration will be to demonstrate the Observability investigation use case in which end users will use a graph query (language/UX)
to discover and traverse on Observability signals.

### Required Achievements
During the investigation phase they will be able to:

 - Describe investigation patterns such as: Log->event(network)->with(host)->during(time)->access(destination)
 - Observe the resulting graph elements which comply with the Observability schema structure
 - Explore the graph elements by continuing discovering connected edges 
 - Preserve Interesting research patters and template graph queries
 - Register for notifications (alerts) on Threshold based graph queries. 
 - Create materialized view based on interesting Graph Queries results 

### Work Assumptions
The following assumptions for the first release are taken:

 - open-graph repository will not be part of the opensearch project
   - May need to create an 'opensearch-lab' / 'opensearch-experimental' space for such beta status projects 
 - open-graph will remain a non-plugin execution engine which is using opensearch via HTTP transport client
 - open-graph will remain a multi-module maven based project
 - open-graph dependency graph will not change drastically
 - open-graph test coverage will not increase drastically
 - open-graph only available API will be the Query-Endpoint (Cyhphe/GraphQL/OQL)
 - 


## The next tasks are necessary to fulfill the described achievements

### Documentation Related - Tasks

- Create Observability open-graph installation guide
- Create Observability open-graph data ingestion guide
- Create Observability open-graph usage tutorial
- Create Observability open-graph client API usage tutorial

### Code Related - Tasks

- <s> Create Observability domain </s> 
- <s> Create Observability ontology & index-provider </s>
- <s> Enable embedded-types queries for Observability </s> 
- Add security tokens to be part of the API

----------------------------------------------------------------------------------------------------------------

- Enable client API including
  - security
  - compression
  - fault tolerance
- Enable nested-types queries for Observability
- Add compression for query response bulk

### Code Related - Tests

- Fix Integration Tests to work with Opensearch Test artifact 
- Add Observability Domain specific IT
- Add Observability coverage metric

### Release - Tasks
 - Disable all external API part from Query (including swagger)
 - Remove Dragons domain from project
 - Perform Op/Sec review and fix needed CVE / findings
 - Create Observability open-graph docker compose bundle
