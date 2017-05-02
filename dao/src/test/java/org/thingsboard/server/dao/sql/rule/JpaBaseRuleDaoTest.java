/**
 * Copyright © 2016-2017 The Thingsboard Authors
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
package org.thingsboard.server.dao.sql.rule;

import com.datastax.driver.core.utils.UUIDs;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.thingsboard.server.common.data.id.RuleId;
import org.thingsboard.server.common.data.id.TenantId;
import org.thingsboard.server.common.data.page.TextPageLink;
import org.thingsboard.server.common.data.rule.RuleMetaData;
import org.thingsboard.server.dao.AbstractJpaDaoTest;
import org.thingsboard.server.dao.rule.RuleDao;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by Valerii Sosliuk on 4/30/2017.
 */
public class JpaBaseRuleDaoTest extends AbstractJpaDaoTest {

    @Autowired
    private RuleDao jpaBaseRuleDao;

    @Test
    @DatabaseSetup("classpath:dbunit/rule.xml")
    public void testFindRulesByPlugin() {
        assertEquals(3, jpaBaseRuleDao.findRulesByPlugin("token_1").size());
    }

    @Test
    @DatabaseSetup("classpath:dbunit/empty_dataset.xml")
    public void testFindByTenantIdAndPageLink() {
        UUID tenantId1 = UUIDs.timeBased();
        UUID tenantId2 = UUIDs.timeBased();
        createRulesTwoTenants(tenantId1, tenantId2, "name_", "token");
        List<RuleMetaData> rules1 = jpaBaseRuleDao.findByTenantIdAndPageLink(
                new TenantId(tenantId1), new TextPageLink(20, "name_"));
        assertEquals(20, rules1.size());

        List<RuleMetaData> rules2 = jpaBaseRuleDao.findByTenantIdAndPageLink(new TenantId(tenantId1),
                new TextPageLink(20, "name_", rules1.get(19).getId().getId(), null));
        assertEquals(10, rules2.size());

        List<RuleMetaData> rules3 = jpaBaseRuleDao.findByTenantIdAndPageLink(new TenantId(tenantId1),
                new TextPageLink(20, "name_", rules2.get(9).getId().getId(), null));
        assertEquals(0, rules3.size());
    }

    @Test
    @DatabaseSetup("classpath:dbunit/empty_dataset.xml")
    public void testFindAllTenantRulesByTenantId() {
        UUID tenantId1 = UUIDs.timeBased();
        UUID tenantId2 = UUIDs.timeBased();
        createTenantsAndSystemRules(tenantId1, tenantId2, "name_", "token");
        List<RuleMetaData> rules1 = jpaBaseRuleDao.findAllTenantRulesByTenantId(
                tenantId1, new TextPageLink(40, "name_"));
        assertEquals(40, rules1.size());

        List<RuleMetaData> rules2 = jpaBaseRuleDao.findAllTenantRulesByTenantId(tenantId1,
                new TextPageLink(40, "name_", rules1.get(39).getId().getId(), null));
        assertEquals(20, rules2.size());

        List<RuleMetaData> rules3 = jpaBaseRuleDao.findAllTenantRulesByTenantId(tenantId1,
                new TextPageLink(40, "name_", rules2.get(19).getId().getId(), null));
        assertEquals(0, rules3.size());
    }

    private void createRulesTwoTenants(UUID tenantId1, UUID tenantId2, String namePrefix, String pluginToken) {
        for (int i = 0; i < 30; i++) {
            createRule(tenantId1, namePrefix, pluginToken, i);
            createRule(tenantId2, namePrefix, pluginToken, i);
        }
    }

    private void createTenantsAndSystemRules(UUID tenantId1, UUID tenantId2, String namePrefix, String pluginToken) {
        for (int i = 0; i < 30; i++) {
            createRule(tenantId1, namePrefix, pluginToken, i);
            createRule(tenantId2, namePrefix, pluginToken, i);
            createRule(null, namePrefix, pluginToken, i);
        }
    }

    private void createRule(UUID tenantId, String namePrefix, String pluginToken, int i) {
        RuleMetaData ruleMetaData = new RuleMetaData();
        ruleMetaData.setId(new RuleId(UUIDs.timeBased()));
        ruleMetaData.setTenantId(new TenantId(tenantId));
        ruleMetaData.setName(namePrefix + i);
        ruleMetaData.setPluginToken(pluginToken);
        jpaBaseRuleDao.save(ruleMetaData);
    }
}
