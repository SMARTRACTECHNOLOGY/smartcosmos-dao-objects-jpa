package net.smartcosmos.dao.objects.domain;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectEntityTest {

    private static Validator validator;

    private static final String OBJECT_URN = RandomStringUtils.randomAlphanumeric(767);
    private static final String OBJECT_URN_INVALID = RandomStringUtils.randomAlphanumeric(768);
    private static final UUID ID = UuidUtil.getNewUuid();
    private static final String TYPE = RandomStringUtils.randomAlphanumeric(255);
    private static final String TYPE_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final String NAME = RandomStringUtils.randomAlphanumeric(255);
    private static final String NAME_INVALID = RandomStringUtils.randomAlphanumeric(256);
    private static final String DESCRIPTION = RandomStringUtils.randomAlphanumeric(1024);
    private static final String DESCRIPTION_INVALID = RandomStringUtils.randomAlphanumeric(1025);
    private static final String MONIKER = RandomStringUtils.randomAlphanumeric(2048);
    private static final String MONIKER_INVALID = RandomStringUtils.randomAlphanumeric(2049);
    private static final Boolean ACTIVE = false;
    private static final UUID ACCOUNT_ID = UuidUtil.getNewUuid();

    @BeforeClass
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void thatEverythingIsOk() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    // region Object URN

    @Test
    public void thatObjectUrnIsNotNull() {

        ObjectEntity objectEntity = ObjectEntity.builder()
//            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatObjectUrnIsNotEmpty() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn("")
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatObjectUrnInvalidFails() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN_INVALID)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    // endregion

    // region Name

    @Test
    public void thatNameIsNotNull() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
//            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatNameIsNotEmpty() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name("")
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatNameInvalidFails() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN_INVALID)
            .id(ID)
            .type(TYPE)
            .name(NAME_INVALID)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    // endregion

    // region Type

    @Test
    public void thatTypeIsNotNull() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
//            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatTypeIsNotEmpty() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type("")
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatTypeInvalidFails() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN_INVALID)
            .id(ID)
            .type(TYPE_INVALID)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    // endregion

    // region Description

    @Test
    public void thatDescriptionMayBeNull() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
//            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatDescriptionMayBeEmpty() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description("")
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatDescriptionInvalidFails() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN_INVALID)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION_INVALID)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    // endregion

    // region Moniker

    @Test
    public void thatMonikerMayBeNull() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
//            .moniker(MONIKER)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatMonikerMayBeEmpty() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker("")
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
    }

    @Test
    public void thatMonikerInvalidFails() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN_INVALID)
            .id(ID)
            .type(TYPE)
            .name(NAME_INVALID)
            .description(DESCRIPTION)
            .moniker(MONIKER_INVALID)
            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    // endregion

    // region Other

    @Test
    public void thatAccountIdIsNotNull() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(ACTIVE)
//            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertFalse(violationSet.isEmpty());
    }

    @Test
    public void thatActiveFlagIsNotNullable() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
            .activeFlag(null)
            .accountId(ACCOUNT_ID)
            .build();

        Set<ConstraintViolation<ObjectEntity>> violationSet = validator.validate(objectEntity);

        assertTrue(violationSet.isEmpty());
        assertTrue(objectEntity.getActiveFlag());
    }

    @Test
    public void thatActiveFlagDefaultsToTrue() {

        ObjectEntity objectEntity = ObjectEntity.builder()
            .objectUrn(OBJECT_URN)
            .id(ID)
            .type(TYPE)
            .name(NAME)
            .description(DESCRIPTION)
            .moniker(MONIKER)
//            .activeFlag(ACTIVE)
            .accountId(ACCOUNT_ID)
            .build();

        assertTrue(objectEntity.getActiveFlag());
    }

    // endregion
}
