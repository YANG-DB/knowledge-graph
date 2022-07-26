package org.opensearch.graph.executor.ontology.schema;





import com.google.common.collect.ImmutableList;
import com.typesafe.config.Config;
import org.opensearch.graph.model.resourceInfo.GraphError;
import org.opensearch.graph.model.schema.*;
import org.jooq.Parser;
import org.jooq.Queries;
import org.jooq.Query;
import org.jooq.Table;
import org.jooq.impl.CreateTableStatement;
import org.jooq.impl.DefaultConfiguration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.opensearch.graph.model.schema.MappingIndexType.STATIC;
import static org.opensearch.graph.model.schema.MappingIndexType.UNIFIED;
import static org.jooq.impl.ConstraintStatement.foreignKey;
import static org.jooq.impl.DSL.using;

public class DDLToIndexProviderTranslator implements IndexProviderTranslator<List<String>> {
    public static final String CREATE_RELATION_BY_FK = "create.relation.byFK";
    private Parser parser;
    private Config config;

    public DDLToIndexProviderTranslator(Config config) {
        this.config = config;
    }

    @Override
    public IndexProvider translate(String ontology, List<String> source) {
        IndexProvider indexProvider = IndexProvider.Builder.generate(ontology);
        parser = using(new DefaultConfiguration()).parser();
        source.forEach(s -> parseTable(s, indexProvider));
        return indexProvider;
    }

    private void parseTable(String table, IndexProvider indexProvider) throws GraphError.GraphErrorException {
        try {
            Queries queries = parser.parse(table);
            Arrays.stream(queries.queries())
                    .filter(q -> q.getClass().getSimpleName().endsWith("CreateTableImpl"))
                    .forEach(q -> parse(q, indexProvider));
        } catch (Throwable t) {
            throw new GraphError.GraphErrorException("Error Parsing DDL file " + table, t);
        }
    }

    private void parse(Query createTable, IndexProvider indexProvider) {
        CreateTableStatement statement = new CreateTableStatement(createTable);
        //get table entity
        Table<?> table = statement.getTable();

        //build ontology entity
        String tableName = table.getName().toLowerCase();
        indexProvider.withEntity(new Entity(tableName, STATIC.name(), "Index",
                new Props(ImmutableList.of(tableName)), Collections.emptyList(), Collections.emptyMap()));

        //build relations - FK will now be transformed into EPairs inside
        boolean createRelationByFK = false;
        try {
            createRelationByFK = config.getBoolean(CREATE_RELATION_BY_FK);
        } catch (Throwable ignored) {}

        if (createRelationByFK) {
            foreignKey(statement.getConstraints())
                    .forEach(fk ->
                            indexProvider.withRelation(
                                    new Relation(fk.getName().toLowerCase(),
                                            UNIFIED.name(),
                                            "Index",
                                            false,
                                            Collections.emptyList(),
                                            new Props(ImmutableList.of(tableName)),
                                            Collections.emptyList(),
                                            Collections.emptyMap())));
        }
    }

}
