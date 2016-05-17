package net.smartcosmos.dao.objects.converter;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.security.user.SmartCosmosUserHolder;

import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ObjectUpdateToObjectEntityConverter
    implements Converter<ObjectUpdate, ObjectEntity>, FormatterRegistrar {

    @Override
    public ObjectEntity convert(ObjectUpdate objectUpdate) {

        // Retrieve current user.
        SmartCosmosUser user = SmartCosmosUserHolder.getCurrentUser();

        return ObjectEntity.builder()
            // Required
            .objectUrn(objectUpdate.getObjectUrn()).type(objectUpdate.getType())
            .name(objectUpdate.getName())
            .accountUrn(UuidUtil.getUuidFromAccountUrn(user.getAccountUrn()))
            // Optional
            .activeFlag(objectUpdate.getActiveFlag())
            .description(objectUpdate.getDescription())
            .moniker(objectUpdate.getMoniker()).build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
