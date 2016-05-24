package net.smartcosmos.dao.objects.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
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

import net.smartcosmos.dao.objects.IObjectDao.QueryParameterType;
import net.smartcosmos.dao.objects.ObjectPersistenceConfig;
import net.smartcosmos.dao.objects.ObjectPersistenceTestApplication;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.util.UuidUtil;
import javax.validation.ConstraintViolationException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static net.smartcosmos.dao.objects.IObjectDao.QueryParameterType.MODIFIED_AFTER;
import static net.smartcosmos.dao.objects.IObjectDao.QueryParameterType.MONIKER_LIKE;
import static net.smartcosmos.dao.objects.IObjectDao.QueryParameterType.TYPE;
import static org.junit.Assert.*;
import static org.junit.Assert.*;

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

    public static final int DELAY_BETWEEN_LAST_MODIFIED_DATES = 10;
    public static final String OBJECT_URN_QUERY_PARAMS_01 = "objectUrnQueryParams01";
    public static final String OBJECT_URN_QUERY_PARAMS_02 = "objectUrnQueryParams02";
    public static final String OBJECT_URN_QUERY_PARAMS_03 = "objectUrnQueryParams03";
    public static final String OBJECT_URN_QUERY_PARAMS_04 = "objectUrnQueryParams04";
    public static final String OBJECT_URN_QUERY_PARAMS_05 = "objectUrnQueryParams05";
    public static final String OBJECT_URN_QUERY_PARAMS_06 = "objectUrnQueryParams06";
    public static final String OBJECT_URN_QUERY_PARAMS_07 = "objectUrnQueryParams07";
    public static final String OBJECT_URN_QUERY_PARAMS_08 = "objectUrnQueryParams08";
    public static final String OBJECT_URN_QUERY_PARAMS_09 = "objectUrnQueryParams09";
    public static final String OBJECT_URN_QUERY_PARAMS_10 = "objectUrnQueryParams10";
    public static final String OBJECT_URN_QUERY_PARAMS_11 = "objectUrnQueryParams11";
    public static final String OBJECT_URN_QUERY_PARAMS_12 = "objectUrnQueryParams12";
    public static final String NAME_ONE = "name one";
    public static final String TYPE_ONE = "type one";
    public static final String NAME_TWO = "name two";
    public static final String NAME_THREE = "name three";
    public static final String TYPE_TWO = "type two";
    public static final String WHATEVER = "whatever";
    public static final String MONIKER_ONE = "moniker one";
    public static final String MONIKER_TWO = "moniker two";
    public static final String MONIKER_THREE = "moniker three";
    public static final String OBJECT_URN_QUERY_PARAMS = "objectUrnQueryParams";
    public static final String OBJECT_URN_QUERY_PARAMS_0 = "objectUrnQueryParams0";
    public static final String OBJECT_URN_QUERY_PARAMS_1 = "objectUrnQueryParams1";
    public static final String OBJECT_URN_QUERY_PARAMS_99 = "objectUrnQueryParams99";
    public static final String BJECT_URN_QUERY_PARAMS = "bjectUrnQueryParams";
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

    @After
    public void tearDown() throws Exception {
        objectRepository.deleteAll();
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

    // region Update

    @Test
    public void thatUpdateByObjectUrnSucceeds() {

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
    public void thatUpdateByUrnSucceeds() {

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
    public void thatUpdateTypeSucceeds() {

        String objectUrn = "urn:fakeUrn-update7";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newType = "new type";

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
            .type(newType)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(name, responseUpdate.get().getName());
        assertEquals(newType, responseUpdate.get().getType());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(moniker, responseUpdate.get().getMoniker());
    }

    @Test(expected=ConstraintViolationException.class)
    public void thatUpdateEmptyTypeFails() {

        String objectUrn = "urn:fakeUrn-update12";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newType = "";

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
            .type(newType)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(name, responseUpdate.get().getName());
        assertEquals(newType, responseUpdate.get().getType());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(moniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void thatUpdateDescriptionSucceeds() {

        String objectUrn = "urn:fakeUrn-update8";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newDescription = "new description";

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
            .description(newDescription)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(name, responseUpdate.get().getName());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(newDescription, responseUpdate.get().getDescription());
        assertEquals(moniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void thatUpdateEmptyDescriptionSucceeds() {

        String objectUrn = "urn:fakeUrn-update9";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newDescription = "";

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
            .description(newDescription)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(name, responseUpdate.get().getName());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(newDescription, responseUpdate.get().getDescription());
        assertEquals(moniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void thatUpdateMonikerSucceeds() {

        String objectUrn = "urn:fakeUrn-update10";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newMoniker = "new moniker";

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
            .moniker(newMoniker)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(name, responseUpdate.get().getName());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(newMoniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void thatUpdateEmptyMonikerSucceeds() {

        String objectUrn = "urn:fakeUrn-update11";
        String name = "name";
        String type = "type";
        String description = "description";
        String moniker = "moniker";

        String newMoniker = "";

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
            .moniker(newMoniker)
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(objectUrn, responseUpdate.get().getObjectUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(description, responseUpdate.get().getDescription());
        assertEquals(newMoniker, responseUpdate.get().getMoniker());
    }

    @Test
    public void thatUpdateNonexistentFails() {
        ObjectUpdate update = ObjectUpdate.builder()
            .objectUrn("urn:DOES-NOT-EXIST")
            .name("new name")
            .build();
        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);

        assertFalse(responseUpdate.isPresent());
    }

    @Test(expected=ConstraintViolationException.class)
    public void thatUpdateWithoutIdThrowsException() {

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
    }

    @Test(expected=ConstraintViolationException.class)
    public void thatUpdateByOverspecifiedIdThrowsException() {

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
    }

    @Test(expected=ConstraintViolationException.class)
    public void thatUpdateByOverspecifiedAndConflictingIdThrowsException() {

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

        Optional<ObjectResponse> responseUpdate = objectPersistenceService.update(accountUrn, update);
    }

    // endregion

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

    // no query data should return an empty response
    @Test
    public void findByQueryParameters_NoQueryParameters() throws Exception {

        Map<QueryParameterType, Object> queryParams = new HashMap<>();
        int expectedSize = 0;
        int actualSize = 0;

        expectedSize = 0;
        List<ObjectResponse> response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        // should also be OK to pass in a null as parameter map
        expectedSize = 0;
        response = objectPersistenceService.findByQueryParameters(accountUrn, null);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);
    }

    @Test
    public void findByQueryParameters_ObjectUrnLike() throws Exception {
        populateQueryData();

        Map<QueryParameterType, Object> queryParams = new HashMap<>();
        int expectedSize = 0;
        int actualSize = 0;

        expectedSize = 12;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, OBJECT_URN_QUERY_PARAMS);
        List<ObjectResponse> response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 9;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, OBJECT_URN_QUERY_PARAMS_0);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 3;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, OBJECT_URN_QUERY_PARAMS_1);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 1;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, OBJECT_URN_QUERY_PARAMS_11);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, OBJECT_URN_QUERY_PARAMS_99);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, BJECT_URN_QUERY_PARAMS);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 3;
        queryParams.put(QueryParameterType.OBJECT_URN_LIKE, OBJECT_URN_QUERY_PARAMS);
        queryParams.put(QueryParameterType.TYPE, "type one");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);
    }

    @Test
    public void findByQueryParameters_Type() throws Exception {
        populateQueryData();

        Map<QueryParameterType, Object> queryParams = new HashMap<>();
        int expectedSize = 0;
        int actualSize = 0;

        queryParams.put(QueryParameterType.TYPE, "type one");
        List<ObjectResponse> response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 3);

        // The next two tests should verify that type does exact matching, unlike objectUrn, name, and moniker, and
        // in a perfect world the first of the two tests would also return 0.
        // Unfortunately, the exact() matcher is broken in Spring. If they ever fix it, uncomment the line containing
        // "exact()" in ObjectPersistenceService.java
        // Only one field-specific matcher per field, unfortunately, so a combination of startsWith() and EndsWith()
        // doesn't work either.
        expectedSize = 3;
        queryParams.put(QueryParameterType.TYPE, "type o");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(QueryParameterType.TYPE, "ype one");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(QueryParameterType.TYPE, "type z");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    @Test
    public void findByQueryParameters_MonikerLike() throws Exception {
        populateQueryData();

        Map<QueryParameterType, Object> queryParams = new HashMap<>();
        int expectedSize = 0;
        int actualSize = 0;

        expectedSize = 3;
        queryParams.remove(QueryParameterType.TYPE);
        queryParams.put(MONIKER_LIKE, "moniker");
        List<ObjectResponse> response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 1;
        queryParams.put(QueryParameterType.MONIKER_LIKE, "moniker o");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 1;
        queryParams.put(QueryParameterType.MONIKER_LIKE, "moniker three");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(QueryParameterType.MONIKER_LIKE, "moniker z");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(QueryParameterType.MONIKER_LIKE, "oniker");
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);
    }

    @Test
    public void findByQueryParameters_LastModified() throws Exception {

        final UUID accountUuid = UuidUtil.getNewUuid();
        final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountUuid);

        Map<QueryParameterType, Object> queryParams = new HashMap<>();
        int expectedSize = 0;
        int actualSize = 0;

        Long firstDate = new Date().getTime();
        Thread.sleep(DELAY_BETWEEN_LAST_MODIFIED_DATES);

        ObjectEntity firstObject = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnLastModTest1").name("last mod test 1").type("anythingIsFine").build();
        this.objectRepository.save(firstObject);

        Long secondDate = new Date().getTime();
        Thread.sleep(DELAY_BETWEEN_LAST_MODIFIED_DATES);

        ObjectEntity secondObject = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnLastModTest2").name("last mod test 2").type("anythingIsFine").build();
        this.objectRepository.save(secondObject);

        Long thirdDate = new Date().getTime();
        Thread.sleep(DELAY_BETWEEN_LAST_MODIFIED_DATES);

        ObjectEntity thirdObject = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnLastModTest3").name("last mod test 3").type("anythingIsFine").build();
        this.objectRepository.save(thirdObject);

        Long fourthDate = new Date().getTime();

        expectedSize = 3;
        queryParams.put(MODIFIED_AFTER, firstDate);
        List<ObjectResponse> response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 2;
        queryParams.put(MODIFIED_AFTER, secondDate);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 1;
        queryParams.put(MODIFIED_AFTER, thirdDate);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

        expectedSize = 0;
        queryParams.put(MODIFIED_AFTER, fourthDate);
        response = objectPersistenceService.findByQueryParameters(accountUrn, queryParams);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);
    }

    // used by findByQueryParametersStringParameters()
    private void populateQueryData() throws Exception {

        final UUID accountUuid = UuidUtil.getNewUuid();

        ObjectEntity entityNameOneTypeOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_01).name(NAME_ONE).type(TYPE_ONE).build();

        ObjectEntity entityNameTwoTypeOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_02).name(NAME_TWO).type(TYPE_ONE).build();

        ObjectEntity entityNameThreeTypeOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_03).name(NAME_THREE).type(TYPE_ONE).build();

        ObjectEntity entityNameOneTypeTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_04).name(NAME_ONE).type(TYPE_TWO).build();

        ObjectEntity entityNameTwoTypeTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_05).name(NAME_TWO).type(TYPE_TWO).build();

        ObjectEntity entityNameThreeTypeTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_06).name(NAME_THREE).type(TYPE_TWO).build();

        ObjectEntity entityNameOneMonikerOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_07).name(NAME_ONE).type(WHATEVER).moniker(MONIKER_ONE).build();

        ObjectEntity entityNameOneMonikerTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_08).name(NAME_ONE).type(WHATEVER).moniker(MONIKER_TWO).build();

        ObjectEntity entityNameOneMonikerThree = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_09).name(NAME_ONE).type(WHATEVER).moniker(MONIKER_THREE).build();

        ObjectEntity entityObjectUrn10 = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_10).name(WHATEVER).type(WHATEVER).build();

        ObjectEntity entityObjectUrn11 = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_11).name(WHATEVER).type(WHATEVER).build();

        ObjectEntity entityObjectUrn12 = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn(OBJECT_URN_QUERY_PARAMS_12).name(WHATEVER).type(WHATEVER).build();

        objectRepository.save(entityNameOneTypeOne);
        objectRepository.save(entityNameTwoTypeOne);
        objectRepository.save(entityNameThreeTypeOne);
        objectRepository.save(entityNameOneTypeTwo);
        objectRepository.save(entityNameTwoTypeTwo);
        objectRepository.save(entityNameThreeTypeTwo);
        objectRepository.save(entityNameOneMonikerOne);
        objectRepository.save(entityNameOneMonikerTwo);
        objectRepository.save(entityNameOneMonikerThree);
        objectRepository.save(entityObjectUrn10);
        objectRepository.save(entityObjectUrn11);
        objectRepository.save(entityObjectUrn12);
    }

}
