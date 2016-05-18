package net.smartcosmos.dao.objects.converter;

    import net.smartcosmos.dto.objects.ObjectResponse;
    import net.smartcosmos.dto.objects.ObjectUpdate;

    import net.smartcosmos.util.UuidUtil;
    import org.springframework.core.convert.converter.Converter;
    import org.springframework.format.FormatterRegistrar;
    import org.springframework.format.FormatterRegistry;
    import org.springframework.stereotype.Component;


@Component
public class ObjectResponseToObjectUpdateConverter
    implements Converter<ObjectResponse, ObjectUpdate>, FormatterRegistrar {

    @Override
    public ObjectUpdate convert(ObjectResponse objectCreate) {

        return ObjectUpdate.builder()
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
