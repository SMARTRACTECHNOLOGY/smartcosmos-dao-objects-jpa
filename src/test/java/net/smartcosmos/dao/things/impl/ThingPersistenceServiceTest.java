package net.smartcosmos.dao.things.impl;

import net.smartcosmos.dao.things.SortOrder;
import net.smartcosmos.dao.things.ThingPersistenceConfig;
import net.smartcosmos.dao.things.ThingsPersistenceTestApplication;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
import net.smartcosmos.security.user.SmartCosmosUser;
import org.junit.After;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = { ThingsPersistenceTestApplication.class,
                                            ThingPersistenceConfig.class })
@ActiveProfiles("test")
@WebAppConfiguration
@IntegrationTest({ "spring.cloud.config.enabled=false", "eureka.client.enabled:false" })
public class ThingPersistenceServiceTest {

    public static final String URN_01 = "urn:thing:uuid:8614fac9-693d-4bee-886f-f9eefd60180a";
    public static final String URN_02 = "urn:thing:uuid:73f81ca4-0800-4769-bb6f-db4a61b0fea1";
    public static final String URN_03 = "urn:thing:uuid:2650c7d5-9dc5-4455-9eda-e34066050f76";
    public static final String URN_04 = "urn:thing:uuid:10857865-4230-4d27-9dcc-9ef17ace3921";
    public static final String URN_05 = "urn:thing:uuid:28124b54-edc4-4eed-b307-2af056119db2";
    public static final String URN_06 = "urn:thing:uuid:fe55664a-0896-42bb-86c6-ba0c668eb348";
    public static final String URN_07 = "urn:thing:uuid:9f1e8d9e-8d8e-4d04-b776-bbfcdf760405";
    public static final String URN_08 = "urn:thing:uuid:71a03bc2-d41a-4c08-a355-6d0d5dc6f8a4";
    public static final String URN_09 = "urn:thing:uuid:17f890b8-caa6-4ad2-98d6-641f3999128e";
    public static final String URN_10 = "urn:thing:uuid:5926a69f-ff56-4a9c-ab79-e90b829f2ec4";
    public static final String URN_11 = "urn:thing:uuid:97005fff-da53-4ad2-8a40-128da31e9cd4";
    public static final String URN_12 = "urn:thing:uuid:4d872a55-ea69-4b9f-935a-9d21303085a5";
    public static final String TYPE_ONE = "type one";
    public static final String TYPE_TWO = "type two";
    public static final String WHATEVER = "whatever";
    private final UUID tenantUuid = UUID.randomUUID();

    private final String tenantUrn = UuidUtil.getTenantUrnFromUuid(tenantUuid);

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

    // region Create

    @Test
    public void thatCreateByTypeAndUrnUsesGivenId() throws Exception {

        final String uuid = "238978cb-c279-47e6-a553-92f2c372ae1d";
        final String urn = "urn:thing:uuid:" + uuid;

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type("type")
            .build();
        Optional<ThingResponse> response = persistenceService
            .create(tenantUrn, create);
        assertTrue(response.isPresent());

        Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(UuidUtil.getUuidFromUrn(urn), tenantUuid, "type");

        assertTrue(entity.isPresent());
        assertEquals(UUID.fromString(uuid), entity.get().getId());

        assertEquals(urn, response.get().getUrn());
    }

    @Test
    public void thatCreateByTypeGeneratesId() throws Exception {
        ThingCreate create = ThingCreate.builder()
            .type("type")
            .build();
        Optional<ThingResponse> response = persistenceService
            .create(tenantUrn, create);
        assertTrue(response.isPresent());

        Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(UuidUtil.getUuidFromUrn(response.get().getUrn()), tenantUuid, "type");

        assertTrue(entity.isPresent());

        assertEquals("type", entity.get().getType());
        assertEquals("type", response.get().getType());
        assertTrue(response.get().getUrn().startsWith("urn:thing:uuid:"));

        try {
            UuidUtil.getUuidFromUrn(response.get().getUrn());
        } catch (IllegalArgumentException e)
        {
            Assert.fail();
        }
    }

