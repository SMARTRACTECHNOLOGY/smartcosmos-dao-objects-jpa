package net.smartcosmos.dao.things.converter;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.ThingCreate;

public final class ThingCreateToThingEntityConverterUtility {
    public static ThingEntity convert(ThingCreate thingCreate, UUID tenantId) {

        UUID id;
        if (StringUtils.isBlank(thingCreate.getUrn())) {
            id = UuidUtil.getNewUuid();
        } else {
            id = UuidUtil.getUuidFromUrn(thingCreate.getUrn());
        }

        return ThingEntity.builder()
            .id(id)
            .type(thingCreate.getType())
            .tenantId(tenantId)
            .active(thingCreate.getActive())
            .build();
    }
}
