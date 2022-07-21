package org.opensearch.graph.executor.cursor.discrete;


import org.opensearch.graph.model.asgQuery.AsgEBase;
import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.query.properties.CalculatedEProp;
import org.opensearch.graph.model.query.properties.EPropGroup;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class CalculatedFieldsUtil {

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
