/*
 * Copyright 2018 Telefonaktiebolaget LM Ericsson
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
package com.ericsson.bss.cassandra.ecaudit.filter.yaml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.ericsson.bss.cassandra.ecaudit.auth.ConnectionResource;
import com.ericsson.bss.cassandra.ecaudit.config.AuditConfig;
import com.ericsson.bss.cassandra.ecaudit.entry.AuditEntry;
import org.apache.cassandra.exceptions.ConfigurationException;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TestYamlAuditFilter
{
    @Mock
    private AuditConfig configMock;

    @Test
    public void testSetupDoNotFail()
    {
        YamlAuditFilter filter = givenConfiguredFilter();
        filter.setup();
    }

    @Test
    public void testWhitelistOnlyFiltersWhitelistedUsers()
    {
        YamlAuditFilter filter = givenConfiguredFilter();

        List<String> users = new ArrayList<>(Arrays.asList("foo", "User1", "bar", "User2", "fnord", "another"));

        assertThat(users.stream().map(TestYamlAuditFilter::toLogEntry).map(filter::isWhitelisted)
                .collect(Collectors.toList()))
                        .containsExactly(false, true, false, true, false, false);
    }

    @Test
    public void testWhitelistDoesntApplyToLoginAttempts()
    {
        YamlAuditFilter filter = givenConfiguredFilter();

        List<String> users = new ArrayList<>(Arrays.asList("foo", "User1", "bar", "User2", "fnord", "another"));

        assertThat(users.stream()
                .map(TestYamlAuditFilter::toLogEntry)
                .map(TestYamlAuditFilter::asLoginEntry)
                .map(filter::isWhitelisted)
                .collect(Collectors.toList()))
                .containsOnly(false);
    }

    @Test
    public void testExceptionOnConfigError()
    {
        when(configMock.getYamlWhitelist()).thenThrow(new ConfigurationException("something failed"));
        YamlAuditFilter filter = new YamlAuditFilter(configMock);

        assertThatExceptionOfType(ConfigurationException.class)
        .isThrownBy(filter::setup);
    }

    private YamlAuditFilter givenConfiguredFilter()
    {
        when(configMock.getYamlWhitelist()).thenReturn(Arrays.asList("User1", "User2"));

        YamlAuditFilter filter = new YamlAuditFilter(configMock);
        filter.setup();
        return filter;
    }

    private static AuditEntry toLogEntry(String user)
    {
        return AuditEntry.newBuilder()
                .user(user)
                .build();
    }

    private static AuditEntry asLoginEntry(AuditEntry entry)
    {
        return AuditEntry.newBuilder()
                .basedOn(entry)
                .resource(ConnectionResource.root())
                .build();
    }
}
