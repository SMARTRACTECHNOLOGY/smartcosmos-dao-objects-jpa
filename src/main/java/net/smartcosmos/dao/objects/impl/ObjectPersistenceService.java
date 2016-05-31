package net.smartcosmos.dao.objects.impl;

import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode;
import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.objects.ObjectDao;
import net.smartcosmos.dao.objects.domain.ObjectEntity;
import net.smartcosmos.dao.objects.repository.ObjectRepository;
import net.smartcosmos.dao.objects.util.ObjectsPersistenceUtil;
import net.smartcosmos.dao.objects.util.SearchSpecifications;
import net.smartcosmos.dto.objects.ObjectCreate;
import net.smartcosmos.dto.objects.ObjectResponse;
import net.smartcosmos.dto.objects.ObjectUpdate;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.bouncycastle.asn1.cmp.OOBCertHash;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.data.jpa.domain.Specifications.where;

/**
 * @author voor
 */
@Slf4j
@Service
public class ObjectPersistenceService implements ObjectDao {

    private final ObjectRepository objectRepository;
    private final ConversionService conversionService;
    private final SearchSpecifications<ObjectEntity> searchSpecifications = new SearchSpecifications<ObjectEntity>();

    @Autowired
    public ObjectPersistenceService(ObjectRepository objectRepository,
            ConversionService conversionService) {
        this.objectRepository = objectRepository;
        this.conversionService = conversionService;
    }

    @Override
    public ObjectResponse create(String accountUrn, ObjectCreate createObject) {

        ObjectEntity entity = conversionService.convert(createObject, ObjectEntity.class);
        entity = persist(entity);

        return conversionService.convert(entity, ObjectResponse.class);
    }

    @Override
    public Optional<ObjectResponse> update(String accountUrn, ObjectUpdate updateObject) throws ConstraintViolationException {

        checkIdentifiers(updateObject);

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
    public Optional<ObjectResponse> findByUrn(String accountUrn, String urn)
    {

        Optional<ObjectEntity> entity = Optional.empty();
        try
        {
            UUID uuid = UuidUtil.getUuidFromUrn(urn);
            entity = objectRepository.findByAccountIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn), uuid);
        } catch (IllegalArgumentException e)
        {
            // Optional.empty() will be returned anyway
            log.warn("Illegal URN submitted: %s by account %s", urn, accountUrn);
        }

