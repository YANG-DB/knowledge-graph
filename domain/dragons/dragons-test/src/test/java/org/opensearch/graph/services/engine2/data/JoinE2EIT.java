package org.opensearch.graph.services.engine2.data;

import org.opensearch.graph.client.BaseGraphClient;
import org.opensearch.graph.model.GlobalConstants;
import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.execution.plan.PlanAssert;
import org.opensearch.graph.model.execution.plan.composite.Plan;
import org.opensearch.graph.model.execution.plan.entity.EntityFilterOp;
import org.opensearch.graph.model.execution.plan.entity.EntityJoinOp;
import org.opensearch.graph.model.execution.plan.entity.EntityOp;
import org.opensearch.graph.model.execution.plan.relation.RelationFilterOp;
import org.opensearch.graph.model.execution.plan.relation.RelationOp;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.query.*;
import org.opensearch.graph.model.query.entity.EConcrete;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.entity.ETyped;
import org.opensearch.graph.model.query.properties.*;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.properties.projection.IdentityProjection;
import org.opensearch.graph.model.query.quant.Quant1;
import org.opensearch.graph.model.query.quant.QuantType;
import org.opensearch.graph.model.resourceInfo.CursorResourceInfo;
import org.opensearch.graph.model.resourceInfo.GraphResourceInfo;
import org.opensearch.graph.model.resourceInfo.PageResourceInfo;
import org.opensearch.graph.model.resourceInfo.QueryResourceInfo;
import org.opensearch.graph.model.results.*;
import org.opensearch.graph.model.results.Entity;
import org.opensearch.graph.services.TestsConfiguration;
import org.opensearch.graph.client.GraphClient;
import org.opensearch.graph.stats.StatCalculator;
import org.opensearch.graph.stats.configuration.StatConfiguration;
import org.opensearch.graph.test.BaseITMarker;
import org.opensearch.graph.test.data.DragonsOntology;
import org.opensearch.graph.test.framework.index.MappingEngineConfigurer;
import org.opensearch.graph.test.framework.index.MappingFileConfigurer;
import org.opensearch.graph.test.framework.index.Mappings;
import org.opensearch.graph.test.framework.index.SearchEmbeddedNode;
import org.opensearch.graph.test.framework.populator.SearchEngineDataPopulator;
import org.apache.commons.configuration.Configuration;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.opensearch.action.admin.indices.delete.DeleteIndexRequest;
import org.opensearch.action.admin.indices.refresh.RefreshRequest;
import org.opensearch.client.transport.TransportClient;
import org.junit.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.opensearch.graph.test.data.DragonsOntology.*;
import static org.opensearch.graph.test.data.DragonsOntology.BIRTH_DATE;
import static org.opensearch.graph.test.data.DragonsOntology.GENDER;
import static org.opensearch.graph.test.data.DragonsOntology.POWER;
import static org.opensearch.graph.test.data.DragonsOntology.TEMPERATURE;
import static org.opensearch.graph.test.data.DragonsOntology.TIMESTAMP;

@Ignore
public class JoinE2EIT implements BaseITMarker {
    @BeforeClass
    public static void setup() throws Exception {
        setup(SearchEmbeddedNode.getClient(), true);
    }

    @AfterClass
    public static void cleanup() throws Exception {
        cleanup(SearchEmbeddedNode.getClient());
    }


