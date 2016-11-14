package net.smartcosmos.dao.things.converter;

import java.util.UUID;

import org.junit.*;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.ThingCreate;

/**
 * Tests the new utility class as a better alternative to the Conversion Service.
 */
public class ThingCreateToThingEntityConverterUtilityTest {

    @Test
    public void convertWithBlankUrn() throws Exception {

        ThingCreate thingCreate = ThingCreate.builder()
            .type("type")
            .active(true)
            .build();

        ThingEntity thingEntity = ThingCreateToThingEntityConverterUtility.convert(thingCreate, UUID.fromString("a3d135e3-a45b-44ad-8f71-319889df7824"));

        Assert.assertEquals(UUID.fromString("a3d135e3-a45b-44ad-8f71-319889df7824"),thingEntity.getTenantId());
        Assert.assertEquals("type", thingEntity.getType());
        Assert.assertTrue(thingEntity.getActive());
        // FIXME This should actually be blank...
        Assert.assertTrue(thingEntity.getId() != null);
    }

}
