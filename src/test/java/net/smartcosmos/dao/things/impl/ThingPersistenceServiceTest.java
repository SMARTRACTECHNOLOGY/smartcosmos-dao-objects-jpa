package net.smartcosmos.dao.things.impl;

import net.smartcosmos.dao.things.ThingsPersistenceTestApplication;
import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.util.UuidUtil;
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

    public static final int DELAY_BETWEEN_LAST_MODIFIED_DATES = 10;
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
    public static final String NAME_ONE = "name one";
    public static final String TYPE_ONE = "type one";
    public static final String NAME_TWO = "name two";
    public static final String NAME_THREE = "name three";
    public static final String TYPE_TWO = "type two";
    public static final String WHATEVER = "whatever";
    public static final String MONIKER_ONE = "moniker one";
    public static final String MONIKER_TWO = "moniker two";
    public static final String MONIKER_THREE = "moniker three";
    public static final String OBJECT_URN_QUERY_PARAMS = "urnQueryParams";
    public static final String OBJECT_URN_QUERY_PARAMS_0 = "urnQueryParams0";
    public static final String OBJECT_URN_QUERY_PARAMS_1 = "urnQueryParams1";
    public static final String OBJECT_URN_QUERY_PARAMS_99 = "urnQueryParams99";
    public static final String BJECT_URN_QUERY_PARAMS = "bjectUrnQueryParams";
    private final UUID tenantId = UUID.randomUUID();

    private final String tenantUrn = UuidUtil.getTenantUrnFromUuid(tenantId);

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
            .thenReturn(new SmartCosmosUser(tenantUrn, "urn:userUrn", "username",
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
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, "urn:fakeUrn");

        assertTrue(entity.isPresent());

        assertEquals("urn:fakeUrn", entity.get().getUrn());
        assertEquals("urn:fakeUrn", response.getUrn());
    }

    // region Update

    @Test
    public void thatUpdateByObjectUrnSucceeds() {

        String urn = "urn:fakeUrn-update";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .urn(urn)
            .active(false)
            .build();
        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(urn, responseUpdate.get().getUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(false, responseUpdate.get().getActive());
    }

    @Test
    public void thatUpdateByUrnSucceeds() {

        String urn = "urn:fakeUrn-update2";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .urn(responseCreate.getUrn())
            .active(false)
            .build();
        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, update);

        assertTrue(responseUpdate.isPresent());

        assertEquals(urn, responseUpdate.get().getUrn());
        assertEquals(responseCreate.getUrn(), responseUpdate.get().getUrn());
        assertEquals(type, responseUpdate.get().getType());
        assertEquals(false, responseUpdate.get().getActive());
    }

    @Test
    public void thatUpdateNonexistentFails() {
        ThingUpdate update = ThingUpdate.builder()
            .urn("urn:DOES-NOT-EXIST")
            .build();
        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, update);

        assertFalse(responseUpdate.isPresent());
    }

    @Test(expected=IllegalArgumentException.class)
    public void thatUpdateWithoutIdThrowsException() {

        String urn = "urn:fakeUrn-update3";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .active(false)
            .build();
        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, update);
    }

    @Test(expected=IllegalArgumentException.class)
    public void thatUpdateByOverspecifiedIdThrowsException() {

        String urn = "urn:fakeUrn-update4";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .urn(responseCreate.getUrn())
            .id(urn)
            .active(false)
            .build();
        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, update);
    }

    @Test(expected=IllegalArgumentException.class)
    public void thatUpdateByOverspecifiedAndConflictingIdThrowsException() {

        String urn = "urn:fakeUrn-update5";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .build();

        ThingResponse responseCreate = persistenceService
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        assertEquals(urn, entity.get().getUrn());
        assertEquals(urn, responseCreate.getUrn());

        ThingUpdate update = ThingUpdate.builder()
            .id(responseCreate.getUrn())
            .urn("urn:fakeUrn-update-different")
            .active(false)
            .build();

        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, update);
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
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        String id = entity.get().getId().toString();

        List<ThingResponse> responseDelete = persistenceService.deleteById(tenantUrn, id);

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
            .create(tenantUrn, create);

        Optional<ThingEntity> entity = repository
            .findByTenantIdAndUrn(tenantId, urn);

        assertTrue(entity.isPresent());

        String id = entity.get().getId().toString();

        List<ThingResponse> responseDelete = persistenceService.deleteByTypeAndUrn(tenantUrn, type, urn);

        assertFalse(responseDelete.isEmpty());
        assertEquals(1, responseDelete.size());
        assertEquals(id, responseDelete.get(0).getId());
    }

    // endregion

    // region Find By Object URN

    @Test
    public void testFindByObjectUrn() throws Exception {

        final UUID accountUuid = UUID.randomUUID();
        final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountUuid);

        ThingEntity entity = ThingEntity.builder().tenantId(accountUuid)
            .urn("urn").type("some type").build();

        this.repository.save(entity);

        Optional<ThingResponse> response = persistenceService
            .findByObjectUrn(accountUrn, "urn");

        assertTrue(response.isPresent());
    }

    @Test
    public void testFindByObjectUrnStartsWithNonexistent() throws Exception {
        populateQueryData();

        List<ThingResponse> response = persistenceService.findByObjectUrnStartsWith(tenantUrn, "no-such-urn");

        assertTrue(response.isEmpty());
    }

    @Test
    public void testFindByObjectUrnStartsWith() throws Exception {
        populateQueryData();

        List<ThingResponse> response = persistenceService.findByObjectUrnStartsWith(tenantUrn, OBJECT_URN_QUERY_PARAMS);

        assertEquals(12, response.size());
    }

    // endregion

    @Test
    public void testFindByUrns() throws Exception
    {
        populateQueryData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_01).get().getUrn();
        String secondUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_02).get().getUrn();
        String thirdUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_03).get().getUrn();

        Collection<String> urns = new ArrayList<>();
        urns.add(firstUrn);
        urns.add(secondUrn);
        urns.add(thirdUrn);

        expectedSize = 3;
        List<Optional<ThingResponse>> response = persistenceService.findByUrns(tenantUrn, urns);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    @Test
    public void thatFindByUrnsReturnsPartialResultsWithNonexistentUrn() throws Exception
    {
        populateQueryData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_01).get().getUrn();
        String secondUrn = UuidUtil.getUrnFromUuid(UuidUtil.getNewUuid());
        String thirdUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_03).get().getUrn();

        Collection<String> urns = new ArrayList<>();
        urns.add(firstUrn);
        urns.add(secondUrn);
        urns.add(thirdUrn);

        expectedSize = 3;
        List<Optional<ThingResponse>> response = persistenceService.findByUrns(tenantUrn, urns);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    @Test
    public void thatFindByUrnsReturnsPartialResultsWithUnparseableUrn() throws Exception
    {
        populateQueryData();

        int expectedSize = 0;
        int actualSize = 0;

        String firstUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_01).get().getUrn();
        String secondUrn = "cannot be parsed as URN";
        String thirdUrn = persistenceService.findByObjectUrn(tenantUrn, OBJECT_URN_QUERY_PARAMS_03).get().getUrn();

        Collection<String> urns = new ArrayList<>();
        urns.add(firstUrn);
        urns.add(secondUrn);
        urns.add(thirdUrn);

        expectedSize = 3;
        List<Optional<ThingResponse>> response = persistenceService.findByUrns(tenantUrn, urns);
        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);

    }

    // endregion

    // region Helper Methods

    // used by findByQueryParametersStringParameters()
    private void populateQueryData() throws Exception {

        ThingEntity entityNameOneTypeOne = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_01).type(TYPE_ONE).build();

        ThingEntity entityNameTwoTypeOne = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_02).type(TYPE_ONE).build();

        ThingEntity entityNameThreeTypeOne = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_03).type(TYPE_ONE).build();

        ThingEntity entityNameOneTypeTwo = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_04).type(TYPE_TWO).build();

        ThingEntity entityNameTwoTypeTwo = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_05).type(TYPE_TWO).build();

        ThingEntity entityNameThreeTypeTwo = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_06).type(TYPE_TWO).build();

        ThingEntity entityNameOneMonikerOne = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_07).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerTwo = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_08).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerThree = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_09).type(WHATEVER).build();

        ThingEntity entityObjectUrn10 = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_10).type(WHATEVER).build();

        ThingEntity entityObjectUrn11 = ThingEntity.builder().tenantId(tenantId)
            .urn(OBJECT_URN_QUERY_PARAMS_11).type(WHATEVER).build();

        ThingEntity entityObjectUrn12 = ThingEntity.builder().tenantId(tenantId)
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
