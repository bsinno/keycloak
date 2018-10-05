/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.storage.ldap.mappers;

import org.jboss.logging.Logger;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.ModelException;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.ldap.LDAPStorageProvider;
import org.keycloak.storage.ldap.idm.model.LDAPObject;
import org.keycloak.storage.ldap.idm.query.internal.LDAPQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:jean-loup.maillet@yesitis.fr">Jean-Loup Maillet</a>
 */
public class HardcodedLDAPGroupStorageMapper extends AbstractLDAPStorageMapper {

    private static final Logger logger = Logger.getLogger(HardcodedLDAPGroupStorageMapper.class);

    public static final String GROUP = "group";

    public HardcodedLDAPGroupStorageMapper(ComponentModel mapperModel, LDAPStorageProvider ldapProvider) {
        super(mapperModel, ldapProvider);
    }

    @Override
    public void beforeLDAPQuery(LDAPQuery query) {
    }

    @Override
    public UserModel proxy(LDAPObject ldapUser, UserModel delegate, RealmModel realm) {
        return new UserModelDelegate(delegate) {

            @Override
            public List<GroupModel> getGroups() {
                List<GroupModel> groups = new ArrayList<>(super.getGroups());

                GroupModel group = getGroup(realm);
                if (group != null) {
                    groups.add(group);
                }

                return groups;
            }

            @Override
            public boolean isMemberOf(GroupModel group) {
                return super.isMemberOf(group) || group.equals(getGroup(realm));
            }

            @Override
            public void leaveGroup(GroupModel group) {
                if (group.equals(getGroup(realm))) {
                    throw new ModelException("Not possible to delete group. It's hardcoded by LDAP mapper");
                } else {
                    super.leaveGroup(group);
                }
            }
        };
    }

    @Override
    public void onRegisterUserToLDAP(LDAPObject ldapUser, UserModel localUser, RealmModel realm) {

    }

    @Override
    public void onImportUserFromLDAP(LDAPObject ldapUser, UserModel user, RealmModel realm, boolean isCreate) {

    }

    private GroupModel getGroup(RealmModel realm) {
        String groupName = mapperModel.getConfig().getFirst(HardcodedLDAPGroupStorageMapper.GROUP);
        GroupModel group = KeycloakModelUtils.findGroupByPath(realm, groupName);
        if (group == null) {
            logger.warnf("Hardcoded group '%s' configured in mapper '%s' is not available anymore");
        }
        return group;
    }
}
