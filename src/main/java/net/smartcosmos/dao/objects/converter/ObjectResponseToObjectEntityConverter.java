package net.smartcosmos.dao.objects.converter;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.security.user.SmartCosmosUserHolder;

import net.smartcosmos.util.UuidUtil;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

/**
 * @author voor
 */
@Component
public class ObjectResponseToObjectEntityConverter
    implements Converter<ObjectResponse, ObjectEntity>, FormatterRegistrar {

    @Override
    public ObjectEntity convert(ObjectResponse objectCreate) {

        // Retrieve current user.
        SmartCosmosUser user = SmartCosmosUserHolder.getCurrentUser();

        return ObjectEntity.builder()
            // Required
            .objectUrn(objectCreate.getObjectUrn()).type(objectCreate.getType())
            .name(objectCreate.getName())
            .accountId(UuidUtil.getUuidFromAccountUrn(user.getAccountUrn()))
            // Optional
            .activeFlag(objectCreate.getActiveFlag())
            .description(objectCreate.getDescription())
            .moniker(objectCreate.getMoniker()).build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
