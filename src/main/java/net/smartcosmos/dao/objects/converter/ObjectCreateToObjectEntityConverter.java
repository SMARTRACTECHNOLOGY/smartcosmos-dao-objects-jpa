package net.smartcosmos.dao.objects.converter;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectCreate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistrar;
import org.springframework.format.FormatterRegistry;
import org.springframework.stereotype.Component;

/**
 * @author voor
 */
@Component
public class ObjectCreateToObjectEntityConverter
        implements Converter<ObjectCreate, ObjectEntity>, FormatterRegistrar {

    @Override
    public ObjectEntity convert(ObjectCreate objectCreate) {

        return ObjectEntity.builder()
                // Required
                .objectUrn(objectCreate.getObjectUrn()).type(objectCreate.getType())
                .name(objectCreate.getName())
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
