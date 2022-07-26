package org.opensearch.graph.executor.ontology.schema.load;





import org.opensearch.graph.model.logical.LogicalGraphModel;
import org.opensearch.graph.model.results.LoadResponse;

import java.io.File;
import java.io.IOException;

public interface GraphDataLoader<S,F> {

    /**
     * load the given input json graph - all must comply with the ontology and physical schema bounded
     *
     * Example:
     * {
     *         "nodes": [
     *             {
     *                 "id": "0",
     *                 "metadata": {
     *                     "label": "person",
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": "first name",
     *                     "lName":"last name",
     *                     "born": "12/12/2000",
     *                     "age": "19",
     *                     "email": "myName@fuse.com",
     *                     "address": {
     *                             "state": "my state",
     *                             "street": "my street",
     *                             "city": "my city",
     *                             "zip": "gZip"
     *                     }
     *                 }
     *             },
     *             {
     *                 "id": "10",
 *                     "label": "person",
     *                 "metadata": {
     *                     "user-defined": "values"
     *                 }
     *                 "properties":{
     *                     "fName": "another first name",
     *                     "lName":"another last name",
     *                     "age": "20",
     *                     "born": "1/1/1999",
     *                     "email": "notMyName@fuse.com",
     *                     "address": {
     *                             "state": "not my state",
     *                             "street": "not my street",
     *                             "city": "not my city",
     *                             "zip": "not gZip"
     *                     }
     *                 }
     *             }
     *         ],
     *         "edges": [
     *             {
     *                 "id": 100,
     *                 "source": "0",
     *                 "target": "1",
     *                 "metadata": {
     *                     "label": "knows",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date":"01/01/2000",
     *                      "medium": "facebook"
     *                 }
     *             },
     *             {
     *                 "id": 101,
     *                 "source": "0",
     *                 "target": "1",
     *                 "metadata": {
     *                     "label": "called",
     *                     "user-defined": "values"
     *                 },
     *                 "properties":{
     *                      "date":"01/01/2000",
     *                      "duration":"120",
     *                      "medium": "cellular"
     *                      "sourceLocation": "40.06,-71.34"
     *                      "sourceTarget": "41.12,-70.9"
     *                 }
     *             }
     *         ]
     * }
     *
     * @param ontology
     * @param root
     * @param directive
     * @return
     * @throws IOException
     */
    LoadResponse<S, F> load(String ontology, LogicalGraphModel root, Directive directive) throws IOException;

    /**
     * does:
     *  - unzip file
     *  - split to multiple small files
     *  - for each file (in parallel)
     *      - convert into bulk set
     *      - commit to repository
     */
    LoadResponse<S, F> load(String ontology, File data, Directive directive) throws IOException;

    enum Directive {
        INSERT,UPSERT
    }

}
