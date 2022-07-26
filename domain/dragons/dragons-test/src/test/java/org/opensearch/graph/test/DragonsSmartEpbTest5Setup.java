package org.opensearch.graph.test;

import javaslang.collection.Stream;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.client.transport.TransportClient;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.OntologyTestUtils;
import org.opensearch.graph.services.test.TestRunner;
import org.opensearch.graph.stats.StatCalculator;
import org.opensearch.graph.stats.configuration.StatConfiguration;
import org.opensearch.graph.test.data.DragonsOntology;
import org.opensearch.graph.test.framework.index.MappingEngineConfigurer;
import org.opensearch.graph.test.framework.index.MappingFileConfigurer;
import org.opensearch.graph.test.framework.index.Mappings;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;

import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static org.opensearch.graph.model.OntologyTestUtils.*;

public class DragonsSmartEpbTest5Setup extends TestSetupBase  {
    public static void main(String[] args) throws Exception {
        DragonsSmartEpbTest5Setup test = new DragonsSmartEpbTest5Setup();
        DragonKingdomQuery5Test dragonKingdomQueryTest = new DragonKingdomQuery5Test();
        TestRunner.run(dragonKingdomQueryTest, test,"m2.smartEpb","m1.dfs.redundant");
    }

    @Override
    protected void loadData(TransportClient client) throws Exception {
        String idField = "id";
        new MappingEngineConfigurer(DRAGON.name.toLowerCase(), new Mappings().addMapping("pge", getDragonMapping()))
                .configure(client);
        new MappingEngineConfigurer(KINGDOM.name.toLowerCase(), new Mappings().addMapping("pge", getKingdomMapping()))
                .configure(client);
        new MappingEngineConfigurer(Arrays.asList(ORIGINATED_IN), new Mappings().addMapping("pge", getOriginMapping()))
                .configure(client);
        new MappingEngineConfigurer(Arrays.asList(FIRE.getName().toLowerCase()), new Mappings().addMapping("pge", getFireMapping()))
                .configure(client);

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        timestampValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        temperatureValueFunction = i -> 1000 + (100 * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        int numDragons = 10000;
        int numKingdoms = 10;

        new SearchEngineDataPopulator(
                client,
                DRAGON.name.toLowerCase(),
                "pge",
                idField,
                () -> createDragons(numDragons, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new SearchEngineDataPopulator(client,
                KINGDOM.name.toLowerCase(),
                "pge",
                idField,
                ()-> createKingdoms(numKingdoms))
                .populate();

        new SearchEngineDataPopulator(client,
                ORIGINATED_IN,
                "pge",
                idField,
                () -> createOriginEdges(numDragons, numKingdoms)
        ).populate();

        new SearchEngineDataPopulator(
                client,
                FIRE.getName().toLowerCase(),
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-11 00:00:00").getTime()).apply(1200000L),
                        temperatureValueFunction))
                .populate(); // date interval is 20 min


        client.admin().indices().refresh(new RefreshRequest(
                DragonsOntology.DRAGON.name.toLowerCase(),
                KINGDOM.name.toLowerCase(),
                ORIGINATED_IN,
                FIRE.getName().toLowerCase()

        )).actionGet();

        new MappingFileConfigurer("stat", Paths.get("fuse-test","fuse-benchmarks-test","src","main","resources","stat","stat_mappings.json").toString()).configure(client);
        Configuration statConfig = new StatConfiguration("stat/statistics.test.properties").getInstance();
        StatCalculator.run(client, client, statConfig);
        client.admin().indices().refresh(new RefreshRequest("stat")).actionGet();
    }

    @Override
    protected void cleanData(TransportClient client) {
        client.admin().indices()
                .delete(new DeleteIndexRequest(
                        DRAGON.name.toLowerCase(),
                        KINGDOM.name.toLowerCase(),
                        "originated_in",
                        FIRE.getName().toLowerCase()))
                .actionGet();
        client.admin().indices().delete(new DeleteIndexRequest("stat")).actionGet();
    }

    private static Mappings.Mapping getDragonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(BIRTH_DATE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"));
    }

    private static Mappings.Mapping getKingdomMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private static Mappings.Mapping getFireMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TIMESTAMP.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"))
                .addProperty("direction", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TEMPERATURE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.integer))
                .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
                .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createDragonFireDragonEdges(
            int numDragons,
            Function<Integer, Long> timestampValueFunction,
            Function<Integer, Integer> temperatureValueFunction
    ) throws ParseException {


        return Stream.range(0,numDragons).flatMap(i -> {
            List<Map<String, Object>> fireEdges = new ArrayList<>();
            int numNeigh = 10;
            for (int j = Math.max(0,i- numNeigh);j<i;j++){
                int counter = i*numNeigh*2+j*2;
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", FIRE.getName() + counter);
                fireEdge.put("type", FIRE.getName());
                fireEdge.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdge.put("direction", Direction.OUT.name());
                fireEdge.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", FIRE.getName() + counter + 1);
                fireEdgeDual.put("type", FIRE.getName());
                fireEdgeDual.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdgeDual.put("direction", Direction.IN.name());
                fireEdgeDual.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "Dragon_" + i);
                entityAI.put("type", DRAGON.name);
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "Dragon_" + j);
                entityAJ.put("type", DRAGON.name);
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "Dragon_" + i);
                entityBI.put("type", DRAGON.name);
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "Dragon_" + j);
                entityBJ.put("type", DRAGON.name);

                fireEdge.put(GlobalConstants.EdgeSchema.SOURCE, entityAI);
                fireEdge.put(GlobalConstants.EdgeSchema.DEST, entityBJ);
                fireEdgeDual.put(GlobalConstants.EdgeSchema.SOURCE, entityAJ);
                fireEdgeDual.put(GlobalConstants.EdgeSchema.DEST, entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

            }
            return fireEdges;
        });
    }

    private Iterable<Map<String, Object>> createDragons(int numDragons,
                                                        Function<Integer, Long> birthDateValueFunction) {
        return Stream.range(0, numDragons).map(i->{
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "Dragon_" + i);
            dragon.put("type", DRAGON.name);
            int nameChar1 = i%58 + 65;

            dragon.put(NAME.name, String.valueOf((char)nameChar1));
            dragon.put(BIRTH_DATE.name, sdf.format(new Date(birthDateValueFunction.apply(i))));
            return dragon;
        });


    }

    private static Iterable<Map<String, Object>> createKingdoms(int numKingdoms) {
        return Stream.range(0,numKingdoms).map(i -> {
            Map<String, Object> kingdom = new HashMap<>();
            kingdom.put("id", "Kingdom_" + i);
            kingdom.put("type", "Kingdom");
            kingdom.put(NAME.name, "kingdom" + i);
            //kingdoms.add(kingdom);
            return kingdom;
        });
    }

    private static Mappings.Mapping getOriginMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty("direction", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
                .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createOriginEdges(int numDragons, int numKingdoms) {
        return Stream.range(0,numDragons).flatMap(i->{
            List<Map<String, Object>> originEdges = new ArrayList<>();
            Map<String, Object> originEdgeOut = new HashMap<>();
            originEdgeOut.put("id", OntologyTestUtils.ORIGINATED_IN.getName() + i*2);
            originEdgeOut.put("type", OntologyTestUtils.ORIGINATED_IN.getName());
            originEdgeOut.put("direction", Direction.OUT.name());

            Map<String, Object> originEdgeIn = new HashMap<>();
            originEdgeIn.put("id", OntologyTestUtils.ORIGINATED_IN.getName() + (i*2+1));
            originEdgeIn.put("type", OntologyTestUtils.ORIGINATED_IN.getName());
            originEdgeIn.put("direction", Direction.IN.name());


            Map<String, Object> dragonEntity = new HashMap<>();
            dragonEntity.put("id", "Dragon_" + i);
            dragonEntity.put("type", DRAGON.name);

            Map<String, Object> kingdomEntity = new HashMap<>();
            kingdomEntity.put("id", "Kingdom_" + i % numKingdoms);
            kingdomEntity.put("type", KINGDOM.name);

            originEdgeOut.put(GlobalConstants.EdgeSchema.SOURCE, dragonEntity);
            originEdgeOut.put(GlobalConstants.EdgeSchema.DEST, kingdomEntity);

            originEdgeIn.put(GlobalConstants.EdgeSchema.SOURCE, kingdomEntity);
            originEdgeIn.put(GlobalConstants.EdgeSchema.DEST, dragonEntity);

            originEdges.add(originEdgeOut);
            originEdges.add(originEdgeIn);
            return originEdges;
        });

    }

    private SimpleDateFormat sdf;
    private Function<Long, Function<Long, Function<Integer, Long>>> birthDateValueFunctionFactory;
    private static Function<Long, Function<Long, Function<Integer, Long>>> timestampValueFunctionFactory;
    private static Function<Integer, Integer> temperatureValueFunction;
    public static final String ORIGINATED_IN = "originated_in";
}
