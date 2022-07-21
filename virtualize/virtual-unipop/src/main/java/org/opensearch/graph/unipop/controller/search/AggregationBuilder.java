package org.opensearch.graph.unipop.controller.search;


import javaslang.collection.Stream;
import org.apache.tinkerpop.gremlin.process.traversal.Compare;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.step.util.HasContainer;
import org.opensearch.script.Script;
import org.opensearch.search.aggregations.AbstractAggregationBuilder;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.aggregations.Aggregator;
import org.opensearch.search.aggregations.bucket.filter.FiltersAggregationBuilder;
import org.opensearch.search.aggregations.bucket.filter.FiltersAggregator;
import org.opensearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.opensearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.opensearch.search.aggregations.metrics.CardinalityAggregationBuilder;
import org.opensearch.search.aggregations.metrics.MaxAggregationBuilder;
import org.opensearch.search.aggregations.metrics.MinAggregationBuilder;
import org.opensearch.search.aggregations.metrics.ScriptedMetricAggregationBuilder;
import org.opensearch.search.aggregations.metrics.StatsAggregationBuilder;
import org.opensearch.search.aggregations.metrics.TopHitsAggregationBuilder;
import org.opensearch.search.aggregations.metrics.ValueCountAggregationBuilder;
import org.opensearch.search.aggregations.PipelineAggregatorBuilders;
import org.unipop.process.predicate.CountFilterP;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by lior.perry on 26/03/2017.
 */
public class AggregationBuilder implements Cloneable {


    public enum Op {
        root,
        param,
        filters,
        filter,
        terms,
        distinctFilter,
        countFilter,
        bucketFilter,
        min,
        max,
        avg,
        stats,
        cardinality,
        having,
        scripted_metric,
        topHits,
        innerAggregationBuilder
    }

    private enum FindMode {
        self,
        childrenOnly,
        selfWithChildren,
        full
    }

    //region Constructor
    public AggregationBuilder() {
        this.root = new RootComposite();
        this.current = this.root;
    }
    //endregion

    //region Public Methods
    public AggregationBuilder filters(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'filters' may only appear in the 'root', 'terms' or 'filters' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite filters = new FiltersComposite(name, this.current);
            this.current.getChildren().add(filters);
            this.current = filters;
        }

