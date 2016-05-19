package net.smartcosmos.dao.objects.util;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.util.UuidUtil;

public class ObjectsPersistenceUtil {

    public static ObjectEntity merge(ObjectEntity objectEntity, ObjectUpdate updateObject) {

        if (updateObject.getObjectUrn() != null) {
            objectEntity.setObjectUrn(updateObject.getObjectUrn());
        }

        if (updateObject.getUrn() != null) {
            objectEntity.setId(UuidUtil.getUuidFromUrn(updateObject.getUrn()));
        }

        if (updateObject.getName() != null && !updateObject.getName().isEmpty()) {
            objectEntity.setName(updateObject.getName());
        }

        if (updateObject.getActiveFlag() != null) {
            objectEntity.setActiveFlag(updateObject.getActiveFlag());
        }

        if (updateObject.getDescription() != null) {
            objectEntity.setDescription(updateObject.getDescription());
        }

        if (updateObject.getMoniker() != null) {
            objectEntity.setMoniker(updateObject.getMoniker());
        }

        return objectEntity;
    }
}
