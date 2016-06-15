package net.smartcosmos.dao.things.repository;

import net.smartcosmos.dao.things.ThingsPersistenceTestApplication;
import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.things.domain.ThingEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 *
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.tenantId
 * @author voor
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ThingsPersistenceTestApplication.class,
        ThingPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class ThingRepositoryTest {

    final UUID tenantId = UUID.randomUUID();
    @Autowired
    ThingRepository repository;
    private UUID id;

    @Before
    public void setUp() throws Exception {
        ThingEntity entity = repository
                .save(ThingEntity.builder()
                    .urn("urn")
                    .tenantId(tenantId)
                    .type("type").build());

        id = entity.getId();
    }

    @Test
    public void deleteByTenantIdAndId() throws Exception {
        List<ThingEntity> deleteList = repository.deleteByTenantIdAndId(tenantId, id);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(id, deleteList.get(0).getId());
    }

    @Test
    public void deleteByTenantIdAndTypeAndUrn() throws Exception {
        List<ThingEntity> deleteList = repository.deleteByTenantIdAndTypeAndUrn(tenantId, "type", "urn");

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(id, deleteList.get(0).getId());
    }

    @Test
    public void findByAccountIdAndObjectUrn() throws Exception {
        assertTrue(this.repository
                .findByTenantIdAndUrn(tenantId, "urn").isPresent());
    }

    @Test
    public void findByAccountIdAndId() throws Exception {
        assertTrue(this.repository.findByTenantIdAndId(tenantId, id).isPresent());
    }

}
