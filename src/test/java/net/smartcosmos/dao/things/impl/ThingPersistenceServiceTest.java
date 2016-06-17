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
import org.junit.Ignore;
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

    public static final String URN_01 = "urn:thing:dummy01";
    public static final String URN_02 = "urn:thing:dummy02";
    public static final String URN_03 = "urn:thing:dummy03";
    public static final String URN_04 = "urn:thing:dummy04";
    public static final String URN_05 = "urn:thing:dummy05";
    public static final String URN_06 = "urn:thing:dummy06";
    public static final String URN_07 = "urn:thing:dummy07";
    public static final String URN_08 = "urn:thing:dummy08";
    public static final String URN_09 = "urn:thing:dummy09";
    public static final String URN_10 = "urn:thing:dummy10";
    public static final String URN_11 = "urn:thing:dummy11";
    public static final String URN_12 = "urn:thing:dummy12";
    public static final String TYPE_ONE = "type one";
    public static final String TYPE_TWO = "type two";
    public static final String WHATEVER = "whatever";
    public static final String OBJECT_URN_QUERY_PARAMS = "urn:thing:dummy";
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

    // region Create

    @Test
    public void createByTypeAndUrn() throws Exception {
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

    // TODO: Generate URN from UUID if no URN is provided
    @Ignore
    @Test
    public void createByType() throws Exception {
        ThingCreate create = ThingCreate.builder()
            .type("type")
            .build();
        ThingResponse response = persistenceService
            .create(tenantId, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndId(tenantUuid, UUID.fromString(response.getId()));

        assertTrue(entity.isPresent());

        assertEquals("type", entity.get().getType());
        assertEquals("type", response.getType());
        assertTrue(entity.get().getUrn().startsWith("urn:"));
    }

    // endregion

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

        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantId, type, urn, update);

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

        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantId, "NO SUCH TYPE", "URN:DOES-NOT-EXIST", update);

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

        List<ThingResponse> responseDelete = persistenceService.delete(tenantId, type, urn);

        assertFalse(responseDelete.isEmpty());
        assertEquals(1, responseDelete.size());
        assertEquals(id, responseDelete.get(0).getId());
    }

    // endregion

    // region Find By Type and URN

    @Test
    public void testFindByTypeAndUrn() throws Exception {
        populateData();

        Optional<ThingResponse> response = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_01);

        assertTrue(response.isPresent());
        assertEquals(URN_01, response.get().getUrn());
        assertEquals(TYPE_ONE, response.get().getType());
    }

    @Test
    public void testFindByTypeAndUrnNonExistent() throws Exception {
        populateData();

        Optional<ThingResponse> response = persistenceService.findByTypeAndUrn(tenantId, WHATEVER, URN_01);
        assertFalse(response.isPresent());
    }

    @Test
    public void testFindByTypeAndUrnStartsWithNonexistent() throws Exception {
        populateData();

        List<ThingResponse> response = persistenceService.findByTypeAndUrnStartsWith(tenantId, "no-such-type", "no-such-urn", 0L, 100L);

        assertTrue(response.isEmpty());
    }

    @Test
    public void testFindByTypeAndUrnStartsWith() throws Exception {
        populateData();

        List<ThingResponse> response = persistenceService.findByTypeAndUrnStartsWith(tenantId, WHATEVER, OBJECT_URN_QUERY_PARAMS, 0L, 100L);

        assertEquals(6, response.size());
    }

    // endregion

    // region Find by ID

    @Test
    public void testFindById() {
        String urn = "urn:fakeUrn-find1";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantId, create);

        String id = responseCreate.getId().toString();

        Optional<ThingResponse> response = persistenceService.findById(tenantId, id);

        assertTrue(response.isPresent());
        assertEquals(type, response.get().getType());
        assertEquals(urn, response.get().getUrn());
        assertEquals(id, response.get().getId());
        assertEquals(tenantId, response.get().getTenantUrn());
    }

    @Test
    public void testFindByIdNonexistent() {

        Optional<ThingResponse> response = persistenceService.findById(tenantId, UUID.randomUUID().toString());
        assertFalse(response.isPresent());
    }

    // endregion

    // region Find by IDs

    @Test
    public void testFindByIds() throws Exception
    {
        populateData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_01).get().getId();
        String secondId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_02).get().getId();
        String thirdId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_03).get().getId();

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
    public void thatFindByIdsReturnsPartialResultsWithNonexistentId() throws Exception
    {
        populateData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_01).get().getId();
        String secondId = UUID.randomUUID().toString();
        String thirdId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_03).get().getId();

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
    public void thatFindByIdsReturnsPartialResultsWithUnparseableId() throws Exception
    {
        populateData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_01).get().getId();
        String secondId = "no UUID";
        String thirdId = persistenceService.findByTypeAndUrn(tenantId, TYPE_ONE, URN_03).get().getId();

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

    private void populateData() throws Exception {

        ThingEntity entityNameOneTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_01).type(TYPE_ONE).build();

        ThingEntity entityNameTwoTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_02).type(TYPE_ONE).build();

        ThingEntity entityNameThreeTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_03).type(TYPE_ONE).build();

        ThingEntity entityNameOneTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_04).type(TYPE_TWO).build();

        ThingEntity entityNameTwoTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_05).type(TYPE_TWO).build();

        ThingEntity entityNameThreeTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_06).type(TYPE_TWO).build();

        ThingEntity entityNameOneMonikerOne = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_07).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerTwo = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_08).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerThree = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_09).type(WHATEVER).build();

        ThingEntity entityObjectUrn10 = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_10).type(WHATEVER).build();

        ThingEntity entityObjectUrn11 = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_11).type(WHATEVER).build();

        ThingEntity entityObjectUrn12 = ThingEntity.builder().tenantId(tenantUuid)
            .urn(URN_12).type(WHATEVER).build();

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
