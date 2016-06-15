package net.smartcosmos.dao.things.util;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.objects.ObjectUpdate;

public class ThingPersistenceUtil {

    public static ThingEntity merge(ThingEntity objectEntity, ObjectUpdate updateObject) {

        if (updateObject.getName() != null) {
            objectEntity.setName(updateObject.getName());
        }

        if (updateObject.getType() != null) {
            objectEntity.setType(updateObject.getType());
        }

        if (updateObject.getDescription() != null) {
            objectEntity.setDescription(updateObject.getDescription());
        }

        if (updateObject.getMoniker() != null) {
            objectEntity.setMoniker(updateObject.getMoniker());
        }

        if (updateObject.getActiveFlag() != null) {
            objectEntity.setActive(updateObject.getActiveFlag());
        }

        return objectEntity;
    }
}
