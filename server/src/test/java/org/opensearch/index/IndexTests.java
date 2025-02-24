/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * The OpenSearch Contributors require contributions made to
 * this file be licensed under the Apache-2.0 license or a
 * compatible open source license.
 */

/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * Modifications Copyright OpenSearch Contributors. See
 * GitHub history for details.
 */

package org.opensearch.index;

import org.opensearch.cluster.ClusterState;
import org.opensearch.common.UUIDs;
import org.opensearch.common.bytes.BytesReference;
import org.opensearch.common.xcontent.ToXContent;
import org.opensearch.common.xcontent.XContentBuilder;
import org.opensearch.common.xcontent.XContentParser;
import org.opensearch.common.xcontent.json.JsonXContent;
import org.opensearch.test.OpenSearchTestCase;

import java.io.IOException;

import static org.apache.lucene.util.TestUtil.randomSimpleString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

public class IndexTests extends OpenSearchTestCase {
    public void testToString() {
        assertEquals("[name/uuid]", new Index("name", "uuid").toString());
        assertEquals("[name]", new Index("name", ClusterState.UNKNOWN_UUID).toString());

        Index random = new Index(randomSimpleString(random(), 1, 100),
                usually() ? UUIDs.randomBase64UUID(random()) : ClusterState.UNKNOWN_UUID);
        assertThat(random.toString(), containsString(random.getName()));
        if (ClusterState.UNKNOWN_UUID.equals(random.getUUID())) {
            assertThat(random.toString(), not(containsString(random.getUUID())));
        } else {
            assertThat(random.toString(), containsString(random.getUUID()));
        }
    }

    public void testXContent() throws IOException {
        final String name = randomAlphaOfLengthBetween(4, 15);
        final String uuid = UUIDs.randomBase64UUID();
        final Index original = new Index(name, uuid);
        final XContentBuilder builder = JsonXContent.contentBuilder();
        original.toXContent(builder, ToXContent.EMPTY_PARAMS);
        try (XContentParser parser = createParser(JsonXContent.jsonXContent, BytesReference.bytes(builder))) {
            parser.nextToken(); // the beginning of the parser
            assertThat(Index.fromXContent(parser), equalTo(original));
        }
    }

    public void testEquals() {
        Index index1 = new Index("a", "a");
        Index index2 = new Index("a", "a");
        Index index3 = new Index("a", "b");
        Index index4 = new Index("b", "a");
        String s = "Some random other object";
        assertEquals(index1, index1);
        assertEquals(index1, index2);
        assertNotEquals(index1, null);
        assertNotEquals(index1, s);
        assertNotEquals(index1, index3);
        assertNotEquals(index1, index4);
    }
}
