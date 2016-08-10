package net.smartcosmos.dao.things.converter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.ThingResponse;

@Component
public class ThingEntityToThingResponseConverter
    implements Converter<ThingEntity, ThingResponse>, FormatterRegistrar {

    @Override
    public ThingResponse convert(ThingEntity entity) {

        if (entity == null) {
            return null;
        }

        return ThingResponse.builder()
            // Required
            .urn(UuidUtil.getThingUrnFromUuid(entity.getId()))
            .type(entity.getType())
            .active(entity.getActive())
            .tenantUrn(UuidUtil.getTenantUrnFromUuid(entity.getTenantId()))
            // Don't forget to build it!
            .build();
    }

    public List convertAll(Iterable<ThingEntity> entities) {

        List<ThingResponse> convertedList = new ArrayList<>();
        for (ThingEntity entity : entities) {
            convertedList.add(convert(entity));
        }
        return convertedList;
    }

    @Override
    public void registerFormatters(FormatterRegistry registry) {

        registry.addConverter(this);
    }
}
