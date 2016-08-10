package net.smartcosmos.dao.things.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.things.ThingsPersistenceTestApplication;
import net.smartcosmos.dao.things.domain.ThingEntity;

import static org.junit.Assert.*;

/**
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.tenantId
 *
 * @author voor
 */
@SuppressWarnings("Duplicates")
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
                      .type(type)
                      .build());
    }

    @After
    public void tearDown() throws Exception {

        repository.deleteAll();
    }

    @Test
    public void deleteByIdAndTenantIdAndType() throws Exception {

        List<ThingEntity> deleteList = repository.deleteByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

        assertFalse(deleteList.isEmpty());
        assertEquals(1, deleteList.size());
        assertEquals(id,
                     deleteList.get(0)
                         .getId());
    }

    @Test
    public void findByIdAndTenantId() throws Exception {

        assertTrue(this.repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type)
                       .isPresent());
    }

    @Test
    public void findByTenantId() throws Exception {

        Page<ThingEntity> entityPage = repository.findByTenantId(tenantId, null);
        assertFalse(entityPage.getContent()
                        .isEmpty());

        assertEquals(1,
                     entityPage.getContent()
                         .size());
        assertEquals(id,
                     entityPage.getContent()
                         .get(0)
                         .getId());
    }

    @Test
    public void findByTenantIdAndType() throws Exception {

        Page<ThingEntity> entityPage = repository.findByTenantIdAndTypeIgnoreCase(tenantId, type, null);
        assertFalse(entityPage.getContent()
                        .isEmpty());

        assertEquals(1,
                     entityPage.getContent()
                         .size());
        assertEquals(id,
                     entityPage.getContent()
                         .get(0)
                         .getId());
    }

    @Test
    public void findByIdInAndTenantId() throws Exception {

        List<UUID> uuids = new ArrayList<>();
        uuids.add(id);

        List<ThingEntity> entityList = repository.findByTenantIdAndTypeIgnoreCaseAndIdIn(tenantId, type, uuids);
        assertFalse(entityList.isEmpty());

        assertEquals(1, entityList.size());
        assertEquals(id,
                     entityList.get(0)
                         .getId());
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
                          .type(type)
                          .build());
        }

        Page<ThingEntity> entityList = repository.findByTenantId(tenantId, new PageRequest(0, 1));
        assertFalse(entityList.getContent()
                        .isEmpty());

        assertEquals(1,
                     entityList.getContent()
                         .size());
        assertTrue(ids.contains(entityList.getContent()
                                    .get(0)
                                    .getId()));
        assertEquals(entityCount, entityList.getTotalElements());
    }

    @Test
    public void findByTenantIdPageableAndSortableAsc() throws Exception {

        final UUID tenantId = UUID.randomUUID();
        final int entityCount = 30;
        List<UUID> ids = new ArrayList<>();
        final String typeA = "A";
        final String typeB = "B";

        for (int i = 0; i < entityCount; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);

            String type;
            if (i < 15) {
                type = typeA;
            } else {
                type = typeB;
            }

            ThingEntity entity = repository
                .save(ThingEntity.builder()
                          .id(id)
                          .type(type)
                          .tenantId(tenantId)
                          .build());
        }

        Page<ThingEntity> page1 = repository.findByTenantId(tenantId, new PageRequest(0, 5, Sort.Direction.ASC, "type"));
        assertFalse(page1.getContent()
                        .isEmpty());

        assertEquals(5,
                     page1.getContent()
                         .size());
        assertTrue(ids.subList(0, 15)
                       .contains(page1.getContent()
                                     .get(0)
                                     .getId()));
        assertTrue(ids.subList(0, 15)
                       .contains(page1.getContent()
                                     .get(4)
                                     .getId()));
        assertEquals(typeA,
                     page1.getContent()
                         .get(0)
                         .getType());
        assertEquals(typeA,
                     page1.getContent()
                         .get(4)
                         .getType());
        assertEquals(entityCount, page1.getTotalElements());

        Page<ThingEntity> page6 = repository.findByTenantId(tenantId, new PageRequest(5, 5, Sort.Direction.ASC, "type"));
        assertFalse(page6.getContent()
                        .isEmpty());

        assertEquals(5,
                     page6.getContent()
                         .size());
        assertTrue(ids.subList(15, 29)
                       .contains(page6.getContent()
                                     .get(0)
                                     .getId()));
        assertTrue(ids.subList(15, 29)
                       .contains(page6.getContent()
                                     .get(4)
                                     .getId()));
        assertEquals(typeB,
                     page6.getContent()
                         .get(0)
                         .getType());
        assertEquals(typeB,
                     page6.getContent()
                         .get(4)
                         .getType());
        assertEquals(entityCount, page6.getTotalElements());
    }

    @Test
    public void findByTenantIdPageableAndSortableDesc() throws Exception {

        final UUID tenantId = UUID.randomUUID();
        final int entityCount = 30;
        List<UUID> ids = new ArrayList<>();
        final String typeA = "A";
        final String typeB = "B";

        for (int i = 0; i < entityCount; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);

            String type;
            if (i < 15) {
                type = typeA;
            } else {
                type = typeB;
            }

            ThingEntity entity = repository
                .save(ThingEntity.builder()
                          .id(id)
                          .type(type)
                          .tenantId(tenantId)
                          .build());
        }

        Page<ThingEntity> page1 = repository.findByTenantId(tenantId, new PageRequest(0, 5, Sort.Direction.DESC, "type"));
        assertFalse(page1.getContent()
                        .isEmpty());

        assertEquals(5,
                     page1.getContent()
                         .size());
        assertTrue(ids.subList(15, 29)
                       .contains(page1.getContent()
                                     .get(0)
                                     .getId()));
        assertTrue(ids.subList(15, 29)
                       .contains(page1.getContent()
                                     .get(4)
                                     .getId()));
        assertEquals(typeB,
                     page1.getContent()
                         .get(0)
                         .getType());
        assertEquals(typeB,
                     page1.getContent()
                         .get(4)
                         .getType());
        assertEquals(entityCount, page1.getTotalElements());

        Page<ThingEntity> page6 = repository.findByTenantId(tenantId, new PageRequest(5, 5, Sort.Direction.DESC, "type"));
        assertFalse(page6.getContent()
                        .isEmpty());

        assertEquals(5,
                     page6.getContent()
                         .size());
        assertTrue(ids.subList(0, 15)
                       .contains(page6.getContent()
                                     .get(0)
                                     .getId()));
        assertTrue(ids.subList(0, 15)
                       .contains(page6.getContent()
                                     .get(4)
                                     .getId()));
        assertEquals(typeA,
                     page6.getContent()
                         .get(0)
                         .getType());
        assertEquals(typeA,
                     page6.getContent()
                         .get(4)
                         .getType());
        assertEquals(entityCount, page6.getTotalElements());
    }

    @Test
    public void findByTenantIdSortableDesc() throws Exception {

        final UUID tenantId = UUID.randomUUID();
        final int entityCount = 30;
        List<UUID> ids = new ArrayList<>();
        final String typeA = "A";
        final String typeB = "B";

        for (int i = 0; i < entityCount; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);

            String type;
            if (i < 15) {
                type = typeA;
            } else {
                type = typeB;
            }

            ThingEntity entity = repository
                .save(ThingEntity.builder()
                          .id(id)
                          .type(type)
                          .tenantId(tenantId)
                          .build());
        }

        Pageable pageable = new PageRequest(0, 100, Sort.Direction.DESC, "type");
        Page<ThingEntity> entityList = repository.findByTenantId(tenantId, pageable);
        assertFalse(entityList.getContent()
                        .isEmpty());

        assertEquals(entityCount, entityList.getTotalElements());
        assertEquals(typeB,
                     entityList.getContent()
                         .get(0)
                         .getType());
        assertEquals(typeB,
                     entityList.getContent()
                         .get(14)
                         .getType());

        assertEquals(typeA,
                     entityList.getContent()
                         .get(15)
                         .getType());
        assertEquals(typeA,
                     entityList.getContent()
                         .get(29)
                         .getType());
    }
}
