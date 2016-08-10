package net.smartcosmos.dao.things.util;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.domain.Sort;

import net.smartcosmos.dao.things.SortOrder;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;

public class ThingPersistenceUtil {

    /**
     * Merges a {@link ThingEntity} instance with the content of a {@link ThingUpdate} instance.
     *
     * @param thingEntity the existing Thing
     * @param updateThing the update content
     * @return the merged entity
     */
    public static ThingEntity merge(ThingEntity thingEntity, ThingUpdate updateThing) {

        if (updateThing.getActive() != null) {
            thingEntity.setActive(updateThing.getActive());
        }

        return thingEntity;
    }

    /**
     * Transforms a field name for a sorted query to a valid case-sensitive field name that exists in the entity class.
     * Returns the input field name, if it does not exist in the entity class.
     *
     * @param fieldName the input field name
     * @return the case-corrected field name
     */
    public static String normalizeFieldName(String fieldName) {

        if (StringUtils.equalsIgnoreCase("urn", fieldName) || StringUtils.equalsIgnoreCase("id", fieldName)) {
            return "id";
        }

        if (StringUtils.equalsIgnoreCase("type", fieldName)) {
            return "type";
        }

        if (StringUtils.equalsIgnoreCase("tenantUrn", fieldName) || StringUtils.equalsIgnoreCase("tenantId", fieldName)) {
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

    /**
     * Checks if a given field name exists in {@link ThingEntity}.
     *
     * @param fieldName the field name
     * @return {@code true} if the field exists
     */
    public static boolean isThingEntityField(String fieldName) {

        try {
            ThingEntity.class.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return false;
        }

        return true;
    }

    /**
     * Gets a valid field name for a {@code sortBy} query in the {@link ThingEntity} data base.
     * The input field name is case-corrected and replaced by {@code id} if it does not exist in the entity class.
     *
     * @param sortBy the input field name
     * @return the case-corrected field name if it exists, {@code id} otherwise
     */
    public static String getSortByFieldName(String sortBy) {

        sortBy = ThingPersistenceUtil.normalizeFieldName(sortBy);
        if (StringUtils.isBlank(sortBy) || !ThingPersistenceUtil.isThingEntityField(sortBy)) {
            sortBy = "id";
        }
        return sortBy;
    }

    /**
     * Converts the {@link SortOrder} value to a Spring-compatible {@link org.springframework.data.domain.Sort.Direction} sort direction.
     *
     * @param sortOrder the sort order
     * @return the Spring sort direction
     */
    public static Sort.Direction getSortDirection(SortOrder sortOrder) {

        Sort.Direction direction = Sort.DEFAULT_DIRECTION;
        switch (sortOrder) {
            case ASC:
                direction = Sort.Direction.ASC;
                break;
            case DESC:
                direction = Sort.Direction.DESC;
                break;
        }
        return direction;
    }

    /**
     * Creates an empty {@link Page<ThingResponse>} instance.
     *
     * @return the empty page
     */
    public static Page<ThingResponse> emptyPage() {

        return Page.<ThingResponse>builder().build();
    }
}
