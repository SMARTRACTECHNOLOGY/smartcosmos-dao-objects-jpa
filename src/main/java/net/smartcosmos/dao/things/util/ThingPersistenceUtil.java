package net.smartcosmos.dao.things.util;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.PageInformation;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;

import java.util.ArrayList;

public class ThingPersistenceUtil {

    public static ThingEntity merge(ThingEntity thingEntity, ThingUpdate updateThing) {

        if (updateThing.getActive() != null) {
            thingEntity.setActive(updateThing.getActive());
        }

        return thingEntity;
    }

    public static Page<ThingResponse> emptyPage() {

        return new Page<>(new ArrayList<>(), new PageInformation());
    }
}
