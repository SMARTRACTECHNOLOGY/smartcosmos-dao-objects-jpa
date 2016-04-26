package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.IObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.CreateObjectDto;
import net.smartcosmos.dto.objects.GetObjectDto;
import net.smartcosmos.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author voor
 */
@Service
public class ObjectPersistenceService implements IObjectDao {

    private final IObjectRepository objectRepository;

    @Autowired
    public ObjectPersistenceService(IObjectRepository objectRepository) {
        this.objectRepository = objectRepository;
    }

    @Override
    public GetObjectDto create(String accountUrn, CreateObjectDto createObject) {

        final ObjectEntity entity = objectRepository.save(ObjectEntity.builder()
            // Required
            .objectUrn(createObject.getObjectUrn()).type(createObject.getType())
            .name(createObject.getName())
            // XXX This is temporary.
            .accountUrn(accountUrn)
            // Optional
            .activeFlag(createObject.getActiveFlag())
            .description(createObject.getDescription())
            .moniker(createObject.getMoniker()).build());

        return GetObjectDto.builder()
            // Required
            .urn(UuidUtil.getUrnFromUuid(entity.getId()))
            .objectUrn(entity.getObjectUrn())
            .accountUrn(entity.getAccountUrn())
            .type(entity.getType()).name(entity.getName())
            .lastModifiedTimestamp(entity.getLastModified() != null
                ? entity.getLastModified().getTime() : null)
            .activeFlag(entity.getActiveFlag())
            // Optional
            .moniker(entity.getMoniker()).description(entity.getDescription())
            // Don't forget to build it!
            .build();
    }

    @Override
    public Optional<GetObjectDto> findByObjectUrn(String accountUrn, String objectUrn) {
        return objectRepository.findByObjectUrn(objectUrn)
            .map(o -> GetObjectDto.builder()
                // Required
                .urn(UuidUtil.getUrnFromUuid(o.getId()))
                .objectUrn(o.getObjectUrn()).accountUrn(o.getAccountUrn())
                .type(o.getType()).name(o.getName())
                .lastModifiedTimestamp(o.getLastModified() != null
                    ? o.getLastModified().getTime() : null)
                .activeFlag(o.getActiveFlag())
                // Optional
                .moniker(o.getMoniker()).description(o.getDescription())
                // Don't forget to build it!
                .build());
    }

    /**
     * This is a temporary function for development purposes -- eventually we don't want to support a "get everything" call, since theoretically that'd be billions of objects.
     *
     * @return All the objects.
     */
    public List<GetObjectDto> getObjects() {
        // You could theoretically create a conversion function to handle this, since
        // it'll happen fairly often and in numerous places, but for example sake it's
        // done inline here.
        return objectRepository.findAll().stream()
            .map(o -> GetObjectDto.builder()
                // Required
                .urn(UuidUtil.getUrnFromUuid(o.getId()))
                .objectUrn(o.getObjectUrn()).accountUrn(o.getAccountUrn())
                .type(o.getType()).name(o.getName())
                .lastModifiedTimestamp(o.getLastModified() != null
                    ? o.getLastModified().getTime() : null)
                .activeFlag(o.getActiveFlag())
                // Optional
                .moniker(o.getMoniker()).description(o.getDescription())
                // Don't forget to build it!
                .build())
            .collect(Collectors.toList());
    }
}
