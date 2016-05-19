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
import org.junit.Assert;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author voor
 */
@SuppressWarnings("Duplicates")
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
    public void updateByObjectUrn() {

        String objectUrn = "urn:fakeUrn-update";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newName = "new name";

        ObjectCreate create = ObjectCreate.builder()
            .objectUrn(objectUrn)
            .name(name)
            .type(type)
            .description(description)
            .moniker(moniker)
            .build();

        ObjectResponse responseCreate = objectPersistenceService
            .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        Optional<ObjectEntity> entity = objectRepository
            .findByAccountIdAndObjectUrn(accountId, objectUrn);

        assertTrue(entity.isPresent());

        assertEquals(objectUrn, entity.get().getObjectUrn());
        assertEquals(objectUrn, responseCreate.getObjectUrn());

        ObjectUpdate update = ObjectUpdate.builder()
            .objectUrn(objectUrn)
            .name(newName)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(newName, responseUpdate.get().getName());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(moniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void updateByUrn() {

        String objectUrn = "urn:fakeUrn-update2";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newName = "new name";

        ObjectCreate create = ObjectCreate.builder()
            .objectUrn(objectUrn)
            .name(name)
            .type(type)
            .description(description)
            .moniker(moniker)
            .build();

        ObjectResponse responseCreate = objectPersistenceService
            .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        Optional<ObjectEntity> entity = objectRepository
            .findByAccountIdAndObjectUrn(accountId, objectUrn);

        assertTrue(entity.isPresent());

        assertEquals(objectUrn, entity.get().getObjectUrn());
        assertEquals(objectUrn, responseCreate.getObjectUrn());

        ObjectUpdate update = ObjectUpdate.builder()
            .urn(responseCreate.getUrn())
            .name(newName)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(newName, responseUpdate.get().getName());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(moniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void updateNonexistent() {
        ObjectUpdate update = ObjectUpdate.builder()
            .objectUrn("urn:DOES-NOT-EXIST")
            .name("new name")
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertFalse(responseUpdate.isPresent());
    }

    @Test
    public void updateInvalid() {

        String objectUrn = "urn:fakeUrn-update3";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newName = "new name";

        ObjectCreate create = ObjectCreate.builder()
            .objectUrn(objectUrn)
            .name(name)
            .type(type)
            .description(description)
            .moniker(moniker)
            .build();

        ObjectResponse responseCreate = objectPersistenceService
            .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        Optional<ObjectEntity> entity = objectRepository
            .findByAccountIdAndObjectUrn(accountId, objectUrn);

        assertTrue(entity.isPresent());

        assertEquals(objectUrn, entity.get().getObjectUrn());
        assertEquals(objectUrn, responseCreate.getObjectUrn());

        ObjectUpdate update = ObjectUpdate.builder()
            .name(newName)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertFalse(responseUpdate.isPresent());
    }

    @Test
    public void updateOverspecified() {

        String objectUrn = "urn:fakeUrn-update4";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newName = "new name";

        ObjectCreate create = ObjectCreate.builder()
            .objectUrn(objectUrn)
            .name(name)
            .type(type)
            .description(description)
            .moniker(moniker)
            .build();

        ObjectResponse responseCreate = objectPersistenceService
            .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        Optional<ObjectEntity> entity = objectRepository
            .findByAccountIdAndObjectUrn(accountId, objectUrn);

        assertTrue(entity.isPresent());

        assertEquals(objectUrn, entity.get().getObjectUrn());
        assertEquals(objectUrn, responseCreate.getObjectUrn());

        ObjectUpdate update = ObjectUpdate.builder()
            .urn(responseCreate.getUrn())
            .objectUrn(objectUrn)
            .name(newName)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertFalse(responseUpdate.isPresent());
    }

    @Test
    public void updateOverspecifiedConflict() {

        String objectUrn = "urn:fakeUrn-update5";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newName = "new name";

        ObjectCreate create = ObjectCreate.builder()
            .objectUrn(objectUrn)
            .name(name)
            .type(type)
            .description(description)
            .moniker(moniker)
            .build();

        ObjectResponse responseCreate = objectPersistenceService
            .create("urn:account:URN-IN-AUDIT-TRAIL", create);

        Optional<ObjectEntity> entity = objectRepository
            .findByAccountIdAndObjectUrn(accountId, objectUrn);

        assertTrue(entity.isPresent());

        assertEquals(objectUrn, entity.get().getObjectUrn());
        assertEquals(objectUrn, responseCreate.getObjectUrn());

        ObjectUpdate update = ObjectUpdate.builder()
            .urn(responseCreate.getUrn())
            .objectUrn("urn:fakeUrn-update-different")
            .name(newName)
            .build();

        try {
            Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);
            Assert.fail();
        } catch (IllegalArgumentException e) {
            assertEquals("urn and objectUrn do not match", e.getMessage());
        }
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
