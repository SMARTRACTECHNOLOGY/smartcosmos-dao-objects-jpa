package net.smartcosmos.dao.objects.repository;

import net.smartcosmos.dao.objects.ObjectPersistenceConfig;
import net.smartcosmos.dao.objects.ObjectPersistenceTestApplication;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
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
 * testing.accountId
 * @author voor
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ObjectPersistenceTestApplication.class,
        ObjectPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class ObjectRepositoryTest {

    final UUID accountId = UUID.randomUUID();
    @Autowired
    ObjectRepository objectRepository;
    private UUID id;

    @Before
    public void setUp() throws Exception {
        ObjectEntity entity = objectRepository
                .save(ObjectEntity.builder().objectUrn("objectUrn").accountId(accountId)
                        .type("type").name("name").build());
        id = entity.getId();
    }

    @Test
    public void findByAccountIdAndObjectUrn() throws Exception {
        assertTrue(this.objectRepository
                .findByAccountIdAndObjectUrn(accountId, "objectUrn").isPresent());
    }

    @Test
    public void findByAccountIdAndId() throws Exception {
        assertTrue(this.objectRepository.findByAccountIdAndId(accountId, id).isPresent());
    }

}
