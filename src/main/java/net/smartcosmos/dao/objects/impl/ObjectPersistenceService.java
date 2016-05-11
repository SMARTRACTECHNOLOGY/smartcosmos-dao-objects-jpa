package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.IObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
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
    private final ConversionService conversionService;

    @Autowired
    public ObjectPersistenceService(IObjectRepository objectRepository,
            ConversionService conversionService) {
        this.objectRepository = objectRepository;
        this.conversionService = conversionService;
    }

    @Override
    public ObjectResponse create(String accountUrn, ObjectCreate createObject) {

        ObjectEntity entity = conversionService.convert(createObject, ObjectEntity.class);
        entity = objectRepository.save(entity);

        return conversionService.convert(entity, ObjectResponse.class);
    }

    @Override
    public Optional<ObjectResponse> update(String accountUrn, ObjectResponse updateObject) {

        Optional<ObjectEntity> entity = objectRepository.findByObjectUrn(updateObject.getObjectUrn());

        if (entity.isPresent()) {
            ObjectEntity entity2 = conversionService.convert(updateObject, ObjectEntity.class);
            entity2.setId(entity.get().getId());
            entity2 = objectRepository.save(entity2);
            final ObjectResponse response = conversionService.convert(entity2, ObjectResponse.class);
            return Optional.ofNullable(response);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ObjectResponse> findByObjectUrn(String accountUrn, String objectUrn) {

        Optional<ObjectEntity> entity = objectRepository.findByObjectUrn(objectUrn);

        if (entity.isPresent()) {
            final ObjectResponse response = conversionService.convert(entity.get(),
                    ObjectResponse.class);
            return Optional.ofNullable(response);
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<ObjectResponse> findByUrn(String accountUrn, String urn) {

        Optional<ObjectEntity> entity = objectRepository.findById(UuidUtil.getUuidFromUrn(urn));

        if (entity.isPresent()) {
            final ObjectResponse response = conversionService.convert(entity.get(),
                ObjectResponse.class);
            return Optional.ofNullable(response);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * This is a temporary function for development purposes -- eventually we don't want
     * to support a "get everything" call, since theoretically that'd be billions of
     * objects.
     *
     * @return All the objects.
     */
    public List<ObjectResponse> getObjects() {
        // You could theoretically create a conversion function to handle this, since
        // it'll happen fairly often and in numerous places, but for example sake it's
        // done inline here.
        return objectRepository.findAll().stream()
                .map(o -> conversionService.convert(o, ObjectResponse.class))
                .collect(Collectors.toList());
    }
}
