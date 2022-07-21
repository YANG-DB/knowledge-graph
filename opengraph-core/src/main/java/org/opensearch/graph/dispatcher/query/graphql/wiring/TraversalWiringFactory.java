package org.opensearch.graph.dispatcher.query.graphql.wiring;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.opensearch.graph.dispatcher.query.graphql.GraphQLSchemaUtils;
import org.opensearch.graph.model.ontology.EntityType;
import org.opensearch.graph.model.ontology.Ontology;
import org.opensearch.graph.model.ontology.Property;
import org.opensearch.graph.model.ontology.RelationshipType;
import org.opensearch.graph.model.query.Query;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.properties.constraint.Constraint;
import org.opensearch.graph.model.query.properties.constraint.ConstraintOp;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.query.quant.QuantType;
import graphql.Internal;
import graphql.Scalars;
import graphql.execution.ExecutionPath;
import graphql.execution.ExecutionStepInfo;
import graphql.schema.*;
import graphql.schema.idl.*;
import javaslang.Tuple2;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;


/**
 * A wiring factory that will echo back the objects defined.  That is if you have a field called
 * "name" of type String, it will echo back the value "name".  This is ONLY useful for mocking out a
 * schema that do don't want to actually execute properly.
 */
@Internal
public class TraversalWiringFactory implements WiringFactory {

    public static final String WHERE = "where";
    public static final String QUERY = "query";
    //wiring jackson mapper
    public static final ObjectMapper mapper = new ObjectMapper();

    private Query.Builder builder;
    private Ontology.Accessor accessor;
    private Map<String, Integer> pathContext;
    private GraphQLSchema schema;

    public static RuntimeWiring newEchoingWiring(GraphQLSchemaUtils schema, Ontology ontology, Query.Builder queryBuilder) {
        return newEchoingWiring(schema,x -> {
        }, ontology, queryBuilder);
    }

    public static RuntimeWiring newEchoingWiring(GraphQLSchemaUtils schema, Consumer<RuntimeWiring.Builder> builderConsumer, Ontology ontology, Query.Builder queryBuilder) {
        RuntimeWiring.Builder builder = RuntimeWiring.newRuntimeWiring();
        builderConsumer.accept(builder);
        return builder
                .wiringFactory(new TraversalWiringFactory(schema,ontology, queryBuilder))
                .build();
    }

    public TraversalWiringFactory(GraphQLSchemaUtils schemaUtils,Ontology ontology, Query.Builder builder) {
        this.schema = schemaUtils.getGraphQLSchema();
        this.builder = builder;
        this.accessor = new Ontology.Accessor(ontology);
        this.pathContext = new HashMap<>();
    }

    public Query.Builder getBuilder() {
        return builder;
    }

