package org.opensearch.graph.model.execution.plan.descriptors;






import org.opensearch.graph.model.descriptors.GraphDescriptor;
import org.opensearch.graph.model.ontology.*;
import org.opensearch.graph.model.resourceInfo.GraphError;

public class OntologyDescriptor implements GraphDescriptor<Ontology> {


    //region Descriptor Implementation
    public static String printGraph(Ontology ontology) {
        return new OntologyDescriptor().visualize(ontology);
    }

    @Override
    public String visualize(Ontology ontology) {
        Ontology.Accessor accessor = new Ontology.Accessor(ontology);

        StringBuilder sb = new StringBuilder();
        // name
        sb.append("digraph G { \n");
        //left to right direction
//        sb.append(" \t rankdir=LR; \n");
        //general node shape
        sb.append(" \t node [shape=Mrecord]; \n");
        sb.append(" \t node [style=filled]; \n");

        //iterate over the entities
        ontology.getEntityTypes().forEach(e -> printEntity(accessor, sb, e));
        //iterate over the relations
        ontology.getRelationshipTypes().forEach(r -> printRelation(accessor, sb, r));
        //print enums
        ontology.getEnumeratedTypes().forEach(enm -> sb.append(printEnum(accessor, enm)));

        sb.append(" \n\t }");
        return sb.toString();
    }


    private void printEntity(Ontology.Accessor ontology, StringBuilder sb, EntityType entityType) {
        sb.append(" \n ");
        sb.append(" \t" + entityType.geteType() + " [ shape=octagon, label=\"" + entityType.geteType() + "\", fillcolor=lightblue] ");
        sb.append(printProps(ontology, entityType.geteType(), entityType));
        sb.append(" \n ");
    }

    private void printRelation(Ontology.Accessor ontology, StringBuilder sb, RelationshipType relationshipType) {
        sb.append(" \t" + relationshipType.getrType() + " [ shape=rarrow, label=\"" + relationshipType.getrType() + "\", fillcolor=darkkhaki] ");
        sb.append(printProps(ontology, relationshipType.getrType(), relationshipType));
        relationshipType.getePairs()
                .forEach(pair -> sb.append(" \t " + pair.geteTypeA() + "->" + relationshipType.getrType() + "->" + pair.geteTypeB() + "\n"));
        sb.append(" \n ");
    }

    private String printEnum(Ontology.Accessor ontology, EnumeratedType enm) {
        //add subgraph for the entire quant
        StringBuilder prpoBuilder = new StringBuilder();
        prpoBuilder.append(" \n subgraph cluster_enum_" + enm.geteType() + " { \n");
        prpoBuilder.append(" \t color=darkorchid1; \n");
        prpoBuilder.append(" \t node [fillcolor=darkolivegreen1, shape=component]; \n");
        //enum values
        prpoBuilder.append(" \t " + enm.geteType() + " [fillcolor=darkolivegreen1, shape=folder]; \n");

        //give specific number to each property in the group
        for (int i = 0; i < enm.getValues().size(); i++) {
            prpoBuilder.append(" \t " + enm.geteType() + "->" + enm.getValues().get(i).getName() + "\n");
        }

        removeRedundentArrow(prpoBuilder);
        prpoBuilder.append(" \n } \n");
        return prpoBuilder.toString();

    }

    private static String printProps(Ontology.Accessor ontology, String type, BaseElement element) {
        //add subgraph for the entire quant
        StringBuilder prpoBuilder = new StringBuilder();
        prpoBuilder.append(" \n subgraph cluster_Props_" + type + " { \n");
        prpoBuilder.append(" \t color=green; \n");
        prpoBuilder.append(" \t node [fillcolor=khaki3, shape=component]; \n");
        //give specific number to each property in the group
        for (int i = 0; i < element.getProperties().size(); i++) {
            String propName = element.getProperties().get(i);
            prpoBuilder.append(" \t " + type + "_" + propName + "[fillcolor=" + primitiveColor(getProp(ontology, propName)) + ", label=\"" + propName + "\" ]\n");
            prpoBuilder.append(" \t " + type + "->" + type + "_" + propName + possibleArrowToType(ontology, getProp(ontology, propName)) + "\n");
        }

        removeRedundentArrow(prpoBuilder);

        prpoBuilder.append(" \n } \n");
        return prpoBuilder.toString();
    }

    private static String possibleArrowToType(Ontology.Accessor accessor, Property prop) {
        Ontology.Accessor.NodeType nodeType = accessor.matchNameToType(prop.getpType()).get()._1();
        switch (nodeType) {
            case PROPERTY:
            case RELATION:
                return "";
            case ENUM:
            case ENTITY:
                return "->" + prop.getType();
        }
        return "";
    }

    private static Property getProp(Ontology.Accessor ontology, String propName) {
        return ontology.properties().stream().filter(p -> p.getName().equals(propName)).findFirst()
                .orElseThrow(() -> new GraphError.GraphErrorException("Ontology missing the next property ", "No property found named " + propName));
    }

    private static void removeRedundentArrow(StringBuilder builder) {
        if (builder.toString().endsWith("->"))
            builder.delete(builder.toString().length() - 2, builder.toString().length());
    }

    private static String primitiveColor(Property type) {
        switch (type.getType()) {
            case "int":
                return "salmon";
            case "string":
                return "skyblue";
            case "text":
                return "plum1";
            case "date":
                return "turquoise";
            case "datetime":
                return "slategray1";
            case "geo_point":
                return "yellow3";
            case "array":
                return "ivory";
        }
        return "white";
    }


}
