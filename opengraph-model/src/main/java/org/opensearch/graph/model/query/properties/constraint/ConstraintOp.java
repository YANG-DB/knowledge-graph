package org.opensearch.graph.model.query.properties.constraint;

/*-
 * #%L
 * opengraph-model
 * %%
 * Copyright (C) 2016 - 2022 org.opensearch
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */






import com.fasterxml.jackson.annotation.JsonProperty;
import javaslang.collection.Stream;

import java.util.Set;

/**
 * Created by lior.perry on 23/02/2017.
 */
public enum ConstraintOp {

    @JsonProperty("empty")
    empty,

    @JsonProperty("not empty")
    notEmpty,

    @JsonProperty("eq")
    eq,

    @JsonProperty("ne")
    ne,

    @JsonProperty("gt")
    gt,

    @JsonProperty("ge")
    ge,

    @JsonProperty("lt")
    lt,

    @JsonProperty("le")
    le,

    @JsonProperty("in set")
    inSet,

    @JsonProperty("not in set")
    notInSet,

    @JsonProperty("in range")
    inRange,

    @JsonProperty("within")
    within,

    @JsonProperty("not in range")
    notInRange,

    @JsonProperty("contains")
    contains,

    @JsonProperty("distinct")
    distinct,

    @JsonProperty("not contains")
    notContains,

    @JsonProperty("starts with")
    startsWith,

    @JsonProperty("not starts with")
    notStartsWith,

    @JsonProperty("ends with")
    endsWith,

    @JsonProperty("not ends with")
    notEndsWith,

    @JsonProperty("match")
    match,

    @JsonProperty("match_phrase")
    match_phrase,

    @JsonProperty("query_string")
    query_string,

    @JsonProperty("not match")
    notMatch,

    @JsonProperty("fuzzy eq")
    fuzzyEq,

    @JsonProperty("fuzzy ne")
    fuzzyNe,

    @JsonProperty("like")
    like,

    @JsonProperty("like any")
    likeAny;

    public static Set<Class<? extends Constraint>> ignorableConstraints;
    public static Set<ConstraintOp> noValueOps;
    public static Set<ConstraintOp> singleValueOps;
    public static Set<ConstraintOp> multiValueOps;
    public static Set<ConstraintOp> exactlyTwoValueOps;

    static {
        ignorableConstraints = Stream.of(ParameterizedConstraint.class,
                JoinParameterizedConstraint.class,
                InnerQueryConstraint.class).toJavaSet();

        noValueOps = Stream.of(empty,notEmpty).toJavaSet();

        singleValueOps = Stream.of(eq, ne, gt, ge, lt, le, contains, startsWith, notContains, notStartsWith, notEndsWith,
                fuzzyEq, fuzzyNe, match, match_phrase, notMatch, empty, notEmpty, query_string).toJavaSet();

        multiValueOps = Stream.of(inRange, notInRange, inSet, notInSet, likeAny).toJavaSet();

        exactlyTwoValueOps = Stream.of(inRange, notInRange).toJavaSet();
    }

}
