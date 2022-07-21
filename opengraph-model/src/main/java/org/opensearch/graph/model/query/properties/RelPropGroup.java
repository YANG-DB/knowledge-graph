package org.opensearch.graph.model.query.properties;



/*-
 *
 * RelPropGroup.java - opengraph-model - yangdb - 2,016
 * org.codehaus.mojo-license-maven-plugin-1.16
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2016 - 2019 yangdb   ------ www.yangdb.org ------
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
 *
 */

import org.opensearch.graph.model.query.quant.QuantType;
import javaslang.collection.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by benishue on 25-Apr-17.
 */
public class RelPropGroup extends BasePropGroup<RelProp, RelPropGroup> {
    //region Constructors
    public RelPropGroup() {
        super(Collections.emptyList());
    }

    public RelPropGroup(int eNum) {
        super(eNum);
    }

    public RelPropGroup(RelProp...props) {
        super(props);
    }

    public RelPropGroup(Iterable<RelProp> props) {
        super(0, props);
    }

    public RelPropGroup(int eNum, RelProp...props) {
        super(eNum, props);
    }

    public RelPropGroup(int eNum, Iterable<RelProp> props) {
        super(eNum, QuantType.all, props);
    }

    public RelPropGroup(int eNum, QuantType quantType, Iterable<RelProp> props) {
        super(eNum, quantType, props, Collections.emptyList());
    }

    public RelPropGroup(int eNum, QuantType quantType, RelProp ... props) {
        super(eNum, quantType, props);
    }

    public RelPropGroup(int eNum, QuantType quantType, Iterable<RelProp> props, Iterable<RelPropGroup> groups) {
        super(eNum, quantType, props, groups);
    }
    //endregion

    //region Override Methods
    @Override
    public RelPropGroup clone() {
        RelPropGroup propGroup = new RelPropGroup();
        propGroup.seteNum(geteNum());
        propGroup.props = new ArrayList<>(props);
        propGroup.groups = new ArrayList<>(groups);
        return propGroup;
    }

    @Override
    public RelPropGroup clone(int eNum) {
        AtomicInteger propsNum = new AtomicInteger(eNum);
        AtomicInteger groupsNum = new AtomicInteger(eNum);
        RelPropGroup propGroup = new RelPropGroup();
        propGroup.seteNum(eNum);
        propGroup.props = getProps().stream().map(p -> p.clone(propsNum.incrementAndGet())).collect(Collectors.toList());
        propGroup.groups = getGroups().stream().map(p -> p.clone(groupsNum.incrementAndGet())).collect(Collectors.toList());
        return propGroup;

    }
    //endregion

    public static RelPropGroup of(List<RelProp> props) {
        return new RelPropGroup(props);
    }

    public static RelPropGroup of(int eNum, RelProp...props) {
        return new RelPropGroup(eNum, props);
    }

    public static RelPropGroup of(int eNum, QuantType quantType, RelProp...props) {
        return new RelPropGroup(eNum, quantType, Stream.of(props));
    }

    public static RelPropGroup of(int eNum, RelPropGroup...groups) {
        return new RelPropGroup(eNum, QuantType.all, Collections.emptyList(), Stream.of(groups));
    }

    public static RelPropGroup of(int eNum, QuantType quantType, RelPropGroup...groups) {
        return new RelPropGroup(eNum, quantType, Stream.empty(), Stream.of(groups));
    }

    public static RelPropGroup of(int eNum, QuantType quantType, Iterable<RelProp> props, Iterable<RelPropGroup> groups) {
        return new RelPropGroup(eNum, quantType, props, groups);
    }
}