    @Test
    public void thatDuplicateIdFails() {

        final String uuid = "238978cb-c279-47e6-a553-92f2c372ae1d";
        final String urn = "urn:thing:uuid:" + uuid;

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type("type")
            .build();
        Optional<ThingResponse> response1 = persistenceService
            .create(tenantUrn, create);
        assertTrue(response1.isPresent());

        Optional<ThingResponse> response2 = persistenceService
            .create(tenantUrn, create);
        assertFalse(response2.isPresent());
    }

    // endregion

    // region Update

    @Test
    public void thatUpdateByTypeAndUrnSucceeds() {

        String urn = "urn:thing:uuid:a71bd56a-841a-4a34-bd9d-1fd3bfcd8f15";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        Optional<ThingResponse> response = persistenceService
            .create(tenantUrn, create);
        assertTrue(response.isPresent());
        ThingResponse responseCreate = response.get();

        Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(UuidUtil.getUuidFromUrn(urn), tenantUuid, type);

        assertTrue(entity.isPresent());

        assertTrue(entity.get().getActive());

        assertEquals(urn, responseCreate.getUrn());
        assertTrue(responseCreate.getActive());

        ThingUpdate update = ThingUpdate.builder()
            .active(false)
            .build();

        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, type, urn, update);

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

        Optional<ThingResponse> responseUpdate = persistenceService.update(tenantUrn, "NO SUCH TYPE", "URN:DOES-NOT-EXIST", update);

