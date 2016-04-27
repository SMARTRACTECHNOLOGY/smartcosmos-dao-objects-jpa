package net.smartcosmos.dao.objects.converter;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.security.user.SmartCosmosUserHolder;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * @author voor
 */
@Component
public class ObjectCreateToObjectEntityConverter
        implements Converter<ObjectCreate, ObjectEntity> {

    @Override
    public ObjectEntity convert(ObjectCreate objectCreate) {

        // Retrieve current user.
        SmartCosmosUser user = SmartCosmosUserHolder.getCurrentUser();

        return ObjectEntity.builder()
                // Required
                .objectUrn(objectCreate.getObjectUrn()).type(objectCreate.getType())
                .name(objectCreate.getName()).accountUrn(user.getAccountUrn())
                // Optional
                .activeFlag(objectCreate.getActiveFlag())
                .description(objectCreate.getDescription())
                .moniker(objectCreate.getMoniker()).build();
    }
}
