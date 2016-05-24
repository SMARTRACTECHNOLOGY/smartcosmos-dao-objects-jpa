package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.IObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.domain.ExampleMatcher.StringMatcher.STARTING;

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
    public Optional<ObjectResponse> update(String accountUrn, ObjectUpdate updateObject) {

        Optional<ObjectEntity> entity = objectRepository.findByAccountIdAndObjectUrn(UuidUtil.getUuidFromAccountUrn(accountUrn), updateObject.getObjectUrn());

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

        Optional<ObjectEntity> entity = objectRepository.findByAccountIdAndObjectUrn(UuidUtil.getUuidFromAccountUrn(accountUrn), objectUrn);

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
    public List<ObjectResponse> findByObjectUrnStartsWith(String accountUrn, String objectUrn) {

        List<ObjectEntity> entityList = objectRepository.findByAccountIdAndObjectUrnStartsWith(UuidUtil.getUuidFromAccountUrn(accountUrn), objectUrn);

        return entityList.stream()
            .map(o -> conversionService.convert(o, ObjectResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ObjectResponse> findByUrn(String accountUrn, String urn) {

        Optional<ObjectEntity> entity = objectRepository.findByAccountIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn), UuidUtil.getUuidFromUrn(urn));

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

    /**
     *
     * @param accountUrn
     * @param queryParameters
     * @return
     *
     * Finds objects matching specified query parameters. List of parameters to check is lifted
     * directly from the Objects V2 specification.
     *
     */
    public List<ObjectResponse> findByQueryParameters(String accountUrn, Map<QueryParameterType, Object> queryParameters) {

        ObjectEntity.ObjectEntityBuilder builder = ObjectEntity.builder();
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(STARTING);
            //.withMatcher(QueryParameterType.TYPE.typeName(), exact()) // would be nice, but broken in Spring

        builder.objectUrn(MapUtils.getString(queryParameters, QueryParameterType.OBJECT_URN_LIKE));
        builder.type(MapUtils.getString(queryParameters, QueryParameterType.TYPE));
        builder.name(MapUtils.getString(queryParameters, QueryParameterType.NAME_LIKE));
        builder.moniker(MapUtils.getString(queryParameters, QueryParameterType.MONIKER_LIKE));

        // findByExample doesn't deal with dates, so we have to do it ourselves
        Long modifiedAfterDate = null;

        if (MapUtils.getLong(queryParameters, QueryParameterType.MODIFIED_AFTER) != null){
            modifiedAfterDate = (Long) queryParameters.get(QueryParameterType.MODIFIED_AFTER);
        }
        ObjectEntity exampleEntity = builder.build();

        Example<ObjectEntity> example = Example.of(exampleEntity, matcher);

        Iterable<ObjectEntity> queryResult =  objectRepository.findAll(example);
        List<ObjectResponse> returnValue = new ArrayList<>();

        for (ObjectEntity singleResult : queryResult)
        {
            // created is set at object creation time, and lastModified is not
            Long singleResultLastModified = singleResult.getLastModified();
            if(modifiedAfterDate == null || singleResultLastModified > modifiedAfterDate)
            {
                returnValue.add(conversionService.convert(singleResult, ObjectResponse.class));
            }
        }
        return returnValue;
    }
}
