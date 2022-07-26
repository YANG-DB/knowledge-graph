package org.opensearch.graph.generator.model.relation;





import org.opensearch.graph.generator.model.enums.EntityType;
import org.opensearch.graph.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 05-Jun-17.
 */
public class Owns extends RelationBase {

    //region Ctrs
    public Owns(String id, EntityType entityType, String source, String target, Date since, Date till) {
        super(id, source, target, RelationType.OWNS);
        this.entityType = entityType;
        this.since = since;
        this.till = till;
    }
    //endregion

    //region Getters & Setters
    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Date getTill() {
        return till;
    }

    public void setTill(Date till) {
        this.till = till;
    }

    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{
                this.getId(),
                this.getSource(),
                entityType.name(),//source entity type
                this.getTarget(),
                "Person",// target entity type
                Long.toString(this.getSince().getTime()),
                Long.toString(this.getTill().getTime())
        };
    }
    //endregion

    private EntityType entityType;
    //region Fields
    private Date since;
    private Date till;
    //endregion
}

