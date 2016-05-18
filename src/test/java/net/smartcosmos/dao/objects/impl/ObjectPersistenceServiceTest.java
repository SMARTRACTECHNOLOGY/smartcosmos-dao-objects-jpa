package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.ObjectPersistenceConfig;
import net.smartcosmos.dao.objects.ObjectPersistenceTestApplication;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.util.UuidUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author voor
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ObjectPersistenceTestApplication.class,
        ObjectPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class ObjectPersistenceServiceTest {

    private final UUID accountId = UUID.randomUUID();

    private final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountId);

    @Autowired
    ObjectPersistenceService objectPersistenceService;

    @Autowired
    IObjectRepository objectRepository;

    @Before
    public void setUp() throws Exception {

        // Need to mock out user for conversion service.
        // Might be a good candidate for a test package util.
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal())
                .thenReturn(new SmartCosmosUser(accountUrn, "urn:userUrn", "username",
                        "password", Arrays.asList(new SimpleGrantedAuthority("USER"))));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

    }

    @Test
    public void create() throws Exception {
        ObjectCreate create = ObjectCreate.builder().objectUrn("urn:fakeUrn")
                .moniker("moniker").description("description").name("name").type("type")
                .build();
        ObjectResponse response = objectPersistenceService
                .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        Optional<ObjectEntity> entity = objectRepository
                .findByAccountIdAndObjectUrn(accountId, "urn:fakeUrn");

        assertTrue(entity.isPresent());

        assertEquals("urn:fakeUrn", entity.get().getObjectUrn());
        assertEquals("urn:fakeUrn", response.getObjectUrn());
    }

    @Test
    public void update() {
        ObjectCreate create = ObjectCreate.builder().objectUrn("urn:fakeUrn-update")
            .moniker("moniker").description("description").name("name").type("type")
            .build();
        ObjectResponse responseCreate = objectPersistenceService
            .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        System.out.println(responseCreate.getAccountUrn());
        System.out.println(accountId);

        Optional<ObjectEntity> entity = objectRepository
            .findByAccountIdAndObjectUrn(accountId, "urn:fakeUrn-update");

        assertTrue(entity.isPresent());

        assertEquals("urn:fakeUrn-update", entity.get().getObjectUrn());
        assertEquals("urn:fakeUrn-update", responseCreate.getObjectUrn());

        ObjectUpdate update = ObjectUpdate.builder().objectUrn("urn:fakeUrn-update").name("new name").build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals("urn:fakeUrn-update", responseUpdate.get().getObjectUrn());
        assertEquals("new name", responseUpdate.get().getName());
    }

    @Test
    public void findByObjectUrn() throws Exception {

        final UUID accountUuid = UUID.randomUUID();
        final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountUuid);

        ObjectEntity entity = ObjectEntity.builder().accountId(accountUuid)
                .objectUrn("objectUrn").name("my object name").type("some type").build();

        this.objectRepository.save(entity);

        Optional<ObjectResponse> response = objectPersistenceService
                .findByObjectUrn(accountUrn, "objectUrn");

        assertTrue(response.isPresent());
    }

    @Test
    public void getObjects() throws Exception {

        // TODO to.. do...

    }

    @Test
    public void findByQueryParameters() throws Exception {
        // TODO to.. do...
    }

}
