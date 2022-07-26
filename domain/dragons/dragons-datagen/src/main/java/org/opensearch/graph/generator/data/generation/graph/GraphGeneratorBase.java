package org.opensearch.graph.generator.data.generation.graph;





import org.opensearch.graph.generator.data.generation.entity.EntityGeneratorBase;
import org.opensearch.graph.generator.data.generation.scale.free.BaseModel;
import org.opensearch.graph.generator.model.relation.RelationBase;
import javaslang.Tuple2;
import org.graphstream.graph.Graph;

import java.io.IOException;
import java.util.List;

/**
 * Created by benishue on 15-May-17.
 */

/**
 * @param <C> - Configuration
 * @param <E> - Element (e.g., vertex with its relations)
 */
public abstract class GraphGeneratorBase<C,E> {

    public static final int BUFFER = 1000;

    //region Ctrs
    public GraphGeneratorBase(C configuration, BaseModel model, EntityGeneratorBase entityGenerator) {
        this.personConf = configuration;
        this.entityGenerator = entityGenerator;
        this.model = model;
    }
    //endregion

    //region Abstract Methods

    /**
     * Intended for small-medium graphs, In-Memory graph.
     * We can do SNA on the graph, export graph, draw graph, etc...
     * @return Graph (Graphstream Barabasi- Albert Graph)
     */

    protected abstract Graph generateGraph();
    /**
     * Intended for massive graphs.
     * Only Edges list are built and not full graph.
     * Refernce:
     * author = {Hadian, Ali and Nobari, Sadegh and Minaei-Bidgoli, Behrooz and Qu, Qiang},
     * Title = {ROLL: Fast In-Memory Generation of Gigantic Scale-free Networks}
     * Source Code: https://github.com/alihadian/ROLL
     * @return List of node ids
     */
    protected abstract  List<String> generateMassiveGraph();

    /**
     * @param id Entity (Node) id
     * @return New Fake Entity (e.g., Dragon, Person)
     */
    protected abstract E buildEntityNode(String id);

    /**
     * @param sourceId Source Node Id
     * @param targetId Target Node Id
     * @param edgeId Target Node Id
     * @return Relation between the source node (Entity X) and the target node (Entity Y)
     */
    protected abstract RelationBase buildEntityRelation(String sourceId, String targetId, String edgeId);

    /**
     * Write the graph to the file system
     * @param nodesList list of nodes ids
     * @param edgesList list of edges (source id, target id)
     */
    protected abstract void writeGraph(List<String> nodesList, List<Tuple2> edgesList) throws IOException;
    /**
     * Write the elements to the file system in csv format for each element & its relations
     * @param elements
     */
    protected abstract void writeCSVs(List<E> elements);
    //endregion

    //region Getters
    protected C getConfiguration() {
        return personConf;
    }

    protected BaseModel getModel() {
        return model;
    }

    protected EntityGeneratorBase getEntityGenerator() {
        return entityGenerator;
    }
    //endregion

    //region Fields
    protected final C personConf;
    protected final BaseModel model;
    protected final EntityGeneratorBase<C,E> entityGenerator;

    public abstract void Cleanup();
    //endregion

}
