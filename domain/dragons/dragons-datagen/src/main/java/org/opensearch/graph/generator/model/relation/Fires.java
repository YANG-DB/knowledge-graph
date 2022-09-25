package org.opensearch.graph.generator.model.relation;

/*-
 * #%L
 * dragons-datagen
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





import org.opensearch.graph.generator.model.enums.RelationType;

import java.util.Date;

/**
 * Created by benishue on 15-May-17.
 */
public class Fires extends RelationBase {

    //region Ctrs

    public Fires(String id, String source, String target, Date date, int temperature) {
        super(id, source, target, RelationType.FIRES);
        this.date = date;
        this.temperature = temperature;
    }
    //endregion

    //region Getters & Setters
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    //endregion

    //region Public Methods
    @Override
    public String[] getRecord() {
        return new String[]{this.getId(),
                this.getSource(),
                "Dragon",// source entity type
                this.getTarget(),
                "Dragon",// target entity type
                Long.toString(this.getDate().getTime()),
                Integer.toString(this.temperature)};
    }
    //endregion

    //region Fields
    private Date date;
    private int temperature;
    //endregion
}
