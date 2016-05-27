package net.smartcosmos.dao.objects.impl;

import net.smartcosmos.dao.objects.ObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.IObjectRepository;
import net.smartcosmos.dao.objects.util.ObjectsPersistenceUtil;
import net.smartcosmos.dao.objects.util.SearchSpecifications;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;
import org.springframework.util.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * @author voor
 */
@Service
public class ObjectPersistenceService implements ObjectDao {

    private final IObjectRepository objectRepository;
    private final ConversionService conversionService;
    private final Validator validator;

    @Autowired
    public ObjectPersistenceService(IObjectRepository objectRepository,
                                    ConversionService conversionService, Validator basicValidator) {
        this.objectRepository = objectRepository;
        this.conversionService = conversionService;
        this.validator = basicValidator;
    }

    @Override
    public ObjectResponse create(String accountUrn, ObjectCreate createObject) {

        ObjectEntity entity = conversionService.convert(createObject, ObjectEntity.class);
        entity = persist(entity);

        return conversionService.convert(entity, ObjectResponse.class);
    }

    @Override
    public Optional<ObjectResponse> update(String accountUrn, ObjectUpdate updateObject) throws ConstraintViolationException {

        validate(updateObject);

        Optional<ObjectEntity> entity = findEntity(UuidUtil.getUuidFromAccountUrn(accountUrn), updateObject.getUrn(), updateObject.getObjectUrn());

        if (entity.isPresent()) {
            ObjectEntity updateEntity = ObjectsPersistenceUtil.merge(entity.get(), updateObject);

            updateEntity = persist(updateEntity);

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

    /**
     * Finds objects matching a specified object URN start.
     *
     * @param accountUrn the account URN
     * @param objectUrnStartsWith the first characters of the object URN
     * @return all objects whose {@code objectUrn} starts with {@code objectUrnStartsWith}
     */
    @Override
    public List<ObjectResponse> findByObjectUrnStartsWith(String accountUrn, String objectUrnStartsWith) {

        List<ObjectEntity> entityList = objectRepository.findByAccountIdAndObjectUrnStartsWith(UuidUtil.getUuidFromAccountUrn(accountUrn),
            objectUrnStartsWith);

        return entityList.stream()
            .map(o -> conversionService.convert(o, ObjectResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public Optional<ObjectResponse> findByUrn(String accountUrn, String urn) {

        Optional<ObjectEntity> entity = objectRepository.findByAccountIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn),
            UuidUtil.getUuidFromUrn(urn));

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

        SearchSpecifications searchSpecifications = new SearchSpecifications<ObjectEntity>();

        Specification accountUrnSpecification = null;
        if (accountUrn != null) {
            UUID accountUuid = UuidUtil.getUuidFromAccountUrn(accountUrn);
            accountUrnSpecification = searchSpecifications.matchUuid(accountUuid, "accountId");
        };

        Specification objectUrnSpecification = null;
        String objectUrnLike = MapUtils.getString(queryParameters, QueryParameterType.OBJECT_URN_LIKE);
        if (objectUrnLike != null) {
            objectUrnSpecification = searchSpecifications.stringStartsWith(objectUrnLike, QueryParameterType.OBJECT_URN_FIELD_NAME.typeName());
        };

        Specification nameLikeSpecification = null;
        String nameLike = MapUtils.getString(queryParameters, QueryParameterType.NAME_LIKE);
        if (nameLike != null) {
            nameLikeSpecification = searchSpecifications.stringStartsWith(nameLike, QueryParameterType.NAME_FIELD_NAME.typeName());
        };

        Specification typeSpecification = null;
        String type = MapUtils.getString(queryParameters, QueryParameterType.TYPE);
        if (type != null) {
            typeSpecification = searchSpecifications.stringMatchesExactly(type, QueryParameterType.TYPE_FIELD_NAME.typeName());
        };

        Specification monikerLikeSpecification = null;
        String monikerLike = MapUtils.getString(queryParameters, QueryParameterType.MONIKER_LIKE);
        if (monikerLike != null) {
            monikerLikeSpecification = searchSpecifications.stringStartsWith(monikerLike, QueryParameterType.MONIKER_FIELD_NAME.typeName());
        };

        Specification lastModifedAfterSpecification = null;
        Long lastModifedAfter = MapUtils.getLong(queryParameters, QueryParameterType.MODIFIED_AFTER);
        if (lastModifedAfter != null) {
            lastModifedAfterSpecification = searchSpecifications.numberGreaterThan(lastModifedAfter,
                QueryParameterType.MODIFIED_AFTER_FIELD_NAME.typeName());
        };

        Iterable<ObjectEntity> returnedValues = objectRepository.findAll(where(objectUrnSpecification)
            .and(accountUrnSpecification)
            .and(nameLikeSpecification)
            .and(typeSpecification)
            .and(monikerLikeSpecification)
            .and(lastModifedAfterSpecification));

        List<ObjectResponse> convertedList = new ArrayList<>();
        for (ObjectEntity entity: returnedValues) {
            convertedList.add(conversionService.convert(entity, ObjectResponse.class));
        }
        return convertedList;

    }

    private Optional<ObjectEntity> findEntity(UUID accountId, String urn, String objectUrn) throws IllegalArgumentException {

        Optional<ObjectEntity> entity = Optional.empty();
        UUID id = UuidUtil.getUuidFromUrn(urn);

        if (id != null) {
            entity = objectRepository.findByAccountIdAndId(accountId, id);

            if (entity.isPresent() && !StringUtils.isEmpty(objectUrn) && !objectUrn.equals(entity.get().getObjectUrn())) {
                throw new IllegalArgumentException("urn and objectUrn do not match");
            }
        }

        if (!StringUtils.isEmpty(objectUrn)) {
            entity = objectRepository.findByAccountIdAndObjectUrn(accountId, objectUrn);

            if (entity.isPresent() && id != null && !id.equals(entity.get().getId())) {
                throw new IllegalArgumentException("urn and objectUrn do not match");
            }
        }

        return entity;
    }

    /**
     * Saves an object entity in an {@link IObjectRepository}.
     *
     * @param objectEntity the object entity to persist
     * @return the persisted object entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
    private ObjectEntity persist(ObjectEntity objectEntity) throws ConstraintViolationException, TransactionException {
        try {
            return objectRepository.save(objectEntity);
        } catch (TransactionException e) {
            // we expect constraint violations to be the root cause for exceptions here,
            // so we throw this particular exception back to the caller
            if (ExceptionUtils.getRootCause(e) instanceof ConstraintViolationException) {
                throw (ConstraintViolationException) ExceptionUtils.getRootCause(e);
            } else {
                throw e;
            }
        }
    }

    private <T> void validate(T object) throws ConstraintViolationException {

        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException("Instance of " + object.getClass().getName() + " violates constraints", violations);
        }
    }
}
