package org.opensearch.graph.unipop.controller.utils.opensearch;

/*-
 * #%L
 * virtual-unipop
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





import org.opensearch.OpenSearchParseException;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.collect.Tuple;
import org.opensearch.common.compress.CompressorFactory;
import org.opensearch.common.compress.DeflateCompressor;
import org.opensearch.common.xcontent.*;
import org.opensearch.search.SearchHit;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class SearchHitUtils {
    public static Map<String, Object> convertToMap(SearchHit searchHit) {
        return convertToMap(searchHit.getSourceRef(), false, XContentType.JSON).v2();
    }

    public static Tuple<XContentType, Map<String, Object>> convertToMap(BytesReference bytes, boolean ordered, XContentType xContentType)
            throws OpenSearchParseException {
        try {
            final XContentType contentType;
            InputStream input;
            if (CompressorFactory.isCompressed(bytes)) {
                DeflateCompressor compressor = (DeflateCompressor) CompressorFactory.compressor(bytes);
                InputStream compressedStreamInput = compressor.threadLocalInputStream(bytes.streamInput());
                if (!compressedStreamInput.markSupported()) {
                    compressedStreamInput = new BufferedInputStream(compressedStreamInput);
                }
                input = compressedStreamInput;
            } else {
                input = bytes.streamInput();
            }
            contentType = xContentType != null ? xContentType : XContentFactory.xContentType(input);
            return new Tuple<>(Objects.requireNonNull(contentType), convertToMap(GraphJsonXContent.GRAPH_JSON_X_CONTENT, input, ordered));
        } catch (IOException e) {
            throw new OpenSearchParseException("Failed to parse content to map", e);
        }
    }

    public static Map<String, Object> convertToMap(XContent xContent, InputStream input, boolean ordered)
            throws OpenSearchParseException {
        // It is safe to use EMPTY here because this never uses namedObject
        try (XContentParser parser = xContent.createParser(NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, input)) {
            return ordered ? parser.mapOrdered() : parser.map();
        } catch (IOException e) {
            throw new OpenSearchParseException("Failed to parse content to map", e);
        }
    }
}
