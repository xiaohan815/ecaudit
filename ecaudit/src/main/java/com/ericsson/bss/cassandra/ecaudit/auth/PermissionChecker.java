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

import java.util.Set;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IAuthorizer;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.auth.Permission;
import org.apache.cassandra.auth.Resources;
import org.apache.cassandra.auth.RoleOptions;
import org.apache.cassandra.auth.RoleResource;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.exceptions.UnauthorizedException;

class PermissionChecker
{
    private IAuthorizer authorizer; // lazy initialization

    PermissionChecker()
    {
    }

    PermissionChecker(IAuthorizer authorizer)
    {
        this.authorizer = authorizer;
    }

    private IAuthorizer getAuthorizer()
    {
        if (authorizer == null)
        {
            return getAuthorizerSync();
        }

        return authorizer;
    }

    private synchronized IAuthorizer getAuthorizerSync()
    {
        if (authorizer == null)
        {
            authorizer = DatabaseDescriptor.getAuthorizer();
        }

        return authorizer;
    }

    void checkAlterRoleAccess(AuthenticatedUser performer, RoleResource role, RoleOptions options)
    {
        if (performer.isSuper())
        {
            return;
        }

        if (isPermissionRequired(performer, role, options) && !hasPermissionToAlterRole(performer, role))
        {
            throw new UnauthorizedException(String.format("User %s is not authorized to alter role %s",
                                                          performer.getName(), role.getRoleName()));
        }
    }

    private boolean isPermissionRequired(AuthenticatedUser performer, RoleResource role, RoleOptions options)
    {
        return isChangingPasswordOfOtherRole(performer, role, options) || isChangingRestrictedSettings(options);
    }

    private boolean isChangingPasswordOfOtherRole(AuthenticatedUser performer, RoleResource role, RoleOptions options)
    {
        return options.getPassword().isPresent() && !performer.getName().equals(role.getRoleName());
    }

    private boolean isChangingRestrictedSettings(RoleOptions options)
    {
        return options.getLogin().isPresent() || options.getSuperuser().isPresent();
    }

    private boolean hasPermissionToAlterRole(AuthenticatedUser performer, RoleResource roleResource)
    {
        return Resources.chain(roleResource)
                        .stream()
                        .map(resource -> getPermissions(performer, resource))
                        .flatMap(Set::stream)
                        .anyMatch(Permission.ALTER::equals);
    }

    private Set<Permission> getPermissions(AuthenticatedUser performer, IResource resource)
    {
        IAuthorizer currentAuthorizer = getAuthorizer();
        if (currentAuthorizer instanceof AuditAuthorizer)
        {
            return ((AuditAuthorizer) currentAuthorizer).realAuthorize(performer, resource);
        }
        else
        {
            return currentAuthorizer.authorize(performer, resource);
        }
    }
}
