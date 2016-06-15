package net.smartcosmos.dao.objects.repository;

import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.objects.ObjectPersistenceTestApplication;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 *
 * Sometimes these runtime created methods have issues that don't come up until they're
 * actually called. It's a minor setback with Spring, one that just requires some diligent
 * testing.tenantId
 * @author voor
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ObjectPersistenceTestApplication.class,
        ThingPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class ObjectRepositoryTest {

    final UUID accountId = UUID.randomUUID();
    @Autowired
    ThingRepository objectRepository;
    private UUID id;

    @Before
    public void setUp() throws Exception {
        ThingEntity entity = objectRepository
                .save(ThingEntity.builder().objectUrn("urn").accountId(accountId)
                        .type("type").name("name").build());
        id = entity.getId();
    }

    @Test
    public void findByAccountIdAndObjectUrn() throws Exception {
        assertTrue(this.objectRepository
                .findByAccountIdAndObjectUrn(accountId, "urn").isPresent());
    }

    @Test
    public void findByAccountIdAndId() throws Exception {
        assertTrue(this.objectRepository.findByAccountIdAndId(accountId, id).isPresent());
    }

}
