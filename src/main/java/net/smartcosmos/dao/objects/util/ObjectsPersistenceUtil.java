package net.smartcosmos.dao.objects.util;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectUpdate;

public class ObjectsPersistenceUtil {

    public static ObjectEntity merge(ObjectEntity objectEntity, ObjectUpdate updateObject) {

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
            objectEntity.setActiveFlag(updateObject.getActiveFlag());
        }

        return objectEntity;
    }
}
