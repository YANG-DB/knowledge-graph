package org.opensearch.graph.executor.cursor.discrete;

/*-
 * #%L
 * virtual-core
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

import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EPropGroup;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CalculatedFieldsUtil {

    /**
     * find calculated eProps under groups which are direct Descendant of a given Entity enum
     * We assume the calculated property is within the first level of the Property group since ASG strategy will flatten any
     * inner hierarchy groups
     *
     * @param query
     * @param eTag
     * @return
     */
    public static List<CalculatedEProp> findCalculaedFields(AsgQuery query, String eTag) {
        final List<AsgEBase<EPropGroup>> groups = AsgQueryUtil.nextDescendants(AsgQueryUtil.getByTag(query.getStart(), eTag).get(), EPropGroup.class);
        if (groups.isEmpty())
            return Collections.emptyList();

        //find all calculated fields
        return groups.stream()
                .flatMap(group -> group.geteBase().getProps().stream())
                .filter(prop -> CalculatedEProp.class.isAssignableFrom(prop.getClass()))
                .map(prop -> (CalculatedEProp) prop)
                .collect(Collectors.toList());
    }
}