        if (entity.isPresent())
        {
            final ObjectResponse response = conversionService.convert(entity.get(), ObjectResponse.class);
            return Optional.ofNullable(response);
        }
        return Optional.empty();
    }

    @Override
    public List<Optional<ObjectResponse>> findByUrns(String accountUrn, Collection<String> urns)
    {

        List<Optional<ObjectResponse>> entities = new ArrayList<>();

        for (String urn: urns)
        {
            Optional<ObjectEntity> entity = Optional.empty();
            try
            {
                UUID uuid = UuidUtil.getUuidFromUrn(urn);
                entity = objectRepository.findByAccountIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn), uuid);

                if (entity.isPresent())
                {
                    final ObjectResponse response = conversionService.convert(entity.get(), ObjectResponse.class);
                    entities.add(Optional.ofNullable(response));
                }
                else {
                    entities.add(Optional.empty());
                }

            } catch (IllegalArgumentException e)
            {
                // If there's a bad value, we return an empty list
                log.warn("Illegal URN submitted: %s by account %s", urn, accountUrn);
                entities.add(Optional.empty());
            }

        }
        return entities;
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

        Specification<ObjectEntity> accountUrnSpecification = null;
        if (accountUrn != null) {
            UUID accountUuid = UuidUtil.getUuidFromAccountUrn(accountUrn);
            accountUrnSpecification = searchSpecifications.matchUuid(accountUuid, "accountId");
        }

        // this is only here so direct testing of this method doesn't need to include exact in the queryParameters,
        // since neither does the GetObjectResource
        Boolean exact = MapUtils.getBoolean(queryParameters, QueryParameterType.EXACT, false);

        Specification<ObjectEntity> objectUrnSpecification = getSearchSpecification(
            QueryParameterType.OBJECT_URN_FIELD_NAME,
            MapUtils.getString(queryParameters, QueryParameterType.OBJECT_URN_LIKE),
            exact);

        Specification<ObjectEntity> nameLikeSpecification = getSearchSpecification(
            QueryParameterType.NAME_FIELD_NAME,
            MapUtils.getString(queryParameters, QueryParameterType.NAME_LIKE),
            exact);

        Specification<ObjectEntity> typeSpecification = getSearchSpecification(
            QueryParameterType.TYPE_FIELD_NAME,
            MapUtils.getString(queryParameters, QueryParameterType.TYPE),
            true);

        Specification<ObjectEntity> monikerLikeSpecification = getSearchSpecification(
            QueryParameterType.MONIKER_FIELD_NAME,
            MapUtils.getString(queryParameters, QueryParameterType.MONIKER_LIKE),
            exact);

        Specification<ObjectEntity> lastModifiedAfterSpecification = null;
        Long lastModifiedAfter = MapUtils.getLong(queryParameters, QueryParameterType.MODIFIED_AFTER);
        if (lastModifiedAfter != null) {
            lastModifiedAfterSpecification = searchSpecifications.numberGreaterThan(lastModifiedAfter,
                QueryParameterType.MODIFIED_AFTER_FIELD_NAME.typeName());
        }

        Iterable<ObjectEntity> returnedValues = objectRepository.findAll(where(objectUrnSpecification)
            .and(accountUrnSpecification)
            .and(nameLikeSpecification)
            .and(typeSpecification)
            .and(monikerLikeSpecification)
            .and(lastModifiedAfterSpecification));

        List<ObjectResponse> convertedList = new ArrayList<>();
        for (ObjectEntity entity: returnedValues) {
            convertedList.add(conversionService.convert(entity, ObjectResponse.class));
        }

        return convertedList;
    }

    private Specification<ObjectEntity> getSearchSpecification(QueryParameterType queryParameterType,
                                                               String query,
                                                               Boolean exact) {
        Specification<ObjectEntity> specification = null;

        if (StringUtils.isNotBlank(query)) {
            if (exact) {
                specification = searchSpecifications.stringMatchesExactly(query, queryParameterType.typeName());
            }
            else {
                specification = searchSpecifications.stringStartsWith(query, queryParameterType.typeName());
            }
        }

        return specification;
    }

    private Optional<ObjectEntity> findEntity(UUID accountId, String urn, String objectUrn) throws IllegalArgumentException {

        Optional<ObjectEntity> entity = Optional.empty();

        if (StringUtils.isNotBlank(urn)) {
            UUID id = UuidUtil.getUuidFromUrn(urn);
            entity = objectRepository.findByAccountIdAndId(accountId, id);

            if (entity.isPresent() && StringUtils.isNotBlank(objectUrn) && !objectUrn.equals(entity.get().getObjectUrn())) {
                throw new IllegalArgumentException("urn and objectUrn do not match");
            }
        }

        if (StringUtils.isNotBlank(objectUrn)) {
            entity = objectRepository.findByAccountIdAndObjectUrn(accountId, objectUrn);

            if (entity.isPresent()) {
                ObjectEntity objectEntity = entity.get();
                String entityUrn = UuidUtil.getUrnFromUuid(objectEntity.getId());

                if (StringUtils.isNotBlank(urn) && !urn.equals(entityUrn)) {
                    throw new IllegalArgumentException("urn and objectUrn do not match");
                }
            }
        }

        return entity;
    }

    /**
     * Saves an object entity in an {@link ObjectRepository}.
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

    private void checkIdentifiers(ObjectUpdate updateObject) throws IllegalArgumentException {
        if (StringUtils.isBlank(updateObject.getUrn()) && StringUtils.isBlank(updateObject.getObjectUrn())) {
            throw new IllegalArgumentException(String.format("urn and objectUrn may not be null: %s", updateObject.toString()));
        }

        if (StringUtils.isNotBlank(updateObject.getUrn()) && StringUtils.isNotBlank(updateObject.getObjectUrn())) {
            throw new IllegalArgumentException(String.format("either urn or objectUrn may be defined: %s", updateObject.toString()));
        }
    }
}
