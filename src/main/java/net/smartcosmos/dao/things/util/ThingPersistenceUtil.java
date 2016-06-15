package net.smartcosmos.dao.things.util;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.ThingUpdate;

public class ThingPersistenceUtil {

    public static ThingEntity merge(ThingEntity thingEntity, ThingUpdate updateThing) {

        if (updateThing.getType() != null) {
            thingEntity.setType(updateThing.getType());
        }

        if (updateThing.getActive() != null) {
            thingEntity.setActive(updateThing.getActive());
        }

        return thingEntity;
    }
}
