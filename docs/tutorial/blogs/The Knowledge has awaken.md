# The rise of Knowledge Graphs

### A story of knowledge and power...

This post is the first in a series of posts related to **Graph databases**, **Knowledge graphs** and their realization on top of **OpenSearch**.

It will cover the ideas behind the graph structure used in modern data & search platforms
It will explain the ontology and taxonomy concepts and how they interact.
It will review the importance of knowledge in a modern Search Engine
Lastly it will inform of a new exciting integration for Graph capabilities on OpenSearch with YangDb.


## What Are Graph - And Why Should I Care ?

**Introduction :**

The concept of entities and relations between these entities is nothing new in the database industry.
Any relational database has this notion in the form of tables (entities) and foreign keys (relations between entities) almost from the beginning.

This relational database store was developed in the 1970s to help enterprises store structured information. A Relational databases stores the different types of tables and relations between them as a schemas.

*So how is that related to a “**Graph**” ?*

This definition of a “Graph” actually arrived from the mathematical definition of Graph -   [graph theory](https://en.wikipedia.org/wiki/Graph_theory):

“A ***graph** is a structure amounting to a [set](https://en.wikipedia.org/wiki/Set_(mathematics)) of objects in which some pairs of the objects are in some sense "related".*
*The objects correspond to mathematical abstractions called [vertices](https://en.wikipedia.org/wiki/Vertex_(graph_theory)) (also called nodes or points) and each of the related pairs of vertices is called an edge (also called link or line).“*

This mathematical definition is a very broad definition which is hard to work with in the well-structured schematic world of relational database .

## Structured & Semi Structured Data

The database world of storage and search is often derived from real world problems - in different domains.
Banking, Insurance, E-Commerce, Telecom and more - all these different domains have a schema.

Aa mentioned earlier, schema is a set of entities with properties and relations that describe the world in which they operate - banking has accounts and clients, Insurance has policies and risks and so on...


There are cases where we haven't defined the complete structure for all our entities or that our entities are gradually  evolving.  During this evolution we may have semi-structured elements that are partially structured and partially unstructured.

This ‘semi-structured’ concept is something that a relational store is not ‘happy’ to work with - the strongly typed nature is a very fundamental concept which is hard to bypass.

Another interesting phenomena is the mixing of different domains into one storage - an organization can have its business domain schema side by side to its operational schema and its security schema and so on.

To make this even more complicated, in some use cases - the same entity can be present in both schemas, each schema will relate to that entity within its own types and definitions.

Due to the rigidity of the traditional relational storage, the above use cases would have to be partitioned into different schemas which are hard to query together and also un-natural to combine .


## Graph DB - To the rescue

These problems are not new - they have been around since the beginning, but the exponential growth of data made their impact tremendous.

The realization that relational databases are not a one store solution has become clear during the recent years.
As a consequence the **NoSQL** has emerged and gained dominance with particular focus on the nature of the data -

Specific sore types for different cases such as

* Big Table
* Documents
* Key/Value
* Columnar
* Graph


Graph database is yet another type of NoSQL databased -  it is a technology for data management designed to handle very large sets of structured, semi-structured or unstructured data.

The Capabilities for a graph database to represent logical entities and relationships in a less constrained and rigid way.
Entities & relationships can be labeled with multiple types and have varying number of fields.

**According to Gartner:**

**“By 2025, graph technologies will be used in 80% of data and analytics innovations, up from 10% in 2021, facilitating rapid decision making across the enterprise.”**

***- Gartner "Market Guide: Graph Database Management Solutions" Merv Adrian, Afraz Jaffri 30 August 2022***

--------------------------------------------------------------------------------------------------------------------------------------------------------

## Graph DB - Storage & Process

Essentially every database (whether graph or relational) has these two main elements:

* Storage
* Process



### **Graph storage**

Commonly refers to the structure of the database that contains raw data.

In a graph, the storage layer is optimized for graphs in many aspects, ensuring that data is stored efficiently, keeping nodes and relationships close to each other in the actual physical layer.

We often see that graph databases are differentiated by term “native” / “non-native”.

Graph storage is classified as *non-native* when the storage comes from an outside source, such as a relational, columnar or any other type of database (most cases a NoSQL store is preferable)

Non-native graph databases usually comprise of existing relational, document and key value stores, adapted for the graph data model query scenarios.

A Native Graph Database has the storage layer optimized for the capabilities of traversing from a Node to its immediate neighbour - this is done by storing the node’s edges with proximity to the node itself. This allows the traversing (hopping) between connected nodes  efficient and fast.


### **Graph Processing**

Includes accessing the graph, traversing the vertices & edges and collecting the results.

A traversal is how you walk on the graph structure, navigating from starting nodes to related nodes, following relationships according to some rules (mostly filters).

Finding answers to questions like "what music do my friends like that I don’t yet own?"

Another type of graph processing are graph algorithms that arrive from the land of graph theory:
- Find path between vertices
- Calculate connectivity of vertices in the graph
- Calculate optimal routes between vertices according to weight given on edges

These type of graph processing algorithms are commonly used in many domains (security, monitoring, risk assessments, billing...) and have become an essential part of the daily business activities .


## Graph Models

In order to perform queries on the data - we need to create some structure to represent the basic elements - entities and relationships.

One of the more popular models for representing a graph is the Property Model.

### Property model

This model contains connected entities (the *nodes*) which can hold any number of attributes (key-value-pairs).

### Nodes

Nodes have a unique id and list of attributes to represent their features and content.
Nodes can be marked with labels representing their different roles in your domain. In addition to relationship properties, labels can also serve metadata over graph elements.

Nodes are often used to represent *entities* but depending on the domain relationships may be used for that purpose as well.

### Relationships

Relationships are represented by the source and target node they are connected by, and in case of multiple connections between the same vertices – having additional label of property to distinguish (type of relationship)

Relationships organize nodes into arbitrary structures, allowing a graph to resemble a list, a tree, a map, or a compound entity — any of which may be combined into yet more complex structures.

Very much like foreign keys between tables in the relational model, In the graph model relationship describes the relations between the vertices.

One major difference in this model (compared to the strict relational schema) is that this schema-free structure enables:  -
- adding / removing relationship between vertices without any constraints.
- adding / removing entity labels without any constraints.
- adding / removing entity properties without any constraints.

--------------------------------------------------------------------------------------------------------------------------------------------------------

Additional graph model is the Resource Description Framework (RDF) model.

### RDF model

At the core of RDF is this notion of a triple, which is a statement composed of three elements that represent two vertices connected by an edge.

It’s called *subject-predicate-object:*

* Subject will be a resource, or a node in the graph.
* Predicate will represent an edge – a relationship.
* Object will be another node or a value.

Resources (vertices/literal values) and relationships (edges) are identified by a URI, which is a unique identifier.

This means that neither nodes nor edges have an internal structure; they are purely a unique label.

When representing data in RDF with triples, we’re breaking it down to the maximum. Completely stripping the data, and we end up finding nodes in the graph that are resources and literal values.

This is a difference from the property model which allows attributes to reside on the graph elements themselves.

This mechanism for describing resources is a major part in the W3C's Semantic Web activity - a modern representation of the World Wide Web in which automated software can store, exchange, and use machine-readable information distributed throughout the Web.



## A Query Language - Traversing & Filtering

### Traversal

A traversal is how you travel over the graph, navigating from starting nodes to related nodes. Traversing a graph means visiting its nodes, following relationships according to some rules. In most cases only a subgraph is visited.

The result of traversing is a projection of the resulted sub-graph. The results can take a tabular form, a sub-graph form or a list of paths - (list of graph elements).

### Query Filters

Most of the time, graph searching involve some type of filter on property values – whether edge property or relation property.

During the traversing of the graph - nodes or relationships which do not comply with the given filter will not be traversed.

For a Graph database to be able to efficiently filter search on a large-scale graph, an index must be created for each property type.

For data that includes Text / Date / Range / Geo-Search - usually a dedicated index is created for each field type. This index will be the most efficient in terms of search time.

Combining different search criteria introduce different ways to traverse the graph, therefore some preliminary planning is needed to smartly select the best filter order for executing the query.


## Modern Graph Query Languages

The graph DB industry developed different graph query languages that query a graph according to its physical native storage model ( Property / RDF ).

### Open Cypher - PropertyGraph Query Language

This graph language was invented and contributed to the community by [Neo4J](https://github.com/neo4j/neo4j) graph DB company.
Open Cypher is an open source declarative, SQL-inspired language for describing patterns in graphs visually using an ascii-art syntax.

It allows us to state what we want to select, insert, update or delete from the graph data without requiring to describe exactly how to do it.

Queries are built up using various clauses. Clauses are chained together, and they feed intermediate result sets between each other.

For example, the matching variables from one `MATCH` clause will be the context that the next clause exists in.

The query language is comprised of several distinct clauses.

` - MATCH`:   The graph pattern to match. This is the most common way to get data from the graph.
` - WHERE`:   Not a clause in its own right, but rather part of `MATCH`, `OPTIONAL MATCH` and `WITH`. Adds constraints to a pattern, or filters the intermediate result passing through `WITH`.

` - RETURN`: What to return.

In addition to the clauses It has two fundamental components - Nodes (vertices) and Edges (relationships)

### Vertices

Cypher uses ASCII-Art to represent patterns, surround nodes with parentheses which look like circles, e.g. (node).

To Refer the node, we’ll give it a variable such as (p) for person or (t) for thing. If the node is not relevant to your question, you can also use empty parentheses ().

Each vertical may be related to none, one or many types (labels) and we instruct this using the colon ‘:’ operator

` MATCH (p:Person) `
Or
` MATCH (p) WHERE p:Teacher OR p:Student`

### Relationships

Relationships are basically an arrow → between two nodes.

Additional information can be placed in square brackets inside of the arrow.

* relationship-types *like* `-[:KNOWS|:LIKE]->`
* a variable name` -[rel:KNOWS]→` before the colon
* additional properties` -[{since:2010}]->`
* structural information for paths of variable length` -[:KNOWS*..4]->`



### Properties

Properties are the fields for both vertices and relationships. They can be filtered using the ***WHERE*** clause

```
MATCH (n:Person) WHERE n.age < 30
RETURN n.name, n.age
```



Lets examine the next query:

`MATCH (p1:Person)-[rel:Comment]→(p2:Post) `
`WHERE [rel.date](http://rel.date/) > {dateValue}`
`RETURN p1, rel, type(rel)`


The above query describes the next pattern:

*Find a **Person** - we will tag him as ‘p****1****’*
*- **Comments** - the person has a relationship of type Comment, we will tagged as ‘****rel****’*
*- **Post** – the comment is connected to a post, we will tag it as ‘p****2****’*

The relation tagged as `‘*rel’*` must follow the date constraints meaning the person commenting on a post must have done it after the given date.

This cypher example shows the simplicity of using such declarative traversing language and how closely it resembles the spoken language.

--------------------------------------------------------------------------------------------------------------------------------------------------------

## The Knowledge  - What is an  Ontology ?

Finally arriving to the Knowledge part - where the actual schema and domain information is present.
The term Ontology is a common description of the actual meaning of the entities in a domain and their relationships with each other.

Ontology are semantic data models that define the **types** of things that exist in a domain and the **properties** that can be used to describe them.

Ontologies are *generalized* data models, meaning that they only model *general* types of things that share certain properties, but don’t include information about *specific* individuals in our domain (class vs instance)

For example, instead of describing an entity instance - for example a dog named fluffy and all of its individual characteristics, an ontology should focus on the general concept of *dogs*, trying to capture characteristics that most/many dogs might have.
Doing this allows reusing the ontology to describe additional dogs in the future.

There are three main components to an ontology, which are usually described as follows:

* **Classes:** the distinct types of things that exist in the data.
* **Relationships:** properties that connect two classes.
* **Attributes:** properties that describe an individual class.

### So What is a knowledge graph ?

Using the ontology as a prototype , data can be added for each individual instances to create a **knowledge graph****.**
A knowledge graph is created when you apply an ontology (semantic data model) to a set of individual data points (the specific domain data).

**- ontology + data = knowledge graph**

### Taxonomy

In Many occasions the term taxonomy is apparent with regard to ontology, so what is Taxonomy ?

Taxonomies provide the terms or categories that a given entity can be described by, and often also describes one or more orthogonal dimensions that provide narrower or broader classification.

A _**cat**_ *is a* _**carnivore**_ *is a* _**mammal**_ *is a* _**chordate**_ *is an* _**animal** _ each of these represents some form of class or clade (family).

The role of a taxonomist, in general, is to determine what particular class a given entity (such as an animal or book) most clearly falls into.  If this sounds like the work of a librarian, it should be since one of the primary roles that a librarian has is to classify new books into a taxonomy.

One of the central notions of any classification system is to minimize ambiguity, or minimizing the number of buckets that a given resource can be put into, preferably to the point where there are no overlaps.

This is one reason that hierarchies are popular classification tools, and why most knowledge systems ultimately tend to utilize some form of hierarchy.

To conclude this short distinction - Ontology deals with specific domain classes, relationships and attributes while taxonomy deals with the categorization of the classes and relationships in a hierarchical manner.

## Knowledge Graph is a Search Enhancement

In recent years, structured data is a first-class citizen among search results. The main search engines (Google.com, Yahoo.com, [Bing.com](http://bing.com/)) make significant efforts to recognize when a user’s query can be answered using structured data. In parallel, they are investing significant resources in building a curated database of facts extending knowledge bases like Freebase.

These databases contain Knowledge that model a broad range of topics of interest.

Let's consider Freebase, It has data is stored in a Knowledge Graph data structure . This structure is composed of nodes connected by edges. The nodes are defined using [/type/object](http://www.freebase.com/type/object) and edges are defined using [/type/link](http://www.freebase.com/type/link).

By storing the data as a property graph, Freebase can quickly traverse arbitrary connections between topics and easily add new schema without having to change the structure of the data.

Freebase has over 39 million topics about real-world entities like people, places and things.

As discussed  in the query section in this document, a query is composed of a list of labels (types) that represent real world entities from a specific domain (examples for such are banking, insurance or E-Commerce).

The entities are related to each other using edges that describe real relationships between the entities in that specific ontology. Lastly the filters define the specifications of the entities/relations fields we want to narrow the search with.

The search engine may also apply none-graph search techniques such as search of words proximity or uniqueness.

These ‘standard ’ none-graph search patterns are used when parts of the data is unstructured or semi-structured.
These search techniques may return better results in some cases due to poor data variety and distribution.

The fusion of both the knowledge based graph search and the ‘standard’ search may yield better performance with regards to search accuracy and relevancy.

## Community Driven Ontologies

As the field of Knowledge has effectively gain the upper hand with relation to the search relevancy and accuracy, many existing data & search engines have adopted its concepts and structures as a core part of their capabilities.

As described earlier, the Freebase search engine uses its own catalog of domain Knowledge. The open source community has also begun a large effort to methodically catalog and organize the common domains into general purpose ontologies to be later used by search engines and data platforms.

*For example:*

#### Biology

* [Gene Ontology (GO)](http://geneontology.org/) - The world’s largest source of information on the functions of genes.
* [Uberon](http://obofoundry.org/ontology/uberon) - All body parts across all animals.

#### Commerce

* [GoodRelations](http://www.heppnetz.de/projects/goodrelations/) - The Web vocabulary for e-commerce.
* [Product Types Ontology](http://www.productontology.org/) - High-precision identifiers for product types based on Wikipedia.

#### Community

* [SIOC (Semantically Interlinked Online Communities)](http://sioc-project.org/) - An ontology of terms that can be used to describe online communities on the Web of Data.

#### Culture

* [CIDOC CRM (Conceptual Reference Model)](http://www.cidoc-crm.org/) - An ontology for cultural heritage information, which describes the explicit and implicit concepts and relations relevant to the documentation of cultural heritage.

#### Food and Drink

* [Wine Ontology](https://www.w3.org/TR/owl-guide/wine.rdf) -
* [Food Ontology](https://www.w3.org/TR/2004/REC-owl-guide-20040210/food.rdf) -
* [FoodOn](https://foodon.org/) - An ontology built to interoperate with the OBO Library and to represent entities which bear a "food role".

#### Geography

* [Geographical Entity Ontology](http://www.obofoundry.org/ontology/geo.html) - An ontology of geographical entities implemented in OWL 2 and based on Basic Formal Ontology (BFO).

#### Humanities

* [CLARIAH/awesome-humanities-ontologies](https://github.com/CLARIAH/awesome-humanities-ontologies) - A curated list of ontologies for Digital Humanities.

#### Legal

* [Liquid-Legal-Institute/Legal-Ontologies](https://github.com/Liquid-Legal-Institute/Legal-Ontologies) - A list of selected resources, methods, and tools dedicated to legal data schemes and ontologies.

#### News

* [rNews](http://dev.iptc.org/rNews) - An approved standard for using semantic markup to annotate news-specific metadata in HTML documents.

#### People

* [Friend Of A Friend (FOAF)](http://www.foaf-project.org/) - A computer language defining a dictionary of people-related terms that can be used in structured data.
* [vCard Ontology](https://www.w3.org/TR/vcard-rdf/) - An ontology for describing people and organizations. A mapping of the vCard specification (RFC6350) to RDF/OWL.


These samples ontologies are domain driven, we should also mention the most general purpose community driven projects for ontologies

* https://schema.org/
* https://www.wikidata.org/wiki/Wikidata:Database_reports/EntitySchema_directory


The capabilities of the **Knowledge Graph** to take a (community driven) domain specific ontology / ontologies  and allow data to be ingested, organized and queried accordingly - make these ontology a valuable and useful tool for many business use-cases.


## OpenSearch & Knowledge Graph

Now that we’ve scratched the surface of the Knowledge Graph and discussed the importance of the ontology and how it helps the search relevancy and accuracy - let’s see how all of this becomes a reality for the [OpenSearch](https://opensearch.org/) engine.


### YangDB - Knowledge Graph Engine

During the past 6 years I’ve been working on [YangDB](https://github.com/YANG-DB/yang-db) - an Open Source Knowledge Graph engine on top of OpenSearch and Elasticsearch.

This framework offers:
-  Logical Ontology Definition and Language
-  Physical  Index Driven Definition and mapping
-  Support Multiple Graph Query Languages (***Cypher, GraphQL***)
-  Allow Graph Traversals on top of Indices (***Unipop***)
-  Execution Planner for complex and heavily compute queries

These capabilities position YangDb as a Knowledge Graph Layer that allows users to utilize their domain specific ontology for both ingestion, organization and searching their data.

Search relevancy and accuracy gain a significant increase due to the Knowledge the domain imposed on top of the data.

In addition to that, combination of non-structured search (text search for example) and aggregations on top of the Knowledge Graph brings the added value of data classification and categorization.

**The Knowledge Layer**

The goal of integrating this framework into OpenSearch will be in the following areas:

- **Creation of a Catalog which can be used by many consumers - both external and internal**
    - The[SQL Query](https://github.com/opensearch-project/sql) Planner can use the catalog to derive validation strategies
    - The [Data Ingestion](https://github.com/opensearch-project/data-prepper) can use the catalog to derive understand the data structure and create real time ingestion rules

- **Delivering a Clear and Complete Ontology for the [Observability Domain](https://github.com/opensearch-project/observability)**
    - This will allow to create intelligent **Service Level Objectives** for the organization
    - Investigation of **incidents** and monitor errors
    -  **Correlate** different log sources into a single clear traceable flow

- Describe rules and patterns to match for [security](https://github.com/opensearch-project/security-analytics) collected logs and data (https://d3fend.mitre.org/dao/)

- Infer hidden relationships between different logical entities using ML techniques

### To Be Continued...

*The next post will discuss*
*- The internals of YangDb*
*- The inherent complexities of implementing a graph on top of an Index Based search engine*
*- How its planned integration with OpenSearch organization will bring the Knowledge value to all the search engine user’s.*


**--------------------------------------------------------------------------------------------------------------------------------------------------------**
**Author:**
*[Lior Perry](https://www.linkedin.com/in/lior-perry-62135314)is one of the founders and contributor of [YangDB](https://github.com/YANG-DB/yang-db)  and has worked for the past 10 years on Big-Data Knowledge related projects.*

