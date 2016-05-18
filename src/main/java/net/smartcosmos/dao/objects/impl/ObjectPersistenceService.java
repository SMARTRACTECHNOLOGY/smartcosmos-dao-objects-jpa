package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.IObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;
import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.*;
import static org.springframework.data.domain.ExampleMatcher.StringMatcher.ENDING;
import static org.springframework.data.domain.ExampleMatcher.StringMatcher.STARTING;
import static org.springframework.data.domain.ExampleMatcher.StringMatcher.EXACT;

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

        builder.objectUrn(MapUtils.getString(queryParameters, OBJECT_URN_LIKE));
        builder.type(MapUtils.getString(queryParameters, TYPE));
        builder.name(MapUtils.getString(queryParameters, NAME_LIKE));
        builder.moniker(MapUtils.getString(queryParameters, MONIKER_LIKE));

        // findByExample doesn't deal with dates, so we have to do it ourselves
        Date modifiedAfterDate = new Date(0);
        boolean modifiedAfterDateSpecified = false;

        if (queryParameters.containsKey(MODIFIED_AFTER)){
            modifiedAfterDate = (Date)queryParameters.get(MODIFIED_AFTER);
            modifiedAfterDateSpecified = true;
        }
        ObjectEntity exampleEntity = builder.build();

        Example<ObjectEntity> example = Example.of(exampleEntity, matcher);

        Iterable<ObjectEntity> queryResult =  objectRepository.findAll(example);
        List<ObjectResponse> returnValue = new ArrayList<>();
        for (ObjectEntity singleResult : queryResult)
        {
            // created is set at object creation time, and lastModified is not
            Date singleResultLastModified = singleResult.getLastModified();
            if (singleResultLastModified == null){
                singleResultLastModified = singleResult.getCreated();
            }
            if(!modifiedAfterDateSpecified || singleResultLastModified.after(modifiedAfterDate))
            {
                returnValue.add(conversionService.convert(singleResult, ObjectResponse.class));
            }
        }
        return returnValue;
    }
}