    public static void setup(TransportClient client, boolean calcStats) throws Exception {
        GraphClient = new BaseGraphClient("http://localhost:8888/opengraph");
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        $ont = new Ontology.Accessor(GraphClient.getOntology(graphResourceInfo.getCatalogStoreUrl() + "/Dragons"));

        String idField = "id";

        new MappingEngineConfigurer(DragonsOntology.PERSON.name.toLowerCase(), new Mappings().addMapping("pge", getPersonMapping()))
                .configure(client);
        new MappingEngineConfigurer(DragonsOntology.DRAGON.name.toLowerCase(), new Mappings().addMapping("pge", getDragonMapping()))
                .configure(client);
        new MappingEngineConfigurer(DragonsOntology.KINGDOM.name.toLowerCase(), new Mappings().addMapping("pge", getKingdomMapping()))
                .configure(client);
        new MappingEngineConfigurer(Arrays.asList(
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513"),
                new Mappings().addMapping("pge", getFireMapping()))
                .configure(client);
        new MappingEngineConfigurer(Arrays.asList("originated_in"), new Mappings().addMapping("pge", getOriginMapping()))
                .configure(client);

        birthDateValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        timestampValueFunctionFactory = startingDate -> interval -> i -> startingDate + (interval * i);
        temperatureValueFunction = i -> 1000 + (100 * i);

        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        int numPersons = 100;
        int numDragons = 100;
        int numKingdoms = 2;

        new SearchEngineDataPopulator(
                client,
                DragonsOntology.PERSON.name.toLowerCase(),
                "pge",
                idField,
                () -> createPeople(numPersons)).populate();

        new SearchEngineDataPopulator(
                client,
                DragonsOntology.DRAGON.name.toLowerCase(),
                "pge",
                idField,
                () -> createDragons(numDragons, birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L)))
                .populate(); // date interval is ~ 1 month

        new SearchEngineDataPopulator(client,
                DragonsOntology.KINGDOM.name.toLowerCase(),
                "pge",
                idField,
                ()-> createKingdoms(numKingdoms))
                .populate();

        new SearchEngineDataPopulator(client,
                "originated_in",
                "pge",
                idField,
                () -> createOriginEdges(numDragons, numKingdoms)
                ).populate();

        new SearchEngineDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170511",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-11 00:00:00").getTime()).apply(1200000L),
                        temperatureValueFunction))
                .populate(); // date interval is 20 min

        new SearchEngineDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170512",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-12 00:00:00").getTime()).apply(600000L),
                        temperatureValueFunction))
                .populate(); // date interval is 10 min

        new SearchEngineDataPopulator(
                client,
                FIRE.getName().toLowerCase() + "20170513",
                "pge",
                idField,
                () -> createDragonFireDragonEdges(
                        numDragons,
                        timestampValueFunctionFactory.apply(sdf.parse("2017-05-13 00:00:00").getTime()).apply(300000L),
                        temperatureValueFunction))
                .populate(); // date interval is 5 min


        client.admin().indices().refresh(new RefreshRequest(
                DragonsOntology.PERSON.name.toLowerCase(),
                DragonsOntology.DRAGON.name.toLowerCase(),
                FIRE.getName().toLowerCase() + "20170511",
                FIRE.getName().toLowerCase() + "20170512",
                FIRE.getName().toLowerCase() + "20170513",
                DragonsOntology.KINGDOM.name.toLowerCase(),
                "originated_in"
        )).actionGet();

        if(calcStats){
            new MappingFileConfigurer("stat", "src/test/resources/stat_mappings.json").configure(client);
            Configuration statConfig = new StatConfiguration("statistics.test.properties").getInstance();
            StatCalculator.run(client, client, statConfig);
            client.admin().indices().refresh(new RefreshRequest("stat")).actionGet();
        }
    }




    public static void cleanup(TransportClient client) throws Exception {
        cleanup(client, true);
    }

    public static void cleanup(TransportClient client, boolean statsUsed) throws Exception {
        client.admin().indices()
                .delete(new DeleteIndexRequest(
                        DragonsOntology.PERSON.name.toLowerCase(),
                        DragonsOntology.DRAGON.name.toLowerCase(),
                        FIRE.getName().toLowerCase() + "20170511",
                        FIRE.getName().toLowerCase() + "20170512",
                        FIRE.getName().toLowerCase() + "20170513",
                        DragonsOntology.KINGDOM.name.toLowerCase(),
                        "originated_in"))
                .actionGet();

        if(statsUsed){
            client.admin().indices().delete(new DeleteIndexRequest("stat")).actionGet();
        }
    }

    @Before
    public void before() {
        Assume.assumeTrue(TestsConfiguration.instance.shouldRunTestClass(this.getClass()));
    }

    private void testAndAssertQuery(Query query, AssignmentsQueryResult expectedAssignmentsQueryResult) throws Exception {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        QueryResultAssert.assertEquals(expectedAssignmentsQueryResult, actualAssignmentsQueryResult, shouldIgnoreRelId());
    }



    private static Iterable<Map<String, Object>> createPeople(int numPeople) {
        List<Map<String, Object>> people = new ArrayList<>();
        for(int i = 0 ; i < numPeople ; i++) {
            Map<String, Object> person = new HashMap<>();
            person.put("id", "Person_" + i);
            person.put("type", "Person");
            person.put(NAME.name, "person" + i);
            people.add(person);
        }
        return people;
    }

    private static Mappings.Mapping getPersonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private static Mappings.Mapping getKingdomMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword));
    }

    private static Iterable<Map<String, Object>> createDragons(
            int numDragons,
            Function<Integer, Long> birthDateValueFunction) {

        List<Map<String, Object>> dragons = new ArrayList<>();
        for(int i = 0 ; i < numDragons ; i++) {
            Map<String, Object> dragon = new HashMap<>();
            dragon.put("id", "Dragon_" + i);
            dragon.put("type", DragonsOntology.DRAGON.name);
            int nameChar = i%58 + 65;
            dragon.put(NAME.name, (char)nameChar);
            dragon.put(BIRTH_DATE.name, sdf.format(new Date(birthDateValueFunction.apply(i))));
            dragons.add(dragon);
        }
        return dragons;
    }

    private static Mappings.Mapping getDragonMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(NAME.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(BIRTH_DATE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"));
    }


    private static Iterable<Map<String, Object>> createDragonFireDragonEdges(
            int numDragons,
            Function<Integer, Long> timestampValueFunction,
            Function<Integer, Integer> temperatureValueFunction
    ) throws ParseException {
        List<Map<String, Object>> fireEdges = new ArrayList<>();

        int counter = 0;
        for(int i = 0 ; i < numDragons ; i++) {
            for(int j = 0 ; j < i ; j++) {
                Map<String, Object> fireEdge = new HashMap<>();
                fireEdge.put("id", FIRE.getName() + counter);
                fireEdge.put("type", FIRE.getName());
                fireEdge.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdge.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.OUT.name());
                fireEdge.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> fireEdgeDual = new HashMap<>();
                fireEdgeDual.put("id", FIRE.getName() + counter + 1);
                fireEdgeDual.put("type", FIRE.getName());
                fireEdgeDual.put(TIMESTAMP.name, timestampValueFunction.apply(counter));
                fireEdgeDual.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.IN.name());
                fireEdgeDual.put(TEMPERATURE.name, temperatureValueFunction.apply(j));

                Map<String, Object> entityAI = new HashMap<>();
                entityAI.put("id", "Dragon_" + i);
                entityAI.put("type", DragonsOntology.DRAGON.name);
                Map<String, Object> entityAJ = new HashMap<>();
                entityAJ.put("id", "Dragon_" + j);
                entityAJ.put("type", DragonsOntology.DRAGON.name);
                Map<String, Object> entityBI = new HashMap<>();
                entityBI.put("id", "Dragon_" + i);
                entityBI.put("type", DragonsOntology.DRAGON.name);
                Map<String, Object> entityBJ = new HashMap<>();
                entityBJ.put("id", "Dragon_" + j);
                entityBJ.put("type", DragonsOntology.DRAGON.name);

                fireEdge.put(GlobalConstants.EdgeSchema.SOURCE, entityAI);
                fireEdge.put(GlobalConstants.EdgeSchema.DEST, entityBJ);
                fireEdgeDual.put(GlobalConstants.EdgeSchema.SOURCE, entityAJ);
                fireEdgeDual.put(GlobalConstants.EdgeSchema.DEST, entityBI);

                fireEdges.addAll(Arrays.asList(fireEdge, fireEdgeDual));

                counter += 2;
            }
        }

        return fireEdges;
    }

    private static Mappings.Mapping getFireMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TIMESTAMP.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.date, "yyyy-MM-dd HH:mm:ss||date_optional_time||epoch_millis"))
                .addProperty(GlobalConstants.EdgeSchema.DIRECTION, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(TEMPERATURE.name, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.integer))
                .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
                .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                        .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                        .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createKingdoms(int numKingdoms) {
        List<Map<String, Object>> kingdoms = new ArrayList<>();
        for(int i = 0 ; i < numKingdoms ; i++) {
            Map<String, Object> kingdom = new HashMap<>();
            kingdom.put("id", "Kingdom_" + i);
            kingdom.put("type", "Kingdom");
            kingdom.put(NAME.name, "kingdom" + i);
            kingdoms.add(kingdom);
        }
        return kingdoms;
    }

    private static Mappings.Mapping getOriginMapping() {
        return new Mappings.Mapping()
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty(GlobalConstants.EdgeSchema.DIRECTION, new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
            .addProperty(GlobalConstants.EdgeSchema.SOURCE, new Mappings.Mapping.Property()
                .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)))
            .addProperty(GlobalConstants.EdgeSchema.DEST, new Mappings.Mapping.Property()
                    .addProperty("id", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword))
                    .addProperty("type", new Mappings.Mapping.Property(Mappings.Mapping.Property.Type.keyword)));
    }

    private static Iterable<Map<String, Object>> createOriginEdges(int numDragons, int numKingdoms) {
        List<Map<String, Object>> originEdges = new ArrayList<>();
        int counter = 0;
        for(int i = 0;i<numDragons;i++){
            Map<String, Object> originEdgeOut = new HashMap<>();
            originEdgeOut.put("id", ORIGIN.getName() + counter);
            originEdgeOut.put("type", ORIGIN.getName());
            originEdgeOut.put(GlobalConstants.EdgeSchema.DIRECTION, Direction.OUT.name());

            Map<String, Object> originEdgeIn = new HashMap<>();
            originEdgeIn.put("id", ORIGIN.getName() + counter+1);
            originEdgeIn.put("type", ORIGIN.getName());
            originEdgeIn.put("direction", Direction.IN.name());


            Map<String, Object> dragonEntity = new HashMap<>();
            dragonEntity.put("id", "Dragon_" + i);
            dragonEntity.put("type", DragonsOntology.DRAGON.name);

            Map<String, Object> kingdomEntity = new HashMap<>();
            kingdomEntity.put("id", "Kingdom_" + i % numKingdoms);
            kingdomEntity.put("type", DragonsOntology.KINGDOM.name);

            originEdgeOut.put(GlobalConstants.EdgeSchema.SOURCE, dragonEntity);
            originEdgeOut.put(GlobalConstants.EdgeSchema.DEST, kingdomEntity);

            originEdgeIn.put(GlobalConstants.EdgeSchema.SOURCE, kingdomEntity);
            originEdgeIn.put(GlobalConstants.EdgeSchema.DEST, dragonEntity);

            originEdges.add(originEdgeOut);
            originEdges.add(originEdgeIn);
            counter += 2;
        }

        return originEdges;
    }

    //endregion

    @Test
    public void testDragonOriginKingdomX2Path() throws IOException, InterruptedException, ParseException {
        Query query = getDragonOriginKingdomX2Query();

        runQueryAndValidate(query, dragonOriginKingdomX2Results(), dragonOriginKingdomX2Plan(query));
    }

    @Test
    public void testDragonOriginKingdomX3Path() throws IOException, InterruptedException, ParseException {
        Query query = getDragonOriginKingdomX3Query();

        runQueryAndValidate(query, dragonOriginKingdomX3Results(), dragonOriginKingdomX3Plan(query));
    }

    private Plan dragonOriginKingdomX3Plan(Query query) {


        Plan leftBranch = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(1))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(101,
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, BIRTH_DATE.type, new IdentityProjection()),
                        new EProp(0, POWER.type, new IdentityProjection()),
                        new EProp(0, GENDER.type, new IdentityProjection()),
                        new EProp(0, COLOR.type, new IdentityProjection())))),
                new RelationOp(new AsgEBase<>((Rel)query.getElements().get(2) )),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(201,
                        new RedundantRelProp(0, "type", GlobalConstants.EdgeSchema.DEST_TYPE, GlobalConstants.EdgeSchema.DEST_TYPE, Constraint.of(ConstraintOp.inSet, Collections.singletonList(DragonsOntology.KINGDOM.name)))))),
                new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(3))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(102,
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, KING.type, new IdentityProjection()),
                        new EProp(0, QUEEN.type, new IdentityProjection()),
                        new EProp(0, INDEPENDENCE_DAY.type, new IdentityProjection()),
                        new EProp(0, FUNDS.type, new IdentityProjection()))))
        );
        Rel rel = (Rel) query.getElements().get(5).clone();
        rel.setDir(Rel.Direction.R);
        Plan rightBranch = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(6))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(7,
                        new SchematicEProp(0,NAME.type, NAME.name, Constraint.of(ConstraintOp.eq, "D")),
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, BIRTH_DATE.type, new IdentityProjection()),
                        new EProp(0, POWER.type, new IdentityProjection()),
                        new EProp(0, GENDER.type, new IdentityProjection()),
                        new EProp(0, COLOR.type, new IdentityProjection())))),
                new RelationOp(new AsgEBase<>( rel)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(501))),
                new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(3))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(102,
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, KING.type, new IdentityProjection()),
                        new EProp(0, QUEEN.type, new IdentityProjection()),
                        new EProp(0, INDEPENDENCE_DAY.type, new IdentityProjection()),
                        new EProp(0, FUNDS.type, new IdentityProjection()))))
        );

        Plan innerJoin = new Plan(new EntityJoinOp(leftBranch, rightBranch));
        Rel rel2 = (Rel) query.getElements().get(8).clone();
        rel2.setDir(Rel.Direction.R);
        Plan rightBranch2 = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(9))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(10,
                        new SchematicEProp(0,NAME.type, NAME.name, Constraint.of(ConstraintOp.eq, "F")),
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, BIRTH_DATE.type, new IdentityProjection()),
                        new EProp(0, POWER.type, new IdentityProjection()),
                        new EProp(0, GENDER.type, new IdentityProjection()),
                        new EProp(0, COLOR.type, new IdentityProjection())))),
                new RelationOp(new AsgEBase<>( rel2)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(801))),
                new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(3))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(102,
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, KING.type, new IdentityProjection()),
                        new EProp(0, QUEEN.type, new IdentityProjection()),
                        new EProp(0, INDEPENDENCE_DAY.type, new IdentityProjection()),
                        new EProp(0, FUNDS.type, new IdentityProjection()))))
        );
        return new Plan(new EntityJoinOp(innerJoin, rightBranch2));
    }

    private AssignmentsQueryResult dragonOriginKingdomX3Results() throws ParseException {
        Function<Integer, Long> birthDateValueFunction =
                birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L);

        AssignmentsQueryResult.Builder builder = AssignmentsQueryResult.Builder.instance();
        Entity entityA = Entity.Builder.instance()
                .withEID("Dragon_1" )
                .withETag(singleton("A"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "B"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(1))))))
                .build();
        Entity entityB = Entity.Builder.instance()
                .withEID("Kingdom_1" )
                .withETag(singleton("B"))
                .withEType($ont.eType$(DragonsOntology.KINGDOM.name))
                .withProperties(singletonList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "kingdom1")))
                .build();

        List<Entity> entitiesC = new ArrayList<>();
        entitiesC.add(Entity.Builder.instance()
                .withEID("Dragon_3" )
                .withETag(singleton("C"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "D"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(3))))))
                .build());

        entitiesC.add(Entity.Builder.instance()
                .withEID("Dragon_61" )
                .withETag(singleton("C"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "D"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(61))))))
                .build());

        List<Entity> entitiesD = new ArrayList<>();
        entitiesD.add(Entity.Builder.instance()
                .withEID("Dragon_5" )
                .withETag(singleton("D"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "F"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(5))))))
                .build());
        entitiesD.add(Entity.Builder.instance()
                .withEID("Dragon_63" )
                .withETag(singleton("D"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "F"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(63))))))
                .build());

        for(Entity entityC : entitiesC) {
            for (Entity entityD : entitiesD) {
                Relationship relationship1 = Relationship.Builder.instance()
                        .withRID("123")
                        .withDirectional(false)
                        .withEID1(entityA.geteID())
                        .withEID2(entityB.geteID())
                        .withETag1("A")
                        .withETag2("B")
                        .withRType($ont.rType$(ORIGIN.getName()))
                        .build();


                Relationship relationship2 = Relationship.Builder.instance()
                        .withRID("123")
                        .withDirectional(false)
                        .withEID1(entityC.geteID())
                        .withEID2(entityB.geteID())
                        .withETag1("C")
                        .withETag2("B")
                        .withRType($ont.rType$(ORIGIN.getName()))
                        .build();

                Relationship relationship3 = Relationship.Builder.instance()
                        .withRID("123")
                        .withDirectional(false)
                        .withEID1(entityD.geteID())
                        .withEID2(entityB.geteID())
                        .withETag1("D")
                        .withETag2("B")
                        .withRType($ont.rType$(ORIGIN.getName()))
                        .build();

                Assignment assignment = Assignment.Builder.instance().withEntity(entityA).withEntity(entityB).withEntity(entityC).withEntity(entityD)
                        .withRelationship(relationship1).withRelationship(relationship2).withRelationship(relationship3).build();
                builder.withAssignment(assignment);
            }
        }

        return builder.build();

    }

    private Plan dragonOriginKingdomX2Plan(Query query) {
        Plan leftBranch = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(1))),
                                    new EntityFilterOp(new AsgEBase<>(new EPropGroup(101,
                                            new EProp(0, NAME.type, new IdentityProjection()),
                                            new EProp(0, BIRTH_DATE.type, new IdentityProjection()),
                                            new EProp(0, POWER.type, new IdentityProjection()),
                                            new EProp(0, GENDER.type, new IdentityProjection()),
                                            new EProp(0, COLOR.type, new IdentityProjection())))),
                                    new RelationOp(new AsgEBase<>((Rel)query.getElements().get(2) )),
                                    new RelationFilterOp(new AsgEBase<>(new RelPropGroup(201,
                                            new RedundantRelProp(0, "type", GlobalConstants.EdgeSchema.DEST_TYPE, GlobalConstants.EdgeSchema.DEST_TYPE, Constraint.of(ConstraintOp.inSet, Collections.singletonList(DragonsOntology.KINGDOM.name)))))),
                                    new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(3))),
                                    new EntityFilterOp(new AsgEBase<>(new EPropGroup(301,
                                            new EProp(0, NAME.type, new IdentityProjection()),
                                            new EProp(0, KING.type, new IdentityProjection()),
                                            new EProp(0, QUEEN.type, new IdentityProjection()),
                                            new EProp(0, INDEPENDENCE_DAY.type, new IdentityProjection()),
                                            new EProp(0, FUNDS.type, new IdentityProjection()))))
                                );
        Rel rel = (Rel) query.getElements().get(4).clone();
        rel.setDir(Rel.Direction.R);
        Plan rightBranch = new Plan(new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(5))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(6,
                        new SchematicEProp(0, NAME.type, NAME.name, Constraint.of(ConstraintOp.eq, "D")),
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, BIRTH_DATE.type, new IdentityProjection()),
                        new EProp(0, POWER.type, new IdentityProjection()),
                        new EProp(0, GENDER.type, new IdentityProjection()),
                        new EProp(0, COLOR.type, new IdentityProjection())))),
                new RelationOp(new AsgEBase<>( rel)),
                new RelationFilterOp(new AsgEBase<>(new RelPropGroup(401))),
                new EntityOp(new AsgEBase<>((EEntityBase) query.getElements().get(3))),
                new EntityFilterOp(new AsgEBase<>(new EPropGroup(301,
                        new EProp(0, NAME.type, new IdentityProjection()),
                        new EProp(0, KING.type, new IdentityProjection()),
                        new EProp(0, QUEEN.type, new IdentityProjection()),
                        new EProp(0, INDEPENDENCE_DAY.type, new IdentityProjection()),
                        new EProp(0, FUNDS.type, new IdentityProjection()))))
        );

        return new Plan(new EntityJoinOp(leftBranch, rightBranch));
    }

    private AssignmentsQueryResult dragonOriginKingdomX2Results() throws ParseException {
        Function<Integer, Long> birthDateValueFunction =
                birthDateValueFunctionFactory.apply(sdf.parse("1980-01-01 00:00:00").getTime()).apply(2592000000L);

        AssignmentsQueryResult.Builder builder = AssignmentsQueryResult.Builder.instance();
        Entity entityA = Entity.Builder.instance()
                .withEID("Dragon_1" )
                .withETag(singleton("A"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "B"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(1))))))
                .build();
        Entity entityB = Entity.Builder.instance()
                .withEID("Kingdom_1" )
                .withETag(singleton("B"))
                .withEType($ont.eType$(DragonsOntology.KINGDOM.name))
                .withProperties(singletonList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "kingdom1")))
                .build();

        List<Entity> entitiesC = new ArrayList<>();
        entitiesC.add(Entity.Builder.instance()
                .withEID("Dragon_3" )
                .withETag(singleton("C"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "D"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(3))))))
                .build());

        entitiesC.add(Entity.Builder.instance()
                .withEID("Dragon_61" )
                .withETag(singleton("C"))
                .withEType($ont.eType$(DragonsOntology.DRAGON.name))
                .withProperties(Arrays.asList(
                        new org.opensearch.graph.model.results.Property(NAME.type, "raw", "D"),
                        new org.opensearch.graph.model.results.Property(BIRTH_DATE.type, "raw", sdf.format(new Date(birthDateValueFunction.apply(61))))))
                .build());

        for(Entity entityC : entitiesC) {
            Relationship relationship1 = Relationship.Builder.instance()
                    .withRID("123")
                    .withDirectional(false)
                    .withEID1(entityA.geteID())
                    .withEID2(entityB.geteID())
                    .withETag1("A")
                    .withETag2("B")
                    .withRType($ont.rType$(ORIGIN.getName()))
                    .build();


            Relationship relationship2 = Relationship.Builder.instance()
                    .withRID("123")
                    .withDirectional(false)
                    .withEID1(entityC.geteID())
                    .withEID2(entityB.geteID())
                    .withETag1("C")
                    .withETag2("B")
                    .withRType($ont.rType$(ORIGIN.getName()))
                    .build();

            Assignment assignment = Assignment.Builder.instance().withEntity(entityA).withEntity(entityB).withEntity(entityC)
                    .withRelationship(relationship1).withRelationship(relationship2).build();
            builder.withAssignment(assignment);
        }

        return builder.build();

    }


    private void runQueryAndValidate(Query query, AssignmentsQueryResult expectedAssignmentsQueryResult, Plan expectedPlan) throws IOException, InterruptedException {
        GraphResourceInfo graphResourceInfo = GraphClient.getInfo();
        QueryResourceInfo queryResourceInfo = GraphClient.postQuery(graphResourceInfo.getQueryStoreUrl(), query);
        Plan actualPlan = GraphClient.getPlanObject(queryResourceInfo.getExplainPlanUrl());
        CursorResourceInfo cursorResourceInfo = GraphClient.postCursor(queryResourceInfo.getCursorStoreUrl());
        PageResourceInfo pageResourceInfo = GraphClient.postPage(cursorResourceInfo.getPageStoreUrl(), 1000);

        while (!pageResourceInfo.isAvailable()) {
            pageResourceInfo = GraphClient.getPage(pageResourceInfo.getResourceUrl());
            if (!pageResourceInfo.isAvailable()) {
                Thread.sleep(10);
            }
        }

        AssignmentsQueryResult actualAssignmentsQueryResult = (AssignmentsQueryResult) GraphClient.getPageData(pageResourceInfo.getDataUrl());
        PlanAssert.assertEquals(expectedPlan, actualPlan);


        QueryResultAssert.assertEquals(expectedAssignmentsQueryResult, actualAssignmentsQueryResult, shouldIgnoreRelId());
    }

    private Query getDragonOriginKingdomX2Query() {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$(DragonsOntology.DRAGON.name), "Dragon_1", "D0", 2, 0),
                new Rel(2, $ont.rType$(ORIGIN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DragonsOntology.KINGDOM.name), 4, 0),
                new Rel(4, $ont.rType$(ORIGIN.getName()), Rel.Direction.L, null, 5, 0),
                new ETyped(5, "C", $ont.eType$(DragonsOntology.DRAGON.name), 6, 0),
                new EProp(6, NAME.type, Constraint.of(ConstraintOp.eq, "D"))
        )).build();
    }

    private Query getDragonOriginKingdomX3Query() {
        return Query.Builder.instance().withName(NAME.name).withOnt($ont.name()).withElements(Arrays.asList(
                new Start(0, 1),
                new EConcrete(1, "A", $ont.eType$(DragonsOntology.DRAGON.name), "Dragon_1", "D0", 2, 0),
                new Rel(2, $ont.rType$(ORIGIN.getName()), Rel.Direction.R, null, 3, 0),
                new ETyped(3, "B", $ont.eType$(DragonsOntology.KINGDOM.name), 4, 0),
                new Quant1(4, QuantType.all, Arrays.asList(5,8),0),
                new Rel(5, $ont.rType$(ORIGIN.getName()), Rel.Direction.L, null, 6, 0),
                new ETyped(6, "C", $ont.eType$(DragonsOntology.DRAGON.name), 7, 0),
                new EProp(7, NAME.type, Constraint.of(ConstraintOp.eq, "D")),
                new Rel(8, $ont.rType$(ORIGIN.getName()), Rel.Direction.L, null, 9, 0),
                new ETyped(9, "D", $ont.eType$(DragonsOntology.DRAGON.name), 10, 0),
                new EProp(10, NAME.type, Constraint.of(ConstraintOp.eq, "F"))
        )).build();
    }


    protected boolean shouldIgnoreRelId() {
        return true;
    }
    private static GraphClient GraphClient;
    private static Ontology.Accessor $ont;
    private static SimpleDateFormat sdf;

    private static Function<Long, Function<Long, Function<Integer, Long>>> timestampValueFunctionFactory;
    private static Function<Long, Function<Long, Function<Integer, Long>>> birthDateValueFunctionFactory;
    private static Function<Integer, Integer> temperatureValueFunction;
}
