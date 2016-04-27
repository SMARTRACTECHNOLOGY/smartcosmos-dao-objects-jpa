package net.smartcosmos.dao.objects.converter;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.util.UuidUtil;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * @author voor
 */
@Component
public class ObjectEntityToObjectResponseConverter
        implements Converter<ObjectEntity, ObjectResponse> {

    @Override
    public ObjectResponse convert(ObjectEntity entity) {

        return ObjectResponse.builder()
                // Required
                .urn(UuidUtil.getUrnFromUuid(entity.getId()))
                .objectUrn(entity.getObjectUrn()).accountUrn(entity.getAccountUrn())
                .type(entity.getType()).name(entity.getName())
                .lastModifiedTimestamp(entity.getLastModified() != null
                        ? entity.getLastModified().getTime() : null)
                .activeFlag(entity.getActiveFlag())
                // Optional
                .moniker(entity.getMoniker()).description(entity.getDescription())
                // Don't forget to build it!
                .build();
    }
}
