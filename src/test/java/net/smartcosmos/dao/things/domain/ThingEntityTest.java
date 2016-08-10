package net.smartcosmos.dao.things.domain;

import java.util.Set;
import java.util.UUID;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.*;

import static org.junit.Assert.*;

@SuppressWarnings("Duplicates")
public class ThingEntityTest {

    private static Validator validator;

    private static final UUID ID = UUID.randomUUID();
    private static final String TYPE = RandomStringUtils.randomAlphanumeric(255);
    private static final String TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final Boolean ACTIVE = false;
    private static final UUID ACCOUNT_ID = UUID.randomUUID();

    @BeforeClass
    public static void setUp() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatEverythingIsOk() {

        ThingEntity objectEntity = ThingEntity.builder()
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    // region Type

    @Test
    public void thatTypeIsNotNull() {

        ThingEntity objectEntity = ThingEntity.builder()
            .id(ID)
            //            .type(TYPE)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("type",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    @Test
    public void thatTypeIsNotEmpty() {

        ThingEntity objectEntity = ThingEntity.builder()
            .id(ID)
            .type("")
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{org.hibernate.validator.constraints.NotEmpty.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("type",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    @Test
    public void thatTypeInvalidFails() {

        ThingEntity objectEntity = ThingEntity.builder()
            .id(ID)
            .type(TYPE_INVALID)
            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.Size.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("type",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    // endregion

    // region Other

    @Test
    public void thatAccountIdIsNotNull() {

        ThingEntity objectEntity = ThingEntity.builder()
            .id(ID)
            .type(TYPE)
            .active(ACTIVE)
            //            .tenantId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ThingEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
        assertEquals(1, violationSet.size());
        assertEquals("{javax.validation.constraints.NotNull.message}",
                     violationSet.iterator()
                         .next()
                         .getMessageTemplate());
        assertEquals("tenantId",
                     violationSet.iterator()
                         .next()
                         .getPropertyPath()
                         .toString());
    }

    @Test
    public void thatActiveIsNotNullable() {

        ThingEntity objectEntity = ThingEntity.builder()
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
            .id(ID)
            .type(TYPE)
            //            .active(ACTIVE)
            .tenantId(ACCOUNT_ID)
            .build();

        assertTrue(objectEntity.getActive());
    }

    // endregion
}
