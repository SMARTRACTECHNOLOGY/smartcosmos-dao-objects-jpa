package net.smartcosmos.dao.objects.domain;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.RandomStringUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("Duplicates")
public class ObjectEntityTest {

    private static Validator validator;

    private static final String URN = RandomStringUtils.randomAlphanumeric(767);
    private static final String URN_INVALID = RandomStringUtils.randomAlphanumeric(768);
    private static final UUID ID = UuidUtil.getNewUuid();
    private static final String TYPE = RandomStringUtils.randomAlphanumeric(255);
    private static final String TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final Boolean ACTIVE = false;
    private static final UUID ACCOUNT_ID = UuidUtil.getNewUuid();

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatEverythingIsOk() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    // region URN

    @Test
    public void thatUrnIsNotNull() {

        ThingEntity objectEntity = ThingEntity.builder()
//            .urn(URN)
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("urn", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatUrnIsNotEmpty() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn("")
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("urn", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatUrnInvalidFails() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN_INVALID)
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("urn", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Type

    @Test
    public void thatTypeIsNotNull() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
//            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("type", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatTypeIsNotEmpty() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
            .type("")
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("type", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatTypeInvalidFails() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
            .type(TYPE_INVALID)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("type", violationSet.iterator().next().getPropertyPath().toString());
    }

    // endregion

    // region Other

    @Test
    public void thatAccountIdIsNotNull() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
//            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}",
            violationSet.iterator().next().getMessageTemplate());
        assertEquals("tenantId", violationSet.iterator().next().getPropertyPath().toString());
    }

    @Test
    public void thatActiveIsNotNullable() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
            .type(TYPE)
            .active(null)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
        assertTrue(objectEntity.getActive());
    }

    @Test
    public void thatActiveDefaultsToTrue() {

        ThingEntity objectEntity = ThingEntity.builder()
            .urn(URN)
            .id(ID)
            .type(TYPE)
//            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        assertTrue(objectEntity.getActive());
    }

    // endregion
}