        return this;
    }

    public AggregationBuilder filter(String name, QueryBuilder queryBuilder) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'filter' may only appear in the 'root', 'terms' or 'filters' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite filter = new FilterComposite(name, this.current, queryBuilder);
            this.current.getChildren().add(filter);
            this.current = filter;
        }

        return this;
    }

    public AggregationBuilder terms(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'terms' may only appear in the 'root', 'terms' or 'filters' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite terms = new TermsComposite(name, this.current);
            this.current.getChildren().add(terms);
            this.current = terms;
        }

        return this;
    }

    public AggregationBuilder bucketFilter(String name, Compare compare) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'bucket-filter' may only appear in the 'terms', 'filters' or 'root' context");
        }
        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite filterComposite = new BucketFilterComposite(name, this.current);
            this.current.getChildren().add(filterComposite);
            this.current = filterComposite;
        }

        return this;
    }

    /**
     * count filter is a terms filter aggregation with a bucket selection sub aggregation
     * @param name
     * @return
     */
    public AggregationBuilder countFilter(String name) {
        //count filter is a terms filter aggregation with a bucket selection sub aggregation
        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite count = new CountFilterComposite(name, this.current);
            this.current.getChildren().add(count);
            this.current = count;
        }

        return this;
    }

    public AggregationBuilder min(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'min' may only appear in the 'terms', 'filters' or 'root' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite min = new MinComposite(name, this.current);
            this.current.getChildren().add(min);
            this.current = min;
        }

        return this;
    }

    public AggregationBuilder max(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'max' may only appear in the 'terms', 'filters' or 'root' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite max = new MaxComposite(name, this.current);
            this.current.getChildren().add(max);
            this.current = max;
        }

        return this;
    }

    public AggregationBuilder avg(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'avg' may only appear in the 'terms', 'filters' or 'root' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite avg = new AvgComposite(name, this.current);
            this.current.getChildren().add(avg);
            this.current = avg;
        }

        return this;
    }

    public AggregationBuilder stats(String name) {
        if (this.current.op == Op.root) {
            throw new UnsupportedOperationException("'stats' may not appear as root aggregation");
        }

        if (this.current.op != Op.terms) {
            throw new UnsupportedOperationException("'stats' may only appear in the 'terms' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite stats = new StatsComposite(name, this.current);
            this.current.getChildren().add(stats);
            this.current = stats;
        }

        return this;
    }

    public AggregationBuilder cardinality(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'avg' may only appear in the 'terms', 'filters' or 'root' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite cardinality = new CardinalityComposite(name, this.current);
            this.current.getChildren().add(cardinality);
            this.current = cardinality;
        }

        return this;
    }

    public AggregationBuilder scriptedMetric(String name) {
        /*if (this.current.op == Op.root) {
            throw new UnsupportedOperationException("'scriptedMetric' may not appear as root aggregation");
        } */

        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'scriptedMetric' may only appear in the 'terms', 'filters' or 'root' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite scriptedMetric = new ScriptedMetricComposite(name, this.current);
            this.current.getChildren().add(scriptedMetric);
            this.current = scriptedMetric;
        }

        return this;
    }

    public AggregationBuilder topHits(String name) {
        if (this.current.op != Op.terms && this.current.op != Op.filters && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'topHits' may only appear in the 'terms', 'filters' or 'root' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite topHits = new TopHitsComposite(name, this.current);
            this.current.getChildren().add(topHits);
            this.current = topHits;
        }

        return this;
    }

    public <V> AggregationBuilder param(String name, V value) {
        if (this.current == this.root) {
            throw new UnsupportedOperationException("parameters may not be added getTo the root aggregation");
        }

        if (seekLocalParam(this.current, name) != null) {
            seekLocalParam(this.current, name).setValue(value);
        } else {
            Composite param = new ParamComposite(name, value, this.current);
            this.current.getChildren().add(param);
        }

        return this;
    }

    public AggregationBuilder field(String value) {
        return this.param("field", value);
    }

    public AggregationBuilder operator(BiPredicate value) {
        return this.param("operator", value);
    }

    public AggregationBuilder operands(List<Object> value) {
        return this.param("operands", value);
    }

    public AggregationBuilder script(String value) {
        return this.param("script", value);
    }

    public AggregationBuilder size(int value) {
        return this.param("size", value);
    }

    public AggregationBuilder shardSize(int value) {
        return this.param("shard_size", value);
    }

    public AggregationBuilder executionHint(String value) {
        return this.param("execution_hint", value);
    }

    public AggregationBuilder precisionThreshold(long value) {
        return this.param("precision_threshold", value);
    }

    public AggregationBuilder collectMode(Aggregator.SubAggCollectionMode value) {
        return this.param("collect_mode", value);
    }

    public AggregationBuilder initScript(String value) {
        return this.param("init_script", value);
    }

    public AggregationBuilder mapScript(String value) {
        return this.param("map_script", value);
    }

    public AggregationBuilder combineScript(String value) {
        return this.param("combine_script", value);
    }

    public AggregationBuilder reduceScript(String value) {
        return this.param("reduce_script", value);
    }

    public AggregationBuilder fetchSource(boolean value) {
        return this.param("fetch_source", value);
    }

    public AggregationBuilder having(String name, BiPredicate predicate, Object value)       {
        if (this.current == this.root) {
            throw new UnsupportedOperationException("'having' may not be added getTo the root aggregation");
        }


        Composite having = new HavingComposite(new HasContainer(name, new P(predicate, value)), this.current);
        this.current.getChildren().add(having);

        return this;
    }

    public AggregationBuilder innerAggregationBuilder(String name, AggregationBuilder innerAggregationBuilder) {
        if (this.current.op != Op.terms && this.current.op != Op.root) {
            throw new UnsupportedOperationException("'innerAggregationBuilder' may only appear in the 'root' or 'terms' context");
        }

        if (this.seekLocalName(this.current, name) != null) {
            this.current = this.seekLocalName(this.current, name);
        } else {
            Composite innerAggregationBuilderComposite = new InnerAggregationBuilderComposite(name, innerAggregationBuilder, this.current);
            this.current.getChildren().add(innerAggregationBuilderComposite);
        }

        return this;
    }

    public Collection<Composite> find(String name) {
        return find(composite -> {
            if (composite == null) {
                return false;
            }

            return composite.getName() != null && composite.getName().equals(name);
        });
    }

    public AggregationBuilder seek(String name) {
        return seek(composite -> {
            if (composite == null) {
                return false;
            }

            return composite.getName() != null && composite.getName().equals(name);
        });
    }

    public AggregationBuilder seek(Composite compositeSeek) {
        return seek(composite -> {
            if (composite == null) {
                return false;
            }

            return composite.equals(compositeSeek);
        });
    }

    public AggregationBuilder seek(Predicate<Composite> predicate) {
        Collection<Composite> seek = this.root.find(predicate, FindMode.full);
        if (seek != null && seek.size() > 0) {
            this.current = seek.stream().findFirst().get();
        }

        return this;
    }

    public Collection<Composite> find(Predicate<Composite> predicate) {
        Collection<Composite> composites = this.root.find(predicate, FindMode.full);
        return composites;
    }

    public AggregationBuilder seekRoot() {
        this.current = this.root;
        return this;
    }

    public AggregationBuilder clear() {
        this.current.clear();
        return this;
    }

    public Iterable<org.opensearch.search.aggregations.AggregationBuilder> getAggregations() {
        return (Iterable< org.opensearch.search.aggregations.AggregationBuilder>)root.build();
    }

    // The clone will return a deep clone of the aggregation builder (except leaf values: e.g the Object value in terms composite).
    // The clone will set the current field getTo point getTo the root due getTo the difficulty in finding the cloned current composite in the clone AggregationBuilder.
    @Override
    public AggregationBuilder clone() {
        try {
            AggregationBuilder clone = (AggregationBuilder) super.clone();
            clone.root = root.clone();
            clone.current = clone.root;
            return clone;
        } catch(CloneNotSupportedException ex){
            return null;
        }
    }
    //endregion

    //region Properties
    public Composite getRoot() {
        return this.root;
    }

    public Composite getCurrent() {
        return this.current;
    }
    //endregion

    //region Private Methods
    private Composite seekLocalName(Composite composite, String name) {
        Optional<Composite> find = composite.find(childComposite -> {
            if (childComposite == null) {
                return false;
            }

            return childComposite.getName() != null && childComposite.getName().equals(name);
        }, FindMode.childrenOnly).stream().findFirst();

        if (find.isPresent()) {
            return find.get();
        } else {
            return null;
        }
    }

    private Composite seekLocalClass(Composite composite, Class<? extends Composite> compositeClass){
        Optional<Composite> find = composite.find(childComposite -> {
            if (childComposite == null) {
                return false;
            }

            return childComposite.getClass().equals(compositeClass);
        }, FindMode.childrenOnly).stream().findFirst();

        if (find.isPresent()) {
            return find.get();
        } else {
            return null;
        }
    }

    private ParamComposite seekLocalParam(Composite composite, String name) {
        Optional<ParamComposite> find = composite.find(childComposite -> {
            if (childComposite == null) {
                return false;
            }

            return childComposite.getName() != null && childComposite.getName().equals(name) &&
                    ParamComposite.class.isAssignableFrom(childComposite.getClass());
        }, FindMode.childrenOnly).stream().map(c -> (ParamComposite)c).findFirst();

        if (find.isPresent()) {
            return find.get();
        } else {
            return null;
        }
    }
    //endregion

    //region Fields
    private Composite root;
    private Composite current;
    //endregion

    //region Composite
    public abstract class Composite implements Cloneable {

        //region Constructor
        public Composite(String name, Op op, Composite parent) {
            this.name = name;
            this.op = op;
            this.parent = parent;

            this.children = new ArrayList<>();
        }
        //endregion

        //region Abstract Methods
        public abstract Object build();
        //endregion

        //region Protected Methods
        protected Collection<Composite> find(Predicate<Composite> predicate, FindMode findMode) {
            List<Composite> foundComposites = new ArrayList<>();

            if (findMode != FindMode.childrenOnly) {
                if (predicate.test(this)) {
                    foundComposites.add(this);
                }
            }

            if ((findMode == FindMode.childrenOnly || findMode == FindMode.selfWithChildren || findMode == FindMode.full)
                    && this.getChildren() != null) {
                for (Composite child : this.getChildren()) {
                    if (findMode == FindMode.full) {
                        foundComposites.addAll(child.find(predicate, findMode));
                    } else {
                        if (predicate.test(child)) {
                            foundComposites.add(child);
                        }
                    }
                }
            }

            return foundComposites;
        }

        protected void clear() {
            this.getChildren().clear();
        }

        @Override
        protected Composite clone() throws CloneNotSupportedException {
            Composite clone = (Composite) super.clone();
            clone.children = new ArrayList<>();
            for (Composite child : this.getChildren()) {
                Composite childClone = child.clone();

                clone.children.add(childClone);
                childClone.parent = clone;
            }

            return clone;
        }
        //endregion

        //region Properties
        public String getName() {
            return name;
        }

        public Op getOp() {
            return op;
        }

        public List<Composite> getChildren() {
            return children;
        }

        public Composite getParent() {
            return parent;
        }
        //endregion

        //region Fields
        private String name;
        private Op op;
        private Composite parent;

        private List<Composite> children;
        //endregion
    }

    public class RootComposite extends Composite {
        //region Constructor
        public RootComposite() {
            super(null, Op.root, null);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return this.getChildren().stream()
                    .map(child -> (org.opensearch.search.aggregations.AbstractAggregationBuilder) child.build()).collect(Collectors.toList());
        }
        //endregion
    }

    public abstract class HasAggregationsComposite extends Composite {
        //region Constructor
        public HasAggregationsComposite(String name, Op op, Composite parent) {
            super(name, op, parent);
        }
        //endregion

        //region Protected Methods
        public void applySubAggregationFromChild(org.opensearch.search.aggregations.AggregationBuilder aggregationBuilder, AggregationBuilder.Composite childComposite) {
            Object childAggregation = childComposite.build();

            if (AbstractAggregationBuilder.class.isAssignableFrom(childAggregation.getClass())) {
                AbstractAggregationBuilder childAggregationBuilder = (AbstractAggregationBuilder) childAggregation;
                if (childAggregationBuilder != null) {
                    aggregationBuilder.subAggregation(childAggregationBuilder);
                }
            } else if (Iterable.class.isAssignableFrom(childAggregation.getClass())) {
                Iterable<AbstractAggregationBuilder> childAggregationBuilders = (Iterable<AbstractAggregationBuilder>)childAggregation;
                for(AbstractAggregationBuilder childAggregationBuilder : childAggregationBuilders) {
                    if (childAggregationBuilder != null) {
                        aggregationBuilder.subAggregation(childAggregationBuilder);
                    }
                }
            }
        }
        //endregion
    }

    public class FiltersComposite extends HasAggregationsComposite {
        //region Constructor
        public FiltersComposite(String name, Composite parent) {
            super(name, Op.filters, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            Map<String, org.opensearch.index.query.QueryBuilder> filterMap = new HashMap<>();
            for (FilterComposite filter : this.getChildren().stream()
                    .filter(child -> FilterComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (FilterComposite) child).collect(Collectors.toList())) {

                org.opensearch.index.query.QueryBuilder filterBuilder = (org.opensearch.index.query.QueryBuilder)filter.queryBuilder.seekRoot().query().filtered().filter().getCurrent().build();
                filterMap.put(filter.getName(), filterBuilder);
            }

            FiltersAggregationBuilder filtersAggsBuilder = AggregationBuilders.filters(this.getName(),
            Stream.ofAll(filterMap.entrySet())
                    .map(entry -> new FiltersAggregator.KeyedFilter(entry.getKey(), entry.getValue()))
                    .toJavaArray(FiltersAggregator.KeyedFilter.class));

            for (Composite childComposite : this.getChildren().stream()
                    .filter(child -> !FilterComposite.class.isAssignableFrom(child.getClass()) &&
                            !ParamComposite.class.isAssignableFrom(child.getClass()) &&
                            !HavingComposite.class.isAssignableFrom(child.getClass())).collect(Collectors.toList())) {

                applySubAggregationFromChild(filtersAggsBuilder, childComposite);
            }

            return filtersAggsBuilder;
        }
        //endregion
    }

    public class FilterComposite extends Composite {
        //region Constructor
        public FilterComposite(String name, Composite parent, QueryBuilder queryBuilder) {
            super(name, Op.filter, parent);
            this.queryBuilder = queryBuilder;
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return AggregationBuilders.filter(this.getName(),
                    (org.opensearch.index.query.QueryBuilder)this.queryBuilder.seekRoot().query().filtered().filter().getCurrent().build());
        }

        @Override
        protected Composite clone() throws CloneNotSupportedException {
            FilterComposite clone = (FilterComposite) super.clone();
            clone.queryBuilder = this.queryBuilder.clone();

            return clone;
        }
        //endregion

        //region Fields
        private QueryBuilder queryBuilder;
        //endregion
    }

    public class TermsComposite extends HasAggregationsComposite {
        //region Constructor
        public TermsComposite(String name, Composite parent) {
            super(name, Op.terms, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            TermsAggregationBuilder terms = AggregationBuilders.terms(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        terms.field((String)param.getValue());
                        break;

                    case "size":
                        terms.size((int)param.getValue());
                        break;

                    case "shard_size":
                        terms.shardSize((int)param.getValue());
                        break;

                    case "execution_hint":
                        terms.executionHint((String)param.getValue());
                        break;

                    case "collect_mode":
                        terms.collectMode((Aggregator.SubAggCollectionMode)param.getValue());
                        break;
                }
            }

            for (Composite childComposite : this.getChildren().stream()
                    .filter(child -> !ParamComposite.class.isAssignableFrom(child.getClass()) &&
                            !HavingComposite.class.isAssignableFrom(child.getClass())).collect(Collectors.toList())) {

                applySubAggregationFromChild(terms, childComposite);
            }

            return terms;
        }
        //endregion
    }

    public class CountFilterComposite extends Composite {
        public static final String COUNT_FIELD = "_Count";
        private BiPredicate operator;
        private List<String> operands;

        //region Constructor
        public CountFilterComposite(String name, Composite parent) {
            super(name, Op.countFilter, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            String countFilterField = null;
            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        countFilterField = (String)param.getValue();
                        break;
                    case "operator":
                        operator = (BiPredicate) param.getValue();
                        break;
                    case "operands":
                        operands = (List<String>)param.getValue();
                        break;

                }
            }

            if (countFilterField != null) {
                String fieldName = (this.getName()+ COUNT_FIELD).replace(".","_");
                TermsAggregationBuilder aggregation = AggregationBuilders.terms(this.getName()).field(this.getName())
                        .subAggregation(PipelineAggregatorBuilders.bucketSelector(countFilterField,
                                Collections.singletonMap(fieldName,"_count"),
                                BuildFilterScript.script(fieldName, operator,operands)));
                return aggregation;
            }

            return this;
        }
        //endregion
    }
    public class BucketFilterComposite extends Composite {
        //region Constructor
        public BucketFilterComposite(String name, Composite parent) {
            super(name, Op.bucketFilter, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            String filterField = null;
            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        filterField = (String)param.getValue();
                        break;
                }
            }

            if (filterField != null) {
                ValueCountAggregationBuilder count = AggregationBuilders.count(this.getName());
                count.field(filterField);
                return count;
            }

            return null;
        }
        //endregion
    }

    public class MinComposite extends Composite {
        //region Constructor
        public MinComposite(String name, Composite parent) {
            super(name, Op.min, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            MinAggregationBuilder min = AggregationBuilders.min(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        min.field((String)param.getValue());
                }
            }

            return min;
        }
        //endregion
    }

    public class MaxComposite extends Composite {
        //region Constructor
        public MaxComposite(String name, Composite parent) {
            super(name, Op.max, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            MaxAggregationBuilder max = AggregationBuilders.max(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        max.field((String)param.getValue());
                }
            }

            return max;
        }
        //endregion
    }

    public class AvgComposite extends Composite {
        //region Constructor
        public AvgComposite(String name, Composite parent) {
            super(name, Op.avg, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            AvgAggregationBuilder avg = AggregationBuilders.avg(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        avg.field((String)param.getValue());
                    /*case "script":
                        avg.script((String)param.getValue());*/
                }
            }

            return avg;
        }
        //endregion
    }

    public class StatsComposite extends Composite {
        //region Constructor
        public StatsComposite(String name, Composite parent) {
            super(name, Op.stats, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            StatsAggregationBuilder stats = AggregationBuilders.stats(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        stats.field((String)param.getValue());
                }
            }

            return stats;
        }
        //endregion
    }

    public class CardinalityComposite extends Composite {
        //region Constructor
        public CardinalityComposite(String name, Composite parent) {
            super(name, Op.cardinality, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            CardinalityAggregationBuilder cardinality = AggregationBuilders.cardinality(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "field":
                        cardinality.field((String)param.getValue());
                        break;

                    /*case "script":
                        cardinality.script((String)param.getValue());
                        break;*/

                    case "precision_threshold":
                        cardinality.precisionThreshold((long)param.getValue());
                        break;
                }
            }

            return cardinality;
        }
        //endregion
    }

    public class ParamComposite<V> extends Composite {
        //region Constructor
        public ParamComposite(String name, V value, Composite parent) {
            super(name, Op.param, parent);
            this.value = value;
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return null;
        }
        //endregion

        //region Properties
        public V getValue() {
            return this.value;
        }

        public void setValue(V value) {
            this.value = value;
        }
        //endregion

        //region Fields
        private V value;
        //endregion
    }

    public class HavingComposite extends Composite {
        //region Constructor
        public HavingComposite(HasContainer hasContainer, Composite parent) {
            super(hasContainer.getKey(), Op.having, parent);
            this.hasContainer = hasContainer;
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return null;
        }
        //endregion

        //region Properties
        public HasContainer getHasContainer() {
            return this.hasContainer;
        }
        //endregion

        //region Fields
        private HasContainer hasContainer;
        //endregion
    }

    public class InnerAggregationBuilderComposite extends Composite {
        //region Constructor
        public InnerAggregationBuilderComposite(String name, AggregationBuilder innerAggregationBuilder, Composite parent) {
            super(name, Op.innerAggregationBuilder, parent);
            this.innerAggregationBuilder = innerAggregationBuilder;
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            return innerAggregationBuilder.getAggregations();
        }

        @Override
        protected Collection<Composite> find(Predicate<Composite> predicate, FindMode findMode) {
            Collection<Composite> composites = super.find(predicate, findMode);

            if (findMode == FindMode.full) {
                composites.addAll(this.innerAggregationBuilder.getRoot().find(predicate, findMode));
            }

            return composites;
        }

        @Override
        protected Composite clone() throws CloneNotSupportedException {
            InnerAggregationBuilderComposite clone = (InnerAggregationBuilderComposite) super.clone();
            clone.innerAggregationBuilder = this.innerAggregationBuilder.clone();

            return clone;
        }
        //endregion

        //region Fields
        private AggregationBuilder innerAggregationBuilder;
        //endregion
    }

    public class ScriptedMetricComposite extends Composite {
        //region Constructor
        public ScriptedMetricComposite(String name, Composite parent) {
            super(name, Op.scripted_metric, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            ScriptedMetricAggregationBuilder scriptedMetric = AggregationBuilders.scriptedMetric(this.getName());
            Map<String, Object> params = new HashMap<>();

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    /*case "init_script":
                        scriptedMetric.initScript((String)param.getValue());
                        break;

                    case "map_script":
                        scriptedMetric.mapScript((String)param.getValue());
                        break;

                    case "combine_script":
                        scriptedMetric.combineScript((String)param.getValue());
                        break;

                    case "reduce_script":
                        scriptedMetric.reduceScript((String)param.getValue());
                        break;*/

                    default:
                        params.put(param.getName(), param.getValue());
                        break;
                }
            }

            if (!params.isEmpty()) {
                scriptedMetric.params(params);
            }

            return scriptedMetric;
        }
        //endregion
    }

    public class TopHitsComposite extends Composite {
        //region Constructor
        public TopHitsComposite(String name, Composite parent) {
            super(name, Op.topHits, parent);
        }
        //endregion

        //region Composite Implementation
        @Override
        public Object build() {
            TopHitsAggregationBuilder topHitsBuilder = AggregationBuilders.topHits(this.getName());

            for (ParamComposite param : this.getChildren().stream()
                    .filter(child -> ParamComposite.class.isAssignableFrom(child.getClass()))
                    .map(child -> (ParamComposite) child).collect(Collectors.toList())) {
                switch (param.getName().toLowerCase()) {
                    case "size":
                        topHitsBuilder.size((int)param.getValue());
                        break;

                    case "fetch_source":
                        topHitsBuilder.fetchSource((boolean)param.getValue());
                        break;
                }
            }

            return topHitsBuilder;
        }
        //endregion
    }

    /**
     * generate script such as "def a=params.edgeCount; a > 405 && a < 567"
     */
    public static class BuildFilterScript {
        /**
         * generate script filter for compare assignment
         * @param field
         * @param operator
         * @param operands
         * @return
         */
        public static Script script(String field,BiPredicate operator, List operands) {
            String variable = "a";
            StringBuilder script = new StringBuilder("def " + variable + "=").append("params.").append(field).append(";");

            //construct filtering expression
            if(!Objects.isNull(operator)  && operator instanceof CountFilterP.CountFilterCompare) {
                script.append(variable);
                switch ((CountFilterP.CountFilterCompare)operator) {
                    case eq:
                        script.append("=");
                        break;
                    case lt:
                        script.append("<");
                        break;
                    case lte:
                        script.append("<=");
                        break;
                    case gt:
                        script.append(">");
                        break;
                    case gte:
                        script.append(">");
                        break;
                }
            }
            if(!operands.isEmpty()) {
                script.append(operands.get(0));
            }

            return new Script(script.toString());
        }
    }
}
