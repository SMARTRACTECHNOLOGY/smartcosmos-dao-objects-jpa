package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.IObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dao.objects.util.ObjectsPersistenceUtil;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.util.UuidUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;
import static org.springframework.data.domain.ExampleMatcher.StringMatcher.STARTING;

/**
 * @author voor
 */
@Service
public class ObjectPersistenceService implements IObjectDao {

    public static final String OBJECT_URN_LIKE = "objectUrnLike";
    public static final String TYPE = "type";
    public static final String NAME_LIKE = "nameLike";
    public static final String MONIKER_LIKE = "monikerLike";
    public static final String MODIFIED_AFTER = "modifiedAfter";

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
    public Optional<ObjectResponse> update(String accountUrn, @Valid ObjectUpdate updateObject) {

        Optional<ObjectEntity> entity = findEntity(UuidUtil.getUuidFromAccountUrn(accountUrn), updateObject.getUrn(), updateObject.getObjectUrn());

        if (entity.isPresent()) {
            ObjectEntity updateEntity = ObjectsPersistenceUtil.merge(entity.get(), updateObject);
            updateEntity = objectRepository.save(updateEntity);

            final ObjectResponse response = conversionService.convert(updateEntity, ObjectResponse.class);
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
    public List<ObjectResponse> findByQueryParameters(String accountUrn, Map<String, Object> queryParameters) {

        ObjectEntity.ObjectEntityBuilder builder = ObjectEntity.builder();
        ExampleMatcher matcher = ExampleMatcher.matching()
            .withStringMatcher(STARTING)
            .withMatcher(TYPE, exact());

        if (queryParameters.containsKey(OBJECT_URN_LIKE)){
            builder.objectUrn((String)queryParameters.get(OBJECT_URN_LIKE));
        }
        if (queryParameters.containsKey(TYPE)){
            builder.type((String)queryParameters.get(TYPE));
        }
        if (queryParameters.containsKey(NAME_LIKE)){
            builder.name((String)queryParameters.get(NAME_LIKE));
        }
        if (queryParameters.containsKey(MONIKER_LIKE)){
            builder.moniker((String)queryParameters.get(MONIKER_LIKE));
        }

        // findByExample doesn't deal with dates, so we have to do it ourselves
        Long modifiedAfterDate = null;

        if (queryParameters.containsKey(MODIFIED_AFTER)){
            modifiedAfterDate = (Long) queryParameters.get(MODIFIED_AFTER);
        }
        ObjectEntity exampleEntity = builder.build();

        Example<ObjectEntity> example = Example.of(exampleEntity, matcher);

        Iterable<ObjectEntity> queryResult =  objectRepository.findAll(example);
        List<ObjectResponse> returnValue = new ArrayList<>();
        for (ObjectEntity singleResult : queryResult)
        {
            // created is set at object creation time, and lastModified is not
            Long singleResultLastModified = singleResult.getLastModified();
            if (singleResultLastModified == null){
                singleResultLastModified = singleResult.getCreated();
            }
            if(modifiedAfterDate == null || singleResultLastModified > modifiedAfterDate)
            {
                returnValue.add(conversionService.convert(singleResult, ObjectResponse.class));
            }
        }
        return returnValue;
    }

    private Optional<ObjectEntity> findEntity(UUID accountId, String urn, String objectUrn) throws IllegalArgumentException {

        Optional<ObjectEntity> entity = Optional.empty();
        UUID id = UuidUtil.getUuidFromUrn(urn);

        if (id != null) {
            entity = objectRepository.findByAccountIdAndId(accountId, id);
        }

        if (!StringUtils.isEmpty(objectUrn)) {
            entity = objectRepository.findByAccountIdAndObjectUrn(accountId, objectUrn);
        }

        if (entity.isPresent() && !StringUtils.isEmpty(objectUrn) && id != null) {
            if (!objectUrn.equals(entity.get().getObjectUrn()) || !id.equals(entity.get().getId())) {
                throw new IllegalArgumentException("urn and objectUrn do not match");
            }
        }

        return entity;
    }
}
