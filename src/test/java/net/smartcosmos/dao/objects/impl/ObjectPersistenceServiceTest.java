package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.ObjectPersistenceConfig;
import net.smartcosmos.dao.objects.ObjectPersistenceTestApplication;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public void findByQueryParametersStringParameters() throws Exception
    {
        populateQueryData();

        Map<String, Object> queryParams = new HashMap<>();

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "objectUrnQueryParams");

        List<ObjectResponse> response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 12);

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "objectUrnQueryParams0");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 9);

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "objectUrnQueryParams1");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 3);

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "objectUrnQueryParams11");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 1);

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "objectUrnQueryParams99");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "bjectUrnQueryParams");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

        queryParams.put(ObjectPersistenceService.OBJECT_URN_LIKE, "objectUrnQueryParams");
        queryParams.put(ObjectPersistenceService.TYPE, "type");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 6);

        queryParams.put(ObjectPersistenceService.TYPE, "type o");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 3);

        queryParams.put(ObjectPersistenceService.TYPE, "type z");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

        queryParams.put(ObjectPersistenceService.TYPE, "ype");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

        queryParams.remove(ObjectPersistenceService.TYPE);
        queryParams.put(ObjectPersistenceService.MONIKER_LIKE, "moniker");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 3);

        queryParams.put(ObjectPersistenceService.MONIKER_LIKE, "moniker o");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 1);

        queryParams.put(ObjectPersistenceService.MONIKER_LIKE, "moniker three");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 1);

        queryParams.put(ObjectPersistenceService.MONIKER_LIKE, "moniker z");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

        queryParams.put(ObjectPersistenceService.MONIKER_LIKE, "oniker");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

    }

    @Test
    public void findByQueryParametersLastModified() throws Exception
    {
        final UUID accountUuid = UuidUtil.getNewUuid();
        final String accountUrn = UuidUtil.getAccountUrnFromUuid(accountUuid);
        Map<String, Object> queryParams = new HashMap<>();

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

        queryParams.put(ObjectPersistenceService.MODIFIED_AFTER, firstDate);
        List<ObjectResponse> response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 3);

        queryParams.put(ObjectPersistenceService.MODIFIED_AFTER, secondDate);
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 2);

        queryParams.put(ObjectPersistenceService.MODIFIED_AFTER, thirdDate);
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 1);

        queryParams.put(ObjectPersistenceService.MODIFIED_AFTER, fourthDate);
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 0);

    }

    // used by findByQueryParametersStringParameters()
    private void populateQueryData() throws Exception
    {

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

        this.objectRepository.save(entityNameOneTypeOne);
        this.objectRepository.save(entityNameTwoTypeOne);
        this.objectRepository.save(entityNameThreeTypeOne);
        this.objectRepository.save(entityNameOneTypeTwo);
        this.objectRepository.save(entityNameTwoTypeTwo);
        this.objectRepository.save(entityNameThreeTypeTwo);
        this.objectRepository.save(entityNameOneMonikerOne);
        this.objectRepository.save(entityNameOneMonikerTwo);
        this.objectRepository.save(entityNameOneMonikerThree);
        this.objectRepository.save(entityObjectUrn10);
        this.objectRepository.save(entityObjectUrn11);
        this.objectRepository.save(entityObjectUrn12);
    }

}
