# Observability Schema

This document represents a proposal for enabling supporting a structured convention for the Observability realm:

### Overview

**Observability**:

“Observability is a measure of how well internal states of a system can be inferred from knowledge of its *external
outputs*”.
In a monolithic system, observability is as simple as traditional monitoring. However, modern distributed applications
are mostly microservice based with third-party softwares involved.
This makes observing its status externaly quite challenging.

### Telemetry

All 'external' outputs are referring to telemetry, including time series logs, metrics and distributed traces.
There are many popular instrumentation API/SDKs, collectors as well as backends for analysis and visualization, such as
ZipKin, Jaeger and Prometheus.

### Data Model

Data model serves as a bridge between the telemetry collecting and visualization. It matters for both developers and
users of these components to declare a uniform data model.
The goal is to understand the role data models play in observability and what kind of data model required in our case.

### The 3 pillars

Supporting the 3 pillars of Observability will require supporting the next schema models:

- **Logs Data Structure**
- **Traces Data Structure**
- **Metrics Data Structure**

---

### Why is it necessary

The main advantages of adopting a structured schema for observability are clear :

- data normalized to a common denominator with agreed structure
- simplify the pre-proccessing / ingestion steps into a unified model
- allow standard data exploration regardless of the input source
- simplify dashboard buildup and customization
- allow a simple & unified logs correlation
- allows to generate a wide community rules and reports that can be shared across customers and tenants
- allow auto-generating an IDL schema (such as graphql https://graphql.org/learn/schema/) to build strongly typed DSL
  code in multiple languages

### Current State

As of today, the Open Telemetry (CNCF) project is supporting a schema model for these pillars, yet there are drawbacks
to the current model.

- Experimental state (lack of maturity)
- Lack of Support for different type of events
-

### Prior Art

- **ECS**
    - https://github.com/elastic/ecs

- **cloudevents** -
    - https://github.com/cloudevents

- **OTEL**
    - https://github.com/open-telemetry/opentelemetry-specification

## How to move forward

Adopting the best of all worlds will allow us to bridge the current gap of the log structured schema

### Logs Events Schema

* Adoption of ECS

### Traces Events Schema

* Adoption of OTEL ()

#### Basic common structure
- dynamic traces
- predetermined traces

### Metrics Events Schema

* Adoption of OTEL

#### Basic common structure
- dynamic metrics
- predetermined metrics

This step will include the adoption of the open-source ECS standard for representing different log types events
This support will come into shape in the form of several different observability indices that will form the
building-blocks of the observability logs store.


## Adoption of Schema dedicated language
Choosing a general purpose structured capable semantic definition language will be a significant task in the process
of evolving the observability domain into maturity.

## Appendix

1. *ECS*
    1. Motivations: https://www.elastic.co/blog/easier-observability-with-the-elastic-common-schema
    2. Products adopt ECS: https://www.elastic.co/guide/en/ecs/current/ecs-products-solutions.html
    3. Benefits: https://www.elastic.co/guide/en/ecs/current/ecs-faq.html#ecs-benefits
    4. Custom fields: https://www.elastic.co/guide/en/ecs/current/ecs-custom-fields-in-ecs.html
    5. ES mapping template: https://github.com/elastic/ecs/blob/master/generated/elasticsearch/7/template.json
    6. SPAN - https://www.elastic.co/guide/en/apm/guide/8.3/data-model-spans.html

2. *GraphQL*
   1. https://github.com/google/rejoiner

