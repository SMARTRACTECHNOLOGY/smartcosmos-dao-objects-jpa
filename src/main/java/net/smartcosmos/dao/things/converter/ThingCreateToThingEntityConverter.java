package net.smartcosmos.dao.things.converter;

import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.ThingCreate;

@Component
public class ThingCreateToThingEntityConverter
    implements Converter<ThingCreate, ThingEntity>, FormatterRegistrar {

    @Override
    public ThingEntity convert(ThingCreate thingCreate) {

        UUID id;
        if (StringUtils.isBlank(thingCreate.getUrn())) {
            id = UuidUtil.getNewUuid();
        } else {
            id = UuidUtil.getUuidFromUrn(thingCreate.getUrn());
        }

        return ThingEntity.builder()
            .id(id)
            .type(thingCreate.getType())
            .active(thingCreate.getActive())
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {

        registry.addConverter(this);
    }
}
