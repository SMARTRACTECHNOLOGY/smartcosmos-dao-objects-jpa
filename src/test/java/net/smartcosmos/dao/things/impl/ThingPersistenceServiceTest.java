package net.smartcosmos.dao.things.impl;

import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.things.ThingsPersistenceTestApplication;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
import net.smartcosmos.security.user.SmartCosmosUser;
import org.junit.After;
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

import java.util.*;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ThingsPersistenceTestApplication.class,
                                            ThingPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class ThingPersistenceServiceTest {

    public static final String OBJECT_URN_QUERY_PARAMS_01 = "urnQueryParams01";
    public static final String OBJECT_URN_QUERY_PARAMS_02 = "urnQueryParams02";
    public static final String OBJECT_URN_QUERY_PARAMS_03 = "urnQueryParams03";
    public static final String OBJECT_URN_QUERY_PARAMS_04 = "urnQueryParams04";
    public static final String OBJECT_URN_QUERY_PARAMS_05 = "urnQueryParams05";
    public static final String OBJECT_URN_QUERY_PARAMS_06 = "urnQueryParams06";
    public static final String OBJECT_URN_QUERY_PARAMS_07 = "urnQueryParams07";
    public static final String OBJECT_URN_QUERY_PARAMS_08 = "urnQueryParams08";
    public static final String OBJECT_URN_QUERY_PARAMS_09 = "urnQueryParams09";
    public static final String OBJECT_URN_QUERY_PARAMS_10 = "urnQueryParams10";
    public static final String OBJECT_URN_QUERY_PARAMS_11 = "urnQueryParams11";
    public static final String OBJECT_URN_QUERY_PARAMS_12 = "urnQueryParams12";
    public static final String TYPE_ONE = "type one";
    public static final String TYPE_TWO = "type two";
    public static final String WHATEVER = "whatever";
    public static final String OBJECT_URN_QUERY_PARAMS = "urnQueryParams";
    private final UUID tenantUuid = UUID.randomUUID();

    private final String tenantId = tenantUuid.toString();

    @Autowired
    ThingPersistenceService persistenceService;

    @Autowired
    ThingRepository repository;

    @Before
    public void setUp() throws Exception {

        // Need to mock out user for conversion service.
        // Might be a good candidate for a test package util.
        Authentication authentication = Mockito.mock(Authentication.class);
        Mockito.when(authentication.getPrincipal())
            .thenReturn(new SmartCosmosUser(tenantId, "urn:userUrn", "username",
                                            "password", Arrays.asList(new SimpleGrantedAuthority("USER"))));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @After
    public void tearDown() throws Exception {
        repository.deleteAll();
    }

    @Test
    public void create() throws Exception {
        ThingCreate create = ThingCreate.builder()
            .urn("urn:fakeUrn")
            .type("type")
            .build();
        ThingResponse response = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndTypeAndUrn(tenantUuid, "type", "urn:fakeUrn");

        assertTrue(entity.isPresent());

        assertEquals("urn:fakeUrn", entity.get().getUrn());
        assertEquals("urn:fakeUrn", response.getUrn());
    }

    // region Update

    @Test
    public void thatUpdateByTypeAndUrnSucceeds() {

        String urn = "urn:fakeUrn-update";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertTrue(entity.get().getActive());

        assertEquals(urn, responseCreate.getUrn());
        assertTrue(responseCreate.getActive());

        ThingUpdate update = ThingUpdate.builder()
            .active(false)
            .build();

        Optional<ThingResponse> responseUpdate = persistenceService.updateByTypeAndUrn(tenantId, type, urn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(urn, responseUpdate.get().getUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(false, responseUpdate.get().getActive());
    }

    @Test
    public void thatUpdateByIdSucceeds() {

        String urn = "urn:fakeUrn-update2";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .active(false)
            .build();

        Optional<ThingResponse> responseUpdate = persistenceService.updateById(tenantId, responseCreate.getId(), update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(urn, responseUpdate.get().getUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(false, responseUpdate.get().getActive());
    }

    @Test
    public void thatUpdateNonexistentByTypeAndUrnFails() {
        ThingUpdate update = ThingUpdate.builder()
            .active(false)
            .build();

        Optional<ThingResponse> responseUpdate = persistenceService.updateByTypeAndUrn(tenantId, "NO SUCH TYPE", "URN:DOES-NOT-EXIST", update);

        assertFalse(responseUpdate.isPresent());
    }

    @Test
    public void thatUpdateNonexistentByIdFails() {

        String urn = "urn:fakeUrn-update3";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .active(false)
            .build();
        Optional<ThingResponse> responseUpdate = persistenceService.updateById(tenantId, UUID.randomUUID().toString(), update);

        assertFalse(responseUpdate.isPresent());
    }

    // endregion

    // region Delete

    @Test
    public void testDeleteById() throws Exception {

        String urn = "urn:fakeUrn-delete";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        assertTrue(entity.isPresent());

        String id = entity.get().getId().toString();

        List<ThingResponse> responseDelete = persistenceService.deleteById(tenantId, id);

        assertFalse(responseDelete.isEmpty());
        assertEquals(1, responseDelete.size());
        assertEquals(id, responseDelete.get(0).getId());
    }

    @Test
    public void testDeleteByTypeAndUrn() throws Exception {

        String urn = "urn:fakeUrn-delete2";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        assertTrue(entity.isPresent());

        String id = entity.get().getId().toString();

        List<ThingResponse> responseDelete = persistenceService.deleteByTypeAndUrn(tenantId, type, urn);

        assertFalse(responseDelete.isEmpty());
        assertEquals(1, responseDelete.size());
        assertEquals(id, responseDelete.get(0).getId());
    }

    // endregion

    // region Find By Object URN

    @Test
    public void testFindByTypeAndUrnStartsWithNonexistent() throws Exception {
        populateQueryData();

        List<ThingResponse> response = persistenceService.findByTypeAndUrnStartsWith(tenantId, "no-such-type", "no-such-urn", 0L, 100L);

        assertTrue(response.isEmpty());
    }

    @Test
    public void testFindByTypeAndUrnStartsWith() throws Exception {
        populateQueryData();

        List<ThingResponse> response = persistenceService.findByTypeAndUrnStartsWith(tenantId, WHATEVER, OBJECT_URN_QUERY_PARAMS, 0L, 100L);

        assertEquals(6, response.size());
    }

    // endregion

    @Test
    public void testFindByIds() throws Exception
    {
        populateQueryData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_01).get().getId();
        String secondId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_02).get().getId();
        String thirdId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_03).get().getId();

        Collection<String> ids = new ArrayList<>();
        ids.add(firstId);
        ids.add(secondId);
        ids.add(thirdId);

        expectedSize = 3;
        List<Optional<ThingResponse>> response = persistenceService.findByIds(tenantId, ids, 0L, 100L);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    @Test
    public void thatFindByUrnsReturnsPartialResultsWithNonexistentId() throws Exception
    {
        populateQueryData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_01).get().getId();
        String secondId = UUID.randomUUID().toString();
        String thirdId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_03).get().getId();

        Collection<String> ids = new ArrayList<>();
        ids.add(firstId);
        ids.add(secondId);
        ids.add(thirdId);

        expectedSize = 3;
        List<Optional<ThingResponse>> response = persistenceService.findByIds(tenantId, ids, 0L, 100L);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    @Test
    public void thatFindByUrnsReturnsPartialResultsWithUnparseableId() throws Exception
    {
        populateQueryData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_01).get().getId();
        String secondId = "no UUID";
        String thirdId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, OBJECT_URN_QUERY_PARAMS_03).get().getId();

        Collection<String> ids = new ArrayList<>();
        ids.add(firstId);
        ids.add(secondId);
        ids.add(thirdId);

        expectedSize = 3;
        List<Optional<ThingResponse>> response = persistenceService.findByIds(tenantId, ids, 0L, 100L);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    // endregion

    // region Helper Methods

    // used by findByQueryParametersStringParameters()
    private void populateQueryData() throws Exception {

        ThingEntity entityNameOneTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_01).type(TYPE_ONE).build();

        ThingEntity entityNameTwoTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_02).type(TYPE_ONE).build();

        ThingEntity entityNameThreeTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_03).type(TYPE_ONE).build();

        ThingEntity entityNameOneTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_04).type(TYPE_TWO).build();

        ThingEntity entityNameTwoTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_05).type(TYPE_TWO).build();

        ThingEntity entityNameThreeTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_06).type(TYPE_TWO).build();

        ThingEntity entityNameOneMonikerOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_07).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_08).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerThree = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_09).type(WHATEVER).build();

        ThingEntity entityObjectUrn10 = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_10).type(WHATEVER).build();

        ThingEntity entityObjectUrn11 = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_11).type(WHATEVER).build();

        ThingEntity entityObjectUrn12 = ThingEntity.builder().tenantId(tenantUuid)
            .urn(OBJECT_URN_QUERY_PARAMS_12).type(WHATEVER).build();

        repository.save(entityNameOneTypeOne);
        repository.save(entityNameTwoTypeOne);
        repository.save(entityNameThreeTypeOne);
        repository.save(entityNameOneTypeTwo);
        repository.save(entityNameTwoTypeTwo);
        repository.save(entityNameThreeTypeTwo);
        repository.save(entityNameOneMonikerOne);
        repository.save(entityNameOneMonikerTwo);
        repository.save(entityNameOneMonikerThree);
        repository.save(entityObjectUrn10);
        repository.save(entityObjectUrn11);
        repository.save(entityObjectUrn12);
    }

    // endregion

}
