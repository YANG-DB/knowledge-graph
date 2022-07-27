package org.opensearch.graph.services.controllers;

/*-
 * #%L
 * opengraph-services
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




import org.opensearch.graph.model.transport.ContentResponse;

import java.util.List;

/**
 * Created by roman.margolis on 20/03/2018.
 */
public interface IdGeneratorController<TId> {

    String IDGENERATOR_INDEX = ".idgenerator";

    ContentResponse<TId> getNext(String genName, int numIds);
    ContentResponse<Boolean> init(List<String> names);
}