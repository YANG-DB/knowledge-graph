# Graph Functions
Graph function describe a dedicated API which answers specific graph related funtionality such as:

### GetNeighbors
Get the given node neighbors - its immediate nodes which it is connected to with a single edge ### GetNeighbors

### FindPath
Find a path between two given nodes  - attempts to find a path (via connected edges) between two nodes with a given max hopes between
the nodes to limit the search time and volume. 

This API also accepts predicates on the types of nodes / edges it is allowed to hope 
