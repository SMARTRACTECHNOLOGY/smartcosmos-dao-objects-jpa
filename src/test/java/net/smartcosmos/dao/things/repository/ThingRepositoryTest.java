package net.smartcosmos.dao.things.repository;

import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.things.ThingsPersistenceTestApplication;
import net.smartcosmos.dao.things.domain.ThingEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
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
    final String type = "type";

    @Autowired
    ThingRepository repository;
    private UUID id;

    @Before
    public void setUp() throws Exception {
        id = UUID.randomUUID();

        ThingEntity entity = repository
                .save(ThingEntity.builder()
                    .id(id)
                    .tenantId(tenantId)
                    .type(type).build());
    }

    @After
    public void tearDown() throws Exception {
        repository.deleteAll();
    }

    @Test
    public void deleteByIdAndTenantIdAndType() throws Exception {
        List<ThingEntity> deleteList = repository.deleteByIdAndTenantIdAndType(id, tenantId, type);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(id, deleteList.get(0).getId());
    }

    @Test
    public void findByIdAndTenantId() throws Exception {
        assertTrue(this.repository.findByIdAndTenantIdAndType(id, tenantId, type).isPresent());
    }

    @Test
    public void findByTenantId() throws Exception {
        List<ThingEntity> entityList = repository.findByTenantId(tenantId);
        assertFalse(entityList.isEmpty());

        assertEquals(1, entityList.size());
        assertEquals(id, entityList.get(0).getId());
    }

    @Test
    public void findByTenantIdAndType() throws Exception {

        List<ThingEntity> entityList = repository.findByTenantIdAndType(tenantId, type);
        assertFalse(entityList.isEmpty());

        assertEquals(1, entityList.size());
        assertEquals(id, entityList.get(0).getId());
    }

    @Test
    public void findByIdInAndTenantId() throws Exception {

        List<UUID> uuids = new ArrayList<>();
        uuids.add(id);

        List<ThingEntity> entityList = repository.findByIdInAndTenantId(uuids, tenantId);
        assertFalse(entityList.isEmpty());

        assertEquals(1, entityList.size());
        assertEquals(id, entityList.get(0).getId());
    }

    @Test
    public void findByTenantIdPageable() throws Exception {

        final UUID tenantId = UUID.randomUUID();
        final int entityCount = 30;
        List<UUID> ids = new ArrayList<>();

        for (int i = 0; i < entityCount; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);

            ThingEntity entity = repository
                .save(ThingEntity.builder()
                    .id(id)
                    .tenantId(tenantId)
                    .type(type).build());
        }

        Page<ThingEntity> entityList = repository.findByTenantId(tenantId, new PageRequest(0, 1));
        assertFalse(entityList.getContent().isEmpty());

        assertEquals(1, entityList.getContent().size());
        assertTrue(ids.contains(entityList.getContent().get(0).getId()));
        assertEquals(entityCount, entityList.getTotalElements());
    }
}
