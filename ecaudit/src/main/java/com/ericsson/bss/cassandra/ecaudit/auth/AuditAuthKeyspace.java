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
package com.ericsson.bss.cassandra.ecaudit.auth;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

import org.apache.cassandra.auth.AuthKeyspace;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.locator.SimpleStrategy;

final class AuditAuthKeyspace
{
    static final String WHITELIST_TABLE_NAME_V1 = "role_audit_whitelists";
    static final String WHITELIST_TABLE_NAME_V2 = "role_audit_whitelists_v2";
    private static final String WHITELIST_TABLE_SCHEMA = "CREATE TABLE " + WHITELIST_TABLE_NAME_V2 + " ("
                                                         + "role text,"
                                                         + "resource text,"
                                                         + "operations set<text>,"
                                                         + "PRIMARY KEY(role, resource))";
    private static final String WHITELIST_TABLE_DESCRIPTION = "audit whitelist assigned to db roles";
    private static final int WHITELIST_TABLE_GC_GRACE_SECONDS = (int) TimeUnit.DAYS.toSeconds(90);
    private static final CFMetaData CREATE_ROLE_AUDIT_WHITELISTS =
    CFMetaData.compile(WHITELIST_TABLE_SCHEMA, AuthKeyspace.NAME)
              .comment(WHITELIST_TABLE_DESCRIPTION)
              .gcGraceSeconds(WHITELIST_TABLE_GC_GRACE_SECONDS);

    private AuditAuthKeyspace()
    {
        // Utility class
    }

    static KSMetaData metadata()
    {
        return new KSMetaData(AuthKeyspace.NAME, SimpleStrategy.class, ImmutableMap.of("replication_factor", "1"), true, Collections.singletonList(CREATE_ROLE_AUDIT_WHITELISTS));
    }
}
