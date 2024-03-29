package org.opensearch.graph.asg.validation;

/*-
 * #%L
 * opengraph-asg
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
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.EBase;
import org.opensearch.graph.model.query.entity.Typed;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.*;

import static org.opensearch.graph.model.validation.ValidationResult.OK;
import static java.util.stream.Collectors.groupingBy;

public class AsgEntityDuplicateETagValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "ETag %s appears in more than one entity with a different type (label)";
    public static final String ERROR_2 = "ETag %s cannot begin with any of the preserved symbols : ['_','$'] ";
    public static final String[] PRESERVED_LANGUAGE_TAG_SYMBOLS = {"_", "$"};

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();


        Map<String, List<AsgEBase<EBase>>> map = AsgQueryUtil.groupByTags(query.getStart());
        //dont allow preserved language etag symbols in name
        map.entrySet().stream()
                .filter(t -> Arrays.stream(PRESERVED_LANGUAGE_TAG_SYMBOLS).anyMatch(w->t.getKey().startsWith(w)))
                .anyMatch(p -> errors.add(String.format(ERROR_2, p.getKey())));

        map.entrySet().stream()
                .filter(e -> e.getValue().size() > 1)
                .forEach(e -> {
                    if (e.getValue()
                            .stream()
                            .filter(v -> v.geteBase() instanceof Typed)
                            .collect(groupingBy(o -> ((Typed) o.geteBase()).getTyped())).size() > 1) {
                        errors.add(String.format(ERROR_1, e.getKey()));
                    }
                });

        if (errors.isEmpty())
            return OK;


        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
