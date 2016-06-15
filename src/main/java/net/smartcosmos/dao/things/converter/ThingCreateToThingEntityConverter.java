package net.smartcosmos.dao.things.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dto.things.ThingCreate;

@Component
public class ThingCreateToThingEntityConverter
        implements Converter<ThingCreate, ThingEntity>, FormatterRegistrar {

    @Override
    public ThingEntity convert(ThingCreate thingCreate) {

        return ThingEntity.builder()
            .urn(thingCreate.getUrn())
            .type(thingCreate.getType())
            .active(thingCreate.getActive())
            .build();
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {
        registry.addConverter(this);
    }
}
