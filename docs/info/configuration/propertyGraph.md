Elements of the Property Graph
-------------
## Intro
A [property graph](https://en.wikipedia.org/wiki/Graph_property) is a graph data structure that consists of a set of vertices (also known as nodes) and edges.
Each [vertex] represents an entity (such as a person, place, or thing) and can have a set of properties (key-value pairs) associated with it.
Each [edge] represents a relationship between two vertices and can also have a set of properties associated with it.

The [ontology](https://en.wikipedia.org/wiki/Ontology_(computer_science)) of a property graph refers to the structure and organization of the vertices and edges within the graph.
In particular, it refers to the way in which the vertices and edges are connected to one another.

A schema is a way of specifying the structure and organization of a property graph, including the types of vertices and edges that can exist within the graph and the properties that can be associated with them.

An **Ontological Query Language** (OQL) is a tool used to retrieve and manipulate data from a property graph. OQL is a declarative language that allows users to specify the patterns they are interested in finding within a property graph.

For example, a user might use OQL to find all the vertices in the graph that represent people and have a specific property (such as a name or age), or to find all the edges in the graph that represent friendships between people.

**Operations**
OQL includes a number of different operations that can be used to manipulate and retrieve data from a property graph.

[Constraints](): This operation allows users to specify conditions that must be met in order for a vertex or edge to be included in the results of a query. For example, a user might use the Filter operation to find all the people in the graph who are older than a specific age.

[(RETURN) As](): This operation allows users to specify the data that they want to retrieve from the graph. For example, a user might use the '(RETURN) As'  operation to retrieve the names of all the people in the graph who are friends with a specific person.

[Quantifier](): This operation allows users to specify additional pattern of vertices and edges that they want to find in the graph in addition to an existing pattern which is to be continued by the patterns following the quantifier.

There are additional operations and features available in OQL, and it is a powerful tool for working with property graphs. 

