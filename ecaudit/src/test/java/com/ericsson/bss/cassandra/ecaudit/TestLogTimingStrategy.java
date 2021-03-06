/*
 * Copyright 2019 Telefonaktiebolaget LM Ericsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ericsson.bss.cassandra.ecaudit;

import org.junit.Test;

import com.ericsson.bss.cassandra.ecaudit.common.record.Status;

import static com.ericsson.bss.cassandra.ecaudit.LogTimingStrategy.POST_LOGGING_STRATEGY;
import static com.ericsson.bss.cassandra.ecaudit.LogTimingStrategy.PRE_LOGGING_STRATEGY;
import static org.assertj.core.api.Assertions.assertThat;

public class TestLogTimingStrategy
{
    @Test
    public void testPreLoggingLogStatus()
    {
        assertThat(PRE_LOGGING_STRATEGY.shouldLogForStatus(Status.ATTEMPT)).isTrue();
        assertThat(PRE_LOGGING_STRATEGY.shouldLogForStatus(Status.SUCCEEDED)).isFalse();
        assertThat(PRE_LOGGING_STRATEGY.shouldLogForStatus(Status.FAILED)).isTrue();
    }

    @Test
    public void testPostLoggingLogStatus()
    {
        assertThat(POST_LOGGING_STRATEGY.shouldLogForStatus(Status.ATTEMPT)).isFalse();
        assertThat(POST_LOGGING_STRATEGY.shouldLogForStatus(Status.SUCCEEDED)).isTrue();
        assertThat(POST_LOGGING_STRATEGY.shouldLogForStatus(Status.FAILED)).isTrue();
    }

    @Test
    public void testShouldLogFailedBatchSummary()
    {
        assertThat(PRE_LOGGING_STRATEGY.shouldLogFailedBatchSummary()).isTrue();
        assertThat(POST_LOGGING_STRATEGY.shouldLogFailedBatchSummary()).isFalse();
    }
}
