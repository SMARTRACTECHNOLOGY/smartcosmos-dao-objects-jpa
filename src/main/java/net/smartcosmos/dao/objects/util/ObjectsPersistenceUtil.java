package net.smartcosmos.dao.objects.util;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectUpdate;
import org.springframework.util.StringUtils;

public class ObjectsPersistenceUtil {

    public static ObjectEntity merge(ObjectEntity objectEntity, ObjectUpdate updateObject) {

        if (!StringUtils.isEmpty(updateObject.getName())) {
            objectEntity.setName(updateObject.getName());
        }

        if (!StringUtils.isEmpty(updateObject.getType())) {
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
