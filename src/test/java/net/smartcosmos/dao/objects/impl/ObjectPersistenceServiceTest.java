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

    public static final int DELAY_BETWEEN_LAST_MODIFIED_DATES = 2000;
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
        findByQueryParametersUtil();

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

        queryParams.put(ObjectPersistenceService.TYPE, "type t");
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

        queryParams.put(ObjectPersistenceService.MONIKER_LIKE, "moniker t");
        response = objectPersistenceService
            .findByQueryParameters(accountUrn, queryParams);
        assertTrue(response.size() == 2);

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
    private void findByQueryParametersUtil() throws Exception
    {

        final UUID accountUuid = UuidUtil.getNewUuid();

        ObjectEntity entityNameOneTypeOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams01").name("name one").type("type one").build();

        ObjectEntity entityNameTwoTypeOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams02").name("name two").type("type one").build();

        ObjectEntity entityNameThreeTypeOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams03").name("name three").type("type one").build();

        ObjectEntity entityNameOneTypeTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams04").name("name one").type("type two").build();

        ObjectEntity entityNameTwoTypeTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams05").name("name two").type("type two").build();

        ObjectEntity entityNameThreeTypeTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams06").name("name three").type("type two").build();

        ObjectEntity entityNameOneMonikerOne = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams07").name("name one").type("whatever").moniker("moniker one").build();

        ObjectEntity entityNameOneMonikerTwo = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams08").name("name one").type("whatever").moniker("moniker two").build();

        ObjectEntity entityNameOneMonikerThree = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams09").name("name one").type("whatever").moniker("moniker three").build();

        ObjectEntity entityObjectUrn10 = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams10").name("whatever").type("whatever").build();

        ObjectEntity entityObjectUrn11 = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams11").name("whatever").type("whatever").build();

        ObjectEntity entityObjectUrn12 = ObjectEntity.builder().accountId(accountUuid)
            .objectUrn("objectUrnQueryParams12").name("whatever").type("whatever").build();

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