        assertFalse(responseUpdate.isPresent());
    }

    // endregion

    // region Delete

    @Test
    public void testDeleteByTypeAndUrn() throws Exception {

        String urn = "urn:thing:uuid:a8996049-18cb-40b0-9ffd-592c6b610d58";
        String type = "type";

        ThingCreate create = ThingCreate.builder()
            .urn(urn)
            .type(type)
            .build();

        Optional<ThingResponse> response = persistenceService
            .create(tenantUrn, create);
        assertTrue(response.isPresent());

        Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(UuidUtil.getUuidFromUrn(urn), tenantUuid, type);

        assertTrue(entity.isPresent());

        UUID id = entity.get().getId();

        List<ThingResponse> responseDelete = persistenceService.delete(tenantUrn, type, urn);

        assertFalse(responseDelete.isEmpty());
        assertEquals(1, responseDelete.size());
        assertEquals(id, UuidUtil.getUuidFromUrn(responseDelete.get(0).getUrn()));
    }

    // endregion

    // region Find By Type and URN

    @Test
    public void testFindByTypeAndUrn() throws Exception {
        populateData();

        Optional<ThingResponse> response = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01);

        assertTrue(response.isPresent());
        assertEquals(URN_01, response.get().getUrn());
        assertEquals(TYPE_ONE, response.get().getType());
    }

    @Test
    public void testFindByTypeAndUrnIsCaseSensitiveUrn() throws Exception {
        populateData();

        Optional<ThingResponse> response = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01.toUpperCase());

        assertTrue(response.isPresent());
    }

    @Test
    public void testFindByTypeAndUrnNonExistent() throws Exception {
        populateData();

        Optional<ThingResponse> response = persistenceService.findByTypeAndUrn(tenantUrn, WHATEVER, URN_01);
        assertFalse(response.isPresent());
    }

    // endregion

    // region Find by Type

    @Test
    public void testFindByType() throws Exception {

        populateData();

        int expectedSize = 3;
        int actualSize = 0;

        List<ThingResponse> response = persistenceService.findByType(tenantUrn, TYPE_ONE);

        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);
    }

    @Test
    public void testFindByTypeIsCaseInSensitive() throws Exception {

        populateData();

        int expectedSize = 0;
        int actualSize = 0;

        List<ThingResponse> response = persistenceService.findByType(tenantUrn, TYPE_ONE.toUpperCase());

        assertFalse(response.isEmpty());
    }

    @Test
    public void testFindByTypePaging() throws Exception {

        populateData();

        int expectedPageSize = 3;
        int actualPageSize = 0;

        long expectedTotalSize = 6;
        long actualTotalSize = 0;

        Page<ThingResponse> response = persistenceService.findByType(tenantUrn, WHATEVER, 1, 3);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertNotNull(response.getPage());

        actualPageSize = response.getData().size();
        assertTrue("Expected " + expectedPageSize + " elements on page, but received " + actualPageSize, actualPageSize == expectedPageSize);

        actualTotalSize = response.getPage().getTotalElements();
        assertTrue("Expected " + expectedTotalSize + " total elements, but received " + actualTotalSize, actualTotalSize == expectedTotalSize);
    }

    // endregion

    // region Find by URNs

    @Test
    public void testFindByUrns() throws Exception
    {
        populateData();

        int expectedDataSize = 3;
        int actualDataSize = 0;

        String firstUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01).get().getUrn();
        String secondUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_02).get().getUrn();
        String thirdUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_03).get().getUrn();

        Collection<String> urn = new ArrayList<>();
        urn.add(firstUrn);
        urn.add(secondUrn);
        urn.add(thirdUrn);

        List<ThingResponse> response = persistenceService.findByUrns(tenantUrn, urn);

        actualDataSize = response.size();
        assertTrue("Expected " + expectedDataSize + " matches, but received " + actualDataSize, actualDataSize == expectedDataSize);
    }

    @Test
    public void testFindByUrnsSortingByType() throws Exception
    {
        populateData();

        int expectedDataSize = 3;
        int actualDataSize = 0;

        String firstUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01).get().getUrn();
        String secondUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_TWO, URN_04).get().getUrn();
        String thirdUrn = persistenceService.findByTypeAndUrn(tenantUrn, WHATEVER, URN_12).get().getUrn();

        Collection<String> urn = new ArrayList<>();
        urn.add(secondUrn);
        urn.add(firstUrn);
        urn.add(thirdUrn);

        List<ThingResponse> response = persistenceService.findByUrns(tenantUrn, urn, SortOrder.ASC, "type");

        actualDataSize = response.size();
        assertTrue("Expected " + expectedDataSize + " matches, but received " + actualDataSize, actualDataSize == expectedDataSize);

        assertEquals(firstUrn, response.get(0).getUrn());
        assertEquals(secondUrn, response.get(1).getUrn());
        assertEquals(thirdUrn, response.get(2).getUrn());
    }

    @Test
    public void testFindByUrnsSortingEmptySortByDefaultsToId() throws Exception
    {
        populateData();

        int expectedDataSize = 3;
        int actualDataSize = 0;

        String firstUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01).get().getUrn();
        String secondUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_TWO, URN_04).get().getUrn();
        String thirdUrn = persistenceService.findByTypeAndUrn(tenantUrn, WHATEVER, URN_12).get().getUrn();

        List<String> urn = new ArrayList<>();
        urn.add(secondUrn);
        urn.add(firstUrn);
        urn.add(thirdUrn);

        List<String> sortedUrns = urn.stream()
            .sorted()
            .collect(Collectors.toList());

        List<ThingResponse> response = persistenceService.findByUrns(tenantUrn, urn, SortOrder.ASC, "");

        actualDataSize = response.size();
        assertTrue("Expected " + expectedDataSize + " matches, but received " + actualDataSize, actualDataSize == expectedDataSize);

        assertEquals(sortedUrns.get(0), response.get(0).getUrn());
        assertEquals(sortedUrns.get(1), response.get(1).getUrn());
        assertEquals(sortedUrns.get(2), response.get(2).getUrn());
    }

    @Test
    public void testFindByUrnsSortingNullSortByDefaultsToId() throws Exception
    {
        populateData();

        int expectedDataSize = 3;
        int actualDataSize = 0;

        String firstUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01).get().getUrn();
        String secondUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_TWO, URN_04).get().getUrn();
        String thirdUrn = persistenceService.findByTypeAndUrn(tenantUrn, WHATEVER, URN_12).get().getUrn();

        List<String> urn = new ArrayList<>();
        urn.add(secondUrn);
        urn.add(firstUrn);
        urn.add(thirdUrn);

        List<String> sortedUrns = urn.stream()
            .sorted()
            .collect(Collectors.toList());

        List<ThingResponse> response = persistenceService.findByUrns(tenantUrn, urn, SortOrder.ASC, null);

        actualDataSize = response.size();
        assertTrue("Expected " + expectedDataSize + " matches, but received " + actualDataSize, actualDataSize == expectedDataSize);

        assertEquals(sortedUrns.get(0), response.get(0).getUrn());
        assertEquals(sortedUrns.get(1), response.get(1).getUrn());
        assertEquals(sortedUrns.get(2), response.get(2).getUrn());
    }

    @Test
    public void thatFindByUrnsReturnsPartialResultsWithNonexistentId() throws Exception
    {
        populateData();

        int expectedDataSize = 2;
        int actualDataSize = 0;

        String firstUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01).get().getUrn();
        String secondUrn = UuidUtil.getThingUrnFromUuid(UUID.randomUUID());
        String thirdUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_03).get().getUrn();

        Collection<String> urns = new ArrayList<>();
        urns.add(firstUrn);
        urns.add(secondUrn);
        urns.add(thirdUrn);

        List<ThingResponse> response = persistenceService.findByUrns(tenantUrn, urns);

        actualDataSize = response.size();
        assertTrue("Expected " + expectedDataSize + " matches, but received " + actualDataSize, actualDataSize == expectedDataSize);
    }

    @Test
    public void thatFindByUrnsReturnsPartialResultsWithUnparseableId() throws Exception
    {
        populateData();

        int expectedDataSize = 2;
        int actualDataSize = 0;

        String firstUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_01).get().getUrn();
        String seconUrn = "no URN";
        String thirdUrn = persistenceService.findByTypeAndUrn(tenantUrn, TYPE_ONE, URN_03).get().getUrn();

        Collection<String> urns = new ArrayList<>();
        urns.add(firstUrn);
        urns.add(seconUrn);
        urns.add(thirdUrn);

        List<ThingResponse> response = persistenceService.findByUrns(tenantUrn, urns);

        actualDataSize = response.size();
        assertTrue("Expected " + expectedDataSize + " matches, but received " + actualDataSize, actualDataSize == expectedDataSize);
    }

    // endregion

    // region Find All

    @Test
    public void testFindAll() throws Exception {

        populateData();

        int expectedSize = 12;
        int actualSize = 0;

        List<ThingResponse> response = persistenceService.findAll(tenantUrn);

        assertFalse(response.isEmpty());

        actualSize = response.size();
        assertTrue("Expected " + expectedSize + " but received " + actualSize, actualSize == expectedSize);
    }

    @Test
    public void testFindAllSortingAsc() throws Exception {

        populateData();

        List<ThingResponse> response = persistenceService.findAll(tenantUrn, SortOrder.ASC, "type");

        assertFalse(response.isEmpty());
        assertEquals(TYPE_ONE, response.get(0).getType());
        assertEquals(TYPE_ONE, response.get(1).getType());
        assertEquals(TYPE_ONE, response.get(2).getType());
        assertEquals(TYPE_TWO, response.get(3).getType());
        assertEquals(TYPE_TWO, response.get(4).getType());
        assertEquals(TYPE_TWO, response.get(5).getType());
        assertEquals(WHATEVER, response.get(6).getType());
        assertEquals(WHATEVER, response.get(7).getType());
        assertEquals(WHATEVER, response.get(8).getType());
        assertEquals(WHATEVER, response.get(9).getType());
        assertEquals(WHATEVER, response.get(10).getType());
        assertEquals(WHATEVER, response.get(11).getType());
    }

    @Test
    public void testFindAllSortingDesc() throws Exception {

        populateData();

        List<ThingResponse> response = persistenceService.findAll(tenantUrn, SortOrder.DESC, "type");

        assertFalse(response.isEmpty());
        assertEquals(WHATEVER, response.get(0).getType());
        assertEquals(WHATEVER, response.get(1).getType());
        assertEquals(WHATEVER, response.get(2).getType());
        assertEquals(WHATEVER, response.get(3).getType());
        assertEquals(WHATEVER, response.get(4).getType());
        assertEquals(WHATEVER, response.get(5).getType());
        assertEquals(TYPE_TWO, response.get(6).getType());
        assertEquals(TYPE_TWO, response.get(7).getType());
        assertEquals(TYPE_TWO, response.get(8).getType());
        assertEquals(TYPE_ONE, response.get(9).getType());
        assertEquals(TYPE_ONE, response.get(10).getType());
        assertEquals(TYPE_ONE, response.get(11).getType());
    }

    @Test
    public void testFindAllPaging() throws Exception {

        populateData();

        Page<ThingResponse> response = persistenceService.findAll(tenantUrn, 1, 3);

        assertFalse(response.getData().isEmpty());
        assertEquals(3, response.getData().size());

        assertEquals(3, response.getPage().getSize());
        assertEquals(4, response.getPage().getTotalPages());
        assertEquals(12, response.getPage().getTotalElements());
        assertEquals(1, response.getPage().getNumber());
    }

    @Test
    public void testFinallPagingAndSorting() throws Exception {

        populateData();

        Page<ThingResponse> page1 = persistenceService.findAll(tenantUrn, 1, 3, SortOrder.ASC, "type");

        assertFalse(page1.getData().isEmpty());
        assertEquals(3, page1.getData().size());

        assertEquals(3, page1.getPage().getSize());
        assertEquals(4, page1.getPage().getTotalPages());
        assertEquals(12, page1.getPage().getTotalElements());
        assertEquals(1, page1.getPage().getNumber());

        assertEquals(TYPE_ONE, page1.getData().get(0).getType());
        assertEquals(TYPE_ONE, page1.getData().get(1).getType());
        assertEquals(TYPE_ONE, page1.getData().get(2).getType());

        Page<ThingResponse> page2 = persistenceService.findAll(tenantUrn, 2, 3, SortOrder.ASC, "type");

        assertFalse(page2.getData().isEmpty());
        assertEquals(3, page2.getData().size());

        assertEquals(3, page2.getPage().getSize());
        assertEquals(4, page2.getPage().getTotalPages());
        assertEquals(12, page2.getPage().getTotalElements());
        assertEquals(2, page2.getPage().getNumber());

        assertEquals(TYPE_TWO, page2.getData().get(0).getType());
        assertEquals(TYPE_TWO, page2.getData().get(1).getType());
        assertEquals(TYPE_TWO, page2.getData().get(2).getType());
    }

    // endregion

    // region Helper Methods

    private void populateData() throws Exception {

        ThingEntity entityNameOneTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_01)).type(TYPE_ONE).build();

        ThingEntity entityNameTwoTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_02)).type(TYPE_ONE).build();

        ThingEntity entityNameThreeTypeOne = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_03)).type(TYPE_ONE).build();

        ThingEntity entityNameOneTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_04)).type(TYPE_TWO).build();

        ThingEntity entityNameTwoTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_05)).type(TYPE_TWO).build();

        ThingEntity entityNameThreeTypeTwo = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_06)).type(TYPE_TWO).build();

        ThingEntity entityNameOneMonikerOne = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_07)).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerTwo = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_08)).type(WHATEVER).build();

        ThingEntity entityNameOneMonikerThree = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_09)).type(WHATEVER).build();

        ThingEntity entityObjectUrn10 = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_10)).type(WHATEVER).build();

        ThingEntity entityObjectUrn11 = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_11)).type(WHATEVER).build();

        ThingEntity entityObjectUrn12 = ThingEntity.builder().tenantId(tenantUuid)
            .id(UuidUtil.getUuidFromUrn(URN_12)).type(WHATEVER).build();

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
