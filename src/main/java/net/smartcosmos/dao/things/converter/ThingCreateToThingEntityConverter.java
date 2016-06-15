package net.smartcosmos.dao.things.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.security.user.SmartCosmosUser;
import net.smartcosmos.security.user.SmartCosmosUserHolder;
import net.smartcosmos.util.UuidUtil;

@Component
public class ThingCreateToThingEntityConverter
        implements Converter<ThingCreate, ThingEntity>, FormatterRegistrar {

    @Override
    public ThingEntity convert(ThingCreate objectCreate) {

        // Retrieve current user.
        SmartCosmosUser user = SmartCosmosUserHolder.getCurrentUser();

        return ThingEntity.builder()
                // Required
                .urn(objectCreate.getUrn()).type(objectCreate.getType())
                .tenantId(UuidUtil.getUuidFromAccountUrn(user.getAccountUrn()))
                // Optional
                .active(objectCreate.getActive())
                .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
