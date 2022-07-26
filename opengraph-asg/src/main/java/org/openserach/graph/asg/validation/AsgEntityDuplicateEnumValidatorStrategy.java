package org.openserach.graph.asg.validation;







import org.opensearch.graph.model.asgQuery.AsgQuery;
import org.opensearch.graph.model.asgQuery.AsgQueryUtil;
import org.opensearch.graph.model.asgQuery.AsgStrategyContext;
import org.opensearch.graph.model.query.Rel;
import org.opensearch.graph.model.query.entity.EEntityBase;
import org.opensearch.graph.model.query.quant.QuantBase;
import org.opensearch.graph.model.validation.ValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opensearch.graph.model.asgQuery.AsgQueryUtil.nextDescendants;
import static org.opensearch.graph.model.validation.ValidationResult.OK;
import static java.util.stream.Collectors.groupingBy;

public class AsgEntityDuplicateEnumValidatorStrategy implements AsgValidatorStrategy {
    public static final String ERROR_1 = "Enum appears in more than one entity";

    @Override
    public ValidationResult apply(AsgQuery query, AsgStrategyContext context) {
        List<String> errors = new ArrayList<>();

        Map<Integer, List<Integer>> collect = AsgQueryUtil.eNums(query, asgEBase ->
                EEntityBase.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                        Rel.class.isAssignableFrom(asgEBase.geteBase().getClass()) ||
                        QuantBase.class.isAssignableFrom(asgEBase.geteBase().getClass()))
                            .stream().collect(groupingBy(Function.identity()));

        List<Map.Entry<Integer, List<Integer>>> inValid = collect.entrySet().stream().filter(v -> v.getValue().size() > 1)
                .collect(Collectors.toList());

        if (inValid.isEmpty())
            return OK;

        errors.add(ERROR_1 + ":" + inValid.stream().map(Map.Entry::getKey).collect(Collectors.toList()));

        return new ValidationResult(false, this.getClass().getSimpleName(), errors.toArray(new String[errors.size()]));
    }
    //endregion
}
