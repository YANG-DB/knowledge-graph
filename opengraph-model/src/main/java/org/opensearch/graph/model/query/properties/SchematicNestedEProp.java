package org.opensearch.graph.model.query.properties;

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



import com.fasterxml.jackson.annotation.JsonInclude;
import org.opensearch.graph.model.query.properties.constraint.Constraint;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SchematicNestedEProp extends SchematicEProp implements NestingProp {

    public SchematicNestedEProp() {}

    public SchematicNestedEProp(int eNum, String pType, String schematicName, Constraint con, String path) {
        super(eNum, pType, schematicName, con);
        this.path = path;
    }

    public SchematicNestedEProp(SchematicEProp schematicEProp, String path) {
        this(schematicEProp.geteNum(), schematicEProp.getpType(), schematicEProp.getSchematicName(), schematicEProp.getCon(), path);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SchematicNestedEProp that = (SchematicNestedEProp) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), path);
    }

    @Override
    public SchematicNestedEProp clone() {
        return clone(geteNum());
    }

    @Override
    public SchematicNestedEProp clone(int eNum) {
        SchematicNestedEProp clone = new SchematicNestedEProp();
        clone.seteNum(eNum);
        clone.setF(getF());
        clone.setProj(getProj());
        clone.setCon(getCon());
        clone.setpTag(getpTag());
        clone.setpType(getpType());
        clone.path = path;
        return clone;
    }

    @Override
    public String getPath() {
        return path;
    }

    private String path;
}
