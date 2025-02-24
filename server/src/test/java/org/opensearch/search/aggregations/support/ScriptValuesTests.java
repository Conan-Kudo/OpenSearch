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
 *    http://www.apache.org/licenses/LICENSE-2.0
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

package org.opensearch.search.aggregations.support;

import com.carrotsearch.randomizedtesting.generators.RandomStrings;
import java.util.Collections;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.Scorable;
import org.apache.lucene.util.BytesRef;
import org.opensearch.common.Strings;
import org.opensearch.script.AggregationScript;
import org.opensearch.search.aggregations.support.values.ScriptBytesValues;
import org.opensearch.search.aggregations.support.values.ScriptDoubleValues;
import org.opensearch.search.aggregations.support.values.ScriptLongValues;
import org.opensearch.search.lookup.LeafSearchLookup;
import org.opensearch.search.lookup.SearchLookup;
import org.opensearch.test.OpenSearchTestCase;

import java.io.IOException;
import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ScriptValuesTests extends OpenSearchTestCase {

    private static class FakeAggregationScript extends AggregationScript {

        private final Object[][] values;
        int index;

        FakeAggregationScript(Object[][] values) {
            super(Collections.emptyMap(), new SearchLookup(null, null, Strings.EMPTY_ARRAY) {

                @Override
                public LeafSearchLookup getLeafSearchLookup(LeafReaderContext context) {
                    LeafSearchLookup leafSearchLookup = mock(LeafSearchLookup.class);
                    when(leafSearchLookup.asMap()).thenReturn(Collections.emptyMap());
                    return leafSearchLookup;
                }
            }, null);
            this.values = values;
            index = -1;
        }

        @Override
        public Object execute() {
            // Script values are supposed to support null, single values, arrays and collections
            final Object[] values = this.values[index];
            if (values.length <= 1 && randomBoolean()) {
                return values.length == 0 ? null : values[0];
            }
            return randomBoolean() ? values : Arrays.asList(values);
        }

        @Override
        public void setScorer(Scorable scorer) {
        }

        @Override
        public void setDocument(int doc) {
            index = doc;
        }

        @Override
        public long runAsLong() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double runAsDouble() {
            throw new UnsupportedOperationException();
        }

    }

    public void testLongs() throws IOException {
        final Object[][] values = new Long[randomInt(10)][];
        for (int i = 0; i < values.length; ++i) {
            Long[] longs = new Long[randomInt(8)];
            for (int j = 0; j < longs.length; ++j) {
                longs[j] = randomLong();
            }
            Arrays.sort(longs);
            values[i] = longs;
        }
        FakeAggregationScript script = new FakeAggregationScript(values);
        ScriptLongValues scriptValues = new ScriptLongValues(script);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i].length > 0, scriptValues.advanceExact(i));
            if (values[i].length > 0) {
                assertEquals(values[i].length, scriptValues.docValueCount());
                for (int j = 0; j < values[i].length; ++j) {
                    assertEquals(values[i][j], scriptValues.nextValue());
                }
            }
        }
    }

    public void testBooleans() throws IOException {
        final Object[][] values = new Boolean[randomInt(10)][];
        for (int i = 0; i < values.length; ++i) {
            Boolean[] booleans = new Boolean[randomInt(8)];
            for (int j = 0; j < booleans.length; ++j) {
                booleans[j] = randomBoolean();
            }
            Arrays.sort(booleans);
            values[i] = booleans;
        }
        FakeAggregationScript script = new FakeAggregationScript(values);
        ScriptLongValues scriptValues = new ScriptLongValues(script);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i].length > 0, scriptValues.advanceExact(i));
            if (values[i].length > 0) {
                assertEquals(values[i].length, scriptValues.docValueCount());
                for (int j = 0; j < values[i].length; ++j) {
                    assertEquals(values[i][j], scriptValues.nextValue() == 1L);
                }
            }
        }
    }

    public void testDoubles() throws IOException {
        final Object[][] values = new Double[randomInt(10)][];
        for (int i = 0; i < values.length; ++i) {
            Double[] doubles = new Double[randomInt(8)];
            for (int j = 0; j < doubles.length; ++j) {
                doubles[j] = randomDouble();
            }
            Arrays.sort(doubles);
            values[i] = doubles;
        }
        FakeAggregationScript script = new FakeAggregationScript(values);
        ScriptDoubleValues scriptValues = new ScriptDoubleValues(script);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i].length > 0, scriptValues.advanceExact(i));
            if (values[i].length > 0) {
                assertEquals(values[i].length, scriptValues.docValueCount());
                for (int j = 0; j < values[i].length; ++j) {
                    assertEquals(values[i][j], scriptValues.nextValue());
                }
            }
        }
    }

    public void testBytes() throws IOException {
        final String[][] values = new String[randomInt(10)][];
        for (int i = 0; i < values.length; ++i) {
            String[] strings = new String[randomInt(8)];
            for (int j = 0; j < strings.length; ++j) {
                strings[j] = RandomStrings.randomAsciiOfLength(random(), 5);
            }
            Arrays.sort(strings);
            values[i] = strings;
        }
        FakeAggregationScript script = new FakeAggregationScript(values);
        ScriptBytesValues scriptValues = new ScriptBytesValues(script);
        for (int i = 0; i < values.length; ++i) {
            assertEquals(values[i].length > 0, scriptValues.advanceExact(i));
            if (values[i].length > 0) {
                assertEquals(values[i].length, scriptValues.docValueCount());
                for (int j = 0; j < values[i].length; ++j) {
                    assertEquals(new BytesRef(values[i][j]), scriptValues.nextValue());
                }
            }
        }
    }

}
