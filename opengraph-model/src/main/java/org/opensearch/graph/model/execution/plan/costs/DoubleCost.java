package org.opensearch.graph.model.execution.plan.costs;

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






/**
 * Created by moti on 4/20/2017.
 */
public class DoubleCost implements Cost {
    //region Static
    public static DoubleCost of(double cost) {
        return new DoubleCost(cost);
    }
    //endregion

    //region Constructors
    public DoubleCost(double cost) {
        this.cost = cost;
    }
    //endregion

    //region Properties
    public double getCost() {
        return cost;
    }
    //endregion

    //region Override Methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoubleCost cost1 = (DoubleCost) o;

        return (Double.compare(cost1.cost, cost) == 0) ;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(cost);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "DoubleCost{" +
                "estimation=" + cost +
                '}';
    }
    //endregion

    //region Fields
    public double cost;
    //endregion
}
