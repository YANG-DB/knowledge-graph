package org.opensearch.graph.stats.model.histogram;

/*-
 * #%L
 * opengraph-statistics
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





import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.opensearch.graph.stats.model.enums.DataType;
import org.opensearch.graph.stats.model.enums.HistogramType;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "histogramType")
@JsonSubTypes({
        @JsonSubTypes.Type(name = "numeric", value = HistogramNumeric.class),
        @JsonSubTypes.Type(name = "string", value = HistogramString.class),
        @JsonSubTypes.Type(name = "manual", value = HistogramManual.class),
        @JsonSubTypes.Type(name = "term", value = HistogramTerm.class),
        @JsonSubTypes.Type(name = "composite", value = HistogramComposite.class),
        @JsonSubTypes.Type(name = "dynamic", value = HistogramDynamic.class)
})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Histogram {

    //region Ctrs
    Histogram() {
    }

    Histogram(HistogramType histogramType) {
        this.histogramType = histogramType;
    }

    public HistogramType getHistogramType() {
        return histogramType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }
    //endregion

    //region Fields
    private HistogramType histogramType;
    private DataType dataType;
    //endregion
}
