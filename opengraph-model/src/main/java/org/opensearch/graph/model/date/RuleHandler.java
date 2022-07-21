package org.opensearch.graph.model.date;

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


import com.github.sisyphsu.retree.ReMatcher;

@FunctionalInterface
public interface RuleHandler {

    /**
     * Parse substring[from, to) of the specified string
     *
     * @param chars   The original string in char[]
     * @param matcher The underline ReMatcher
     * @param dt      DateTime to accept parsed properties.
     */
    void handle(CharSequence chars, ReMatcher matcher, DateBuilder dt);

}