    @Override
    public boolean providesTypeResolver(InterfaceWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(InterfaceWiringEnvironment environment) {
        return env -> schema.getImplementations((GraphQLInterfaceType) env.getFieldType()).get(0);
    }

    @Override
    public boolean providesTypeResolver(UnionWiringEnvironment environment) {
        return true;
    }

    @Override
    public TypeResolver getTypeResolver(UnionWiringEnvironment environment) {
        return env -> env.getSchema().getQueryType();
    }

    @Override
    public DataFetcher getDefaultDataFetcher(FieldWiringEnvironment environment) {
        return env -> {
            GraphQLType fieldType = env.getFieldType();
            if (fieldType instanceof GraphQLList) {
                return Arrays.asList(getObject(env, ((GraphQLList) fieldType).getWrappedType()));
            } else {
                return getObject(env, fieldType);
            }
        };
    }

    private Object getObject(DataFetchingEnvironment env, GraphQLType fieldType) throws IOException {
        fieldType = extractConcreteFieldType(fieldType);
        // in parent is of type vertex and current query element not quant -> add quant
        ExecutionStepInfo parent = env.getExecutionStepInfo().getParent();
        if (parent.getFieldDefinition() != null) {
            ExecutionPath parentPath = env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd();
            if (isParentObjectType(parent.getFieldDefinition()) &&
                    //assume a path was already entered
                    pathContext.containsKey(parentPath.getPathWithoutListEnd().toString()) &&
                    //validate no quant related to the
                    !(builder.current(pathContext.get(parentPath.getPathWithoutListEnd().toString())) instanceof QuantBase)) {
                //todo add quant
                if(!(builder.current() instanceof QuantBase)) {
                    builder.quant(QuantType.all);
                }
                pathContext.put(parent.getPath().getPathWithoutListEnd().toString(), builder.currentIndex());
            }
        }

        if (fieldType instanceof GraphQLObjectType) {
            GraphQLObjectType type = (GraphQLObjectType) fieldType;
            GraphQLObjectType parentType = (GraphQLObjectType) parent.getType();
            //add the start query element
            if (parentType.getName().toLowerCase().equals(QUERY)) {
                builder.start();
            }
            //populate vertex or relation
            Optional<EntityType> realType = populateGraphObject(env, type.getName());
            addWhereClause(env,realType);
//            return fakeObjectValue(accessor, builder, (GraphQLObjectType) fieldType);
            return new Object();
            //todo create concrete union types from abstract interface
        } else if (fieldType instanceof GraphQLInterfaceType) {
            //select the first implementing of interface (no matter which one since all share same common fields)
            List<GraphQLObjectType> implementations = schema.getImplementations((GraphQLInterfaceType) fieldType);
            //populate vertex or relation
            Optional<EntityType> realType = populateGraphObject(env, ((GraphQLInterfaceType) fieldType).getName());
            addWhereClause(env, realType);
//            return fakeObjectValue(accessor, builder, implementations.get(0));
            return new Object();
        }
        //populate values
        if (fieldType instanceof GraphQLScalarType) {
            String name = populateGraphValue(env);
            return fakeScalarValue(name, (GraphQLScalarType) fieldType);
        } else if (fieldType instanceof GraphQLEnumType) {
            String name = populateGraphEnum(env);
            return fakeEnumValue(name, (GraphQLEnumType) fieldType);
        }

        return new Object();
    }

    /**
     *
     * @param env
     * @param realType
     * @throws IOException
     */
    private void addWhereClause(DataFetchingEnvironment env, Optional<EntityType> realType) throws IOException {
        //arguments
        if(realType.isPresent() && env.getArguments().containsKey(WHERE)) {
            Object argument = env.getArguments().get(WHERE);
            InputTypeWhereClause whereClause = mapper.readValue(mapper.writeValueAsString(argument), InputTypeWhereClause.class);
            //verify fields exist within entity type
            List<InputTypeConstraint> nonFoundFields = whereClause.getConstraints().stream()
                    .filter(c -> !realType.get().containsProperty(c.getOperand()))
                    .collect(Collectors.toList());

            if(!nonFoundFields.isEmpty())
                throw new IllegalArgumentException("Fields "+nonFoundFields +" are not a part of the queried entity "+realType.get().getName());

            //build the where clause query
            List<Tuple2<String, Optional<Constraint>>> constraints = whereClause.getConstraints().stream()
                    .map(c -> new Tuple2<>(c.getOperand(),
                            Optional.of(new Constraint(ConstraintOp.valueOf(c.getOperator()), c.getExpression()))))
                    .collect(Collectors.toList());


            //if no quant exists - add one
            if(!getBuilder().pop(eBase -> eBase instanceof QuantBase).isPresent()) {
                builder.quant(QuantType.all);
            }

            //add to group
            builder.ePropGroup(constraints,asQuantType(whereClause.getOperator()));

        }
    }

    private QuantType asQuantType(InputTypeWhereClause.WhereOperator operator) {
        switch (operator) {
            case AND: return QuantType.all;
            case OR: return QuantType.some;
            default:return QuantType.all;
        }
    }

    private boolean isParentObjectType(GraphQLFieldDefinition parentField) {
        return (extractConcreteFieldType(parentField.getType()) instanceof GraphQLInterfaceType) ||
                (extractConcreteFieldType(parentField.getType()) instanceof GraphQLObjectType);
    }

    /**
     * @param env
     * @return
     */
    private String populateGraphValue(DataFetchingEnvironment env) {
        //pop to the correct index according to path
        if (pathContext.containsKey(env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString())) {
            builder.currentIndex(pathContext.get(env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString()));
        }
        String name = env.getField().getName();
        Property property = accessor.property$(name);
        builder.eProp(property.getpType());
        return name;
    }

    /**
     * @param env
     * @return
     */
    private String populateGraphEnum(DataFetchingEnvironment env) {
        //pop to the correct index according to path
        if (pathContext.containsKey(env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString())) {
            builder.currentIndex(pathContext.get(env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString()));
        }
        Property enumProp = accessor.property$(env.getField().getName());
        builder.eProp(env.getField().getName());
        //select first value since no matter which value selected for mock data
        return accessor.enumeratedType$(enumProp.getType()).getValues().get(0).getName();
    }

    /**
     * get concrete friend type
     *
     * @param fieldType
     * @return
     */
    private GraphQLType extractConcreteFieldType(GraphQLType fieldType) {
        //list to wrapping type
        if (fieldType instanceof GraphQLList) {
            //inner field type
            fieldType = ((GraphQLList) fieldType).getWrappedType();
        }
        //non-null to wrapping type
        if (fieldType instanceof GraphQLNonNull) {
            //inner field type
            fieldType = ((GraphQLNonNull) fieldType).getWrappedType();
            //list to wrapping type
            if (fieldType instanceof GraphQLList) {
                //inner field type
                fieldType = ((GraphQLList) fieldType).getWrappedType();
            }
        }
        return fieldType;
    }

    private Optional<EntityType> populateGraphObject(DataFetchingEnvironment env, String typeName) {
        //pop to the correct index according to path
        if (pathContext.containsKey(env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString())) {
            builder.currentIndex(pathContext.get(env.getExecutionStepInfo().getParent().getPath().getPathWithoutListEnd().toString()));
        }

        if (accessor.relation(env.getField().getName()).isPresent()) {
            //relation
            RelationshipType relationshipType = accessor.relation$(env.getField().getName());
            builder.rel(relationshipType.getrType(), Rel.Direction.R, env.getField().getName());
            //right after will be the vertex
            EntityType entityType = accessor.entity$(typeName);
            builder.eType(entityType.geteType(), typeName);
            pathContext.put(env.getExecutionStepInfo().getPath().getPathWithoutListEnd().toString(), builder.currentIndex());
            return Optional.of(entityType);
        } else if (accessor.entity(typeName).isPresent()) {
            EntityType entityType = accessor.entity$(typeName);
            builder.eType(entityType.geteType(), typeName);
            pathContext.put(env.getExecutionStepInfo().getPath().getPathWithoutListEnd().toString(), builder.currentIndex());
            return Optional.of(entityType);
        }
        return Optional.empty();
    }


    @Deprecated
    private  Object fakeObjectValue(Ontology.Accessor accessor, Query.Builder builder, GraphQLObjectType fieldType) {
        Map<String, Object> map = new LinkedHashMap<>();

        if (!fieldType.getFieldDefinitions().isEmpty())
            builder.quant(QuantType.all);

        fieldType.getFieldDefinitions().forEach(fldDef -> {
            GraphQLOutputType innerFieldType = fldDef.getType();

            if (innerFieldType instanceof GraphQLNonNull) {
                innerFieldType = (GraphQLOutputType) ((GraphQLNonNull) innerFieldType).getWrappedType();
            }

            if (innerFieldType instanceof GraphQLList) {
                innerFieldType = (GraphQLOutputType) ((GraphQLList) innerFieldType).getWrappedType();
            }

            map.put(fldDef.getName(), getObject(accessor, builder, fieldType, fldDef, innerFieldType));
        });
        return map;
    }

    private  Object getObject(Ontology.Accessor accessor, Query.Builder builder, GraphQLObjectType fieldType, GraphQLFieldDefinition fldDef, GraphQLOutputType innerFieldType) {
        if (innerFieldType instanceof GraphQLObjectType) {
            RelationshipType relType = accessor.relation$(fldDef.getName());
            builder.rel(relType.getrType(), Rel.Direction.R, fieldType.getName());
            return new Object();
        } else if (innerFieldType instanceof GraphQLInterfaceType) {
            RelationshipType relType = accessor.relation$(fldDef.getName());
            builder.rel(relType.getrType(), Rel.Direction.R, fieldType.getName());
            //select first implementing concrete class since ....
            return new Object();
        } else if (innerFieldType instanceof GraphQLScalarType) {
            populateGraphValue(accessor, builder, fldDef);
            return fakeScalarValue(fldDef.getName(), (GraphQLScalarType) innerFieldType);
        } else if (innerFieldType instanceof GraphQLEnumType) {
            populateGraphValue(accessor, builder, fldDef);
            return fakeEnumValue(fldDef.getName(), (GraphQLEnumType) innerFieldType);
        }
        return null;
    }

    private  void populateGraphValue(Ontology.Accessor accessor, Query.Builder builder, GraphQLFieldDefinition fldDef) {
        Property property = accessor.property$(fldDef.getName());
        builder.eProp(property.getpType());
    }


    private  Object fakeScalarValue(String fieldName, GraphQLScalarType scalarType) {
        if (scalarType.equals(Scalars.GraphQLString)) {
            return fieldName;
        } else if (scalarType.equals(Scalars.GraphQLBoolean)) {
            return true;
        } else if (scalarType.equals(Scalars.GraphQLInt)) {
            return 1;
        } else if (scalarType.equals(Scalars.GraphQLFloat)) {
            return 1.0;
        } else if (scalarType.equals(Scalars.GraphQLID)) {
            return "id_" + fieldName;
        } else if (scalarType.equals(Scalars.GraphQLBigDecimal)) {
            return new BigDecimal(1.0);
        } else if (scalarType.equals(Scalars.GraphQLBigInteger)) {
            return new BigInteger("1");
        } else if (scalarType.equals(Scalars.GraphQLByte)) {
            return Byte.valueOf("1");
        } else if (scalarType.equals(Scalars.GraphQLShort)) {
            return Short.valueOf("1");
        } else {
            return null;
        }
    }

    private  Object fakeEnumValue(String fieldName, GraphQLEnumType enumType) {
        return fieldName;
    }

    public  GraphQLScalarType fakeScalar(String name) {
        return new GraphQLScalarType(name, name, new Coercing() {
            @Override
            public Object serialize(Object dataFetcherResult) {
                return dataFetcherResult;
            }

            @Override
            public Object parseValue(Object input) {
                return input;
            }

            @Override
            public Object parseLiteral(Object input) {
                return input;
            }
        });
    }
}


