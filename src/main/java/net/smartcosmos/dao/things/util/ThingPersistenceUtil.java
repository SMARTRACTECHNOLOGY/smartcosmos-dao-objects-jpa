package net.smartcosmos.dao.things.util;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.ThingUpdate;

public class ThingPersistenceUtil {

    public static ThingEntity merge(ThingEntity thingEntity, ThingUpdate updateObject) {

        if (updateObject.getType() != null) {
            thingEntity.setType(updateObject.getType());
        }

        if (updateObject.getActive() != null) {
            thingEntity.setActive(updateObject.getActive());
        }

        return thingEntity;
    }
}
