package net.smartcosmos.dao.things.util;

import net.smartcosmos.dao.things.SortOrder;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.PageInformation;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;

public class ThingPersistenceUtil {

    public static ThingEntity merge(ThingEntity thingEntity, ThingUpdate updateThing) {

        if (updateThing.getActive() != null) {
            thingEntity.setActive(updateThing.getActive());
        }

        return thingEntity;
    }

    public static String normalizeFieldName(String fieldName) {

        if (StringUtils.equalsIgnoreCase("urn", fieldName)) {
            return "id";
        }

        if (StringUtils.equalsIgnoreCase("type", fieldName)) {
            return "type";
        }

        if (StringUtils.equalsIgnoreCase("tenantUrn", fieldName)) {
            return "tenantId";
        }

        if (StringUtils.equalsIgnoreCase("created", fieldName)) {
            return "created";
        }

        if (StringUtils.equalsIgnoreCase("lastModified", fieldName)) {
            return "lastModified";
        }

        if (StringUtils.equalsIgnoreCase("active", fieldName)) {
            return "active";
        }

        return fieldName;
    }

    public static boolean isThingEntityField(String fieldName) {

        try {
            ThingEntity.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return false;
        }

        return true;
    }

    public static String getSortByFieldName(String sortBy) {
        sortBy = ThingPersistenceUtil.normalizeFieldName(sortBy);
        if (StringUtils.isBlank(sortBy) || !ThingPersistenceUtil.isThingEntityField(sortBy)) {
            sortBy = "id";
        }
        return sortBy;
    }

    public static Sort.Direction getSortDirection(SortOrder sortOrder) {
        Sort.Direction direction = Sort.DEFAULT_DIRECTION;
        switch (sortOrder) {
            case ASC: direction = Sort.Direction.ASC; break;
            case DESC: direction = Sort.Direction.DESC; break;
        }
        return direction;
    }

    public static Page<ThingResponse> emptyPage() {

        return new Page<>(new ArrayList<>(), new PageInformation());
    }
}
