
package org.opensearch.graph.client.export.graphml;




import org.opensearch.graph.client.export.GraphWriter;
import org.opensearch.graph.model.logical.Edge;
import org.opensearch.graph.model.logical.Vertex;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.results.Assignment;
import org.opensearch.graph.model.results.Entity;
import org.opensearch.graph.model.results.Property;
import org.opensearch.graph.model.results.Relationship;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public final class GraphMLWriter implements GraphWriter {
    private final XMLOutputFactory inputFactory = XMLOutputFactory.newInstance();

    private final Optional<Map<String, String>> vertexKeyTypes;
    private final Optional<Map<String, String>> edgeKeyTypes;
    private final Optional<String> xmlSchemaLocation;
    private final String edgeLabelKey;
    private final String vertexLabelKey;

    public GraphMLWriter(final boolean normalize, final Map<String, String> vertexKeyTypes,
                          final Map<String, String> edgeKeyTypes, final String xmlSchemaLocation,
                          final String edgeLabelKey, final String vertexLabelKey) {
        this.vertexKeyTypes = Optional.ofNullable(vertexKeyTypes);
        this.edgeKeyTypes = Optional.ofNullable(edgeKeyTypes);
        this.xmlSchemaLocation = Optional.ofNullable(xmlSchemaLocation);
        this.edgeLabelKey = edgeLabelKey;
        this.vertexLabelKey = vertexLabelKey;
    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeVertex(final OutputStream outputStream, final Entity v, Rel.Direction direction) {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));

    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeVertex(final OutputStream outputStream, final Entity v) {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));
    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeEdge(final OutputStream outputStream, final Relationship e) throws IOException {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));
    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeVertices(final OutputStream outputStream, final Iterator<Entity> vertexIterator, final Rel.Direction direction) throws IOException {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));

    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeVertices(final OutputStream outputStream, final Iterator<Entity> vertexIterator) throws IOException {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));

    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeVertexProperty(final OutputStream outputStream, final Property vp) throws IOException {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));
    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeProperty(final OutputStream outputStream, final Property p) throws IOException {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));
    }

    /**
     * This method is not supported for this writer.
     *
     * @throws UnsupportedOperationException when called.
     */
    @Override
    public void writeObject(final OutputStream outputStream, final Object object) throws IOException {
        throw new UnsupportedOperationException(String.format("%s only writes an entire Graph", this.getClass()));
    }

    /**
     * Write the data in a Graph to a GraphML OutputStream.
     *
     * @param outputStream the GraphML OutputStream to write the Graph data to
     * @throws IOException thrown if there is an error generating the GraphML data
     */
    @Override
    public void writeGraph(final OutputStream outputStream, final Assignment g) throws IOException {
        final Map<String, String> identifiedVertexKeyTypes = new HashMap<>(this.vertexKeyTypes.orElseGet(() -> GraphMLWriter.determineVertexTypes(g)));
        final Map<String, String> identifiedEdgeKeyTypes = new HashMap<>(this.edgeKeyTypes.orElseGet(() -> GraphMLWriter.determineEdgeTypes(g)));

        if (identifiedEdgeKeyTypes.containsKey(this.edgeLabelKey))
            throw new IllegalStateException(String.format("The edgeLabelKey value of[%s] conflicts with the name of an existing property key to be included in the GraphML", this.edgeLabelKey));
        if (identifiedEdgeKeyTypes.containsKey(this.edgeLabelKey))
            throw new IllegalStateException(String.format("The vertexLabelKey value of[%s] conflicts with the name of an existing property key to be included in the GraphML", this.vertexLabelKey));

        identifiedEdgeKeyTypes.put(this.edgeLabelKey, GraphMLTokens.STRING);
        identifiedVertexKeyTypes.put(this.vertexLabelKey, GraphMLTokens.STRING);

        try {
            final XMLStreamWriter writer;
            writer = configureWriter(outputStream);

            writer.writeStartDocument();
            writer.writeStartElement(GraphMLTokens.GRAPHML);
            writeXmlNsAndSchema(writer);

            writeTypes(identifiedVertexKeyTypes, identifiedEdgeKeyTypes, writer);

            writer.writeStartElement(GraphMLTokens.GRAPH);
            writer.writeAttribute(GraphMLTokens.ID, GraphMLTokens.G);
            writer.writeAttribute(GraphMLTokens.EDGEDEFAULT, GraphMLTokens.DIRECTED);

            writeVertices(writer, g);
            writeEdges(writer, g);

            writer.writeEndElement(); // graph
            writer.writeEndElement(); // graphml
            writer.writeEndDocument();

            writer.flush();
            writer.close();
        } catch (Throwable xse) {
            throw new IOException(xse);
        }
    }

    private XMLStreamWriter configureWriter(final OutputStream outputStream) throws XMLStreamException {
        final XMLStreamWriter utf8Writer = inputFactory.createXMLStreamWriter(outputStream, "UTF8");
        final XMLStreamWriter writer = new GraphMLWriterHelper.IndentingXMLStreamWriter(utf8Writer);
        ((GraphMLWriterHelper.IndentingXMLStreamWriter) writer).setIndentStep("    ");
        return writer;
    }

    private void writeTypes(final Map<String, String> identifiedVertexKeyTypes,
                            final Map<String, String> identifiedEdgeKeyTypes,
                            final XMLStreamWriter writer) throws XMLStreamException {
        // <key id="weight" for="edge" attr.name="weight" attr.type="float"/>
        final Collection<String> vertexKeySet = getVertexKeysAndNormalizeIfRequired(identifiedVertexKeyTypes);
        for (String key : vertexKeySet) {
            writer.writeStartElement(GraphMLTokens.KEY);
            writer.writeAttribute(GraphMLTokens.ID, key);
            writer.writeAttribute(GraphMLTokens.FOR, GraphMLTokens.NODE);
            writer.writeAttribute(GraphMLTokens.ATTR_NAME, key);
            writer.writeAttribute(GraphMLTokens.ATTR_TYPE, identifiedVertexKeyTypes.get(key));
            writer.writeEndElement();
        }

        final Collection<String> edgeKeySet = getEdgeKeysAndNormalizeIfRequired(identifiedEdgeKeyTypes);
        for (String key : edgeKeySet) {
            writer.writeStartElement(GraphMLTokens.KEY);
            writer.writeAttribute(GraphMLTokens.ID, key);
            writer.writeAttribute(GraphMLTokens.FOR, GraphMLTokens.EDGE);
            writer.writeAttribute(GraphMLTokens.ATTR_NAME, key);
            writer.writeAttribute(GraphMLTokens.ATTR_TYPE, identifiedEdgeKeyTypes.get(key));
            writer.writeEndElement();
        }
    }

    private void writeEdges(final XMLStreamWriter writer, final Assignment<Vertex, Edge> graph) throws XMLStreamException {
        final List<Edge> edges = new ArrayList<>(graph.getRelationships());
        Collections.sort(edges, Comparator.comparing(e -> e.id(), String.CASE_INSENSITIVE_ORDER));

        for (Edge edge : edges) {
            writer.writeStartElement(GraphMLTokens.EDGE);
            writer.writeAttribute(GraphMLTokens.ID, edge.id());
            writer.writeAttribute(GraphMLTokens.SOURCE, edge.source());
            writer.writeAttribute(GraphMLTokens.TARGET, edge.target());

            writer.writeStartElement(GraphMLTokens.DATA);
            writer.writeAttribute(GraphMLTokens.KEY, this.edgeLabelKey);
            writer.writeCharacters(edge.label());
            writer.writeEndElement();

            final List<Map.Entry<String,Object>> keys = new ArrayList<>(edge.fields().entrySet());

            for (Map.Entry<String,Object> key : keys) {
                writer.writeStartElement(GraphMLTokens.DATA);
                writer.writeAttribute(GraphMLTokens.KEY, key.getKey());
                // technically there can't be a null here as gremlin structure forbids that occurrence even if Graph
                // implementations support it, but out to empty string just in case.
                writer.writeCharacters(key.getValue()!=null ? key.getValue().toString() : "Null");
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }

    private void writeVertices(final XMLStreamWriter writer, final Assignment graph) throws XMLStreamException {
        final Iterable<Vertex> vertices = graph.getEntities();
        for (Vertex vertex : vertices) {
            writer.writeStartElement(GraphMLTokens.NODE);
            writer.writeAttribute(GraphMLTokens.ID, vertex.id());

            writer.writeStartElement(GraphMLTokens.DATA);
            writer.writeAttribute(GraphMLTokens.KEY, this.vertexLabelKey);
            writer.writeCharacters(vertex.label());
            writer.writeEndElement();

            final Collection<Map.Entry<String,Object>> keys = vertex.fields().entrySet();
            for (Map.Entry<String,Object> key : keys) {
                writer.writeStartElement(GraphMLTokens.DATA);
                writer.writeAttribute(GraphMLTokens.KEY, key.getKey());
                // technically there can't be a null here as gremlin structure forbids that occurrence even if Graph
                // implementations support it, but out to empty string just in case.
                writer.writeCharacters(key.getValue()!=null ? key.getValue().toString() : "Null");
                writer.writeEndElement();
            }
            writer.writeEndElement();
        }
    }


    private Collection<String> getEdgeKeysAndNormalizeIfRequired(final Map<String, String> identifiedEdgeKeyTypes) {
        final Collection<String> edgeKeySet;
        edgeKeySet = new ArrayList<>();
        edgeKeySet.addAll(identifiedEdgeKeyTypes.keySet());
        Collections.sort((List<String>) edgeKeySet);

        return edgeKeySet;
    }

    private Collection<String> getVertexKeysAndNormalizeIfRequired(final Map<String, String> identifiedVertexKeyTypes) {
        final Collection<String> keyset;
        keyset = new ArrayList<>();
        keyset.addAll(identifiedVertexKeyTypes.keySet());
        Collections.sort((List<String>) keyset);

        return keyset;
    }

    private void writeXmlNsAndSchema(final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeAttribute(GraphMLTokens.XMLNS, GraphMLTokens.GRAPHML_XMLNS);

        //XML Schema instance namespace definition (xsi)
        writer.writeAttribute(XMLConstants.XMLNS_ATTRIBUTE + ':' + GraphMLTokens.XML_SCHEMA_NAMESPACE_TAG,
                XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);
        //XML Schema location
        writer.writeAttribute(GraphMLTokens.XML_SCHEMA_NAMESPACE_TAG + ':' + GraphMLTokens.XML_SCHEMA_LOCATION_ATTRIBUTE,
                GraphMLTokens.GRAPHML_XMLNS + ' ' + this.xmlSchemaLocation.orElse(GraphMLTokens.DEFAULT_GRAPHML_SCHEMA_LOCATION));
    }

    private static Map<String, String> determineVertexTypes(final Assignment<Entity,Relationship> graph) {
        final Map<String, String> vertexKeyTypes = new HashMap<>();
        final Collection<Entity> vertices = graph.getEntities();
        for (Entity entity : vertices) {
            for (Property prop : entity.getProperties()) {
                if (!vertexKeyTypes.containsKey(prop.getpType())) {
                    vertexKeyTypes.put(prop.getpType(), GraphMLWriter.getStringType(prop.getValue()));
                }
            }
        }

        return vertexKeyTypes;
    }

    private static Map<String, String> determineEdgeTypes(final Assignment<Entity,Relationship> graph) {
        final Map<String, String> edgeKeyTypes = new HashMap<>();
        final Collection<Relationship> relationships = graph.getRelationships();
        for (Relationship relationship : relationships) {
            for (Property prop : relationship.getProperties()) {
                if (!edgeKeyTypes.containsKey(prop.getpType())) {
                    edgeKeyTypes.put(prop.getpType(), GraphMLWriter.getStringType(prop.getValue()));
                }
            }
        }

        return edgeKeyTypes;
    }


    private static String getStringType(final Object object) {
        if (object instanceof String)
            return GraphMLTokens.STRING;
        else if (object instanceof Integer)
            return GraphMLTokens.INT;
        else if (object instanceof Long)
            return GraphMLTokens.LONG;
        else if (object instanceof Float)
            return GraphMLTokens.FLOAT;
        else if (object instanceof Double)
            return GraphMLTokens.DOUBLE;
        else if (object instanceof Boolean)
            return GraphMLTokens.BOOLEAN;
        else
            return GraphMLTokens.STRING;
    }
}

