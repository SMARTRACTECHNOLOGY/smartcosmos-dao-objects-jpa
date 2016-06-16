package net.smartcosmos.dao.things.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.things.ThingDao;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dao.things.util.ThingPersistenceUtil;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
import net.smartcosmos.util.UuidUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ThingPersistenceService implements ThingDao {

    private final ThingRepository repository;
    private final ConversionService conversionService;

    @Autowired
    public ThingPersistenceService(ThingRepository repository,
                                   ConversionService conversionService) {
        this.repository = repository;
        this.conversionService = conversionService;
    }

    @Override
    public ThingResponse create(String tenantId, ThingCreate createThing) {

        UUID tenantUuid = UUID.fromString(tenantId);

        ThingEntity entity = conversionService.convert(createThing, ThingEntity.class);
        entity.setTenantId(tenantUuid);

        entity = persist(entity);

        return conversionService.convert(entity, ThingResponse.class);
    }

    @Override
    public Optional<ThingResponse> updateByTypeAndUrn(String tenantId, String type, String urn, ThingUpdate updateThing) throws ConstraintViolationException {

        UUID tenantUuid = UUID.fromString(tenantId);

        Optional<ThingEntity> thing = repository.findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        return update(thing, updateThing);
    }

    @Override
    public Optional<ThingResponse> updateById(String tenantId, String id, ThingUpdate updateThing) throws ConstraintViolationException {

        UUID tenantUuid = UUID.fromString(tenantId);
        UUID thingId = UUID.fromString(id);

        Optional<ThingEntity> thing = repository.findByTenantIdAndId(tenantUuid, thingId);

        return update(thing, updateThing);
    }

    private Optional<ThingResponse> update(Optional<ThingEntity> entity, ThingUpdate updateThing) {

        if (entity.isPresent()) {
            ThingEntity updateEntity = ThingPersistenceUtil.merge(entity.get(), updateThing);
            updateEntity = persist(updateEntity);
            final ThingResponse response = conversionService.convert(updateEntity, ThingResponse.class);

            return Optional.ofNullable(response);
        }

        return Optional.empty();
    }

    @Override
    public List<ThingResponse> deleteById(String tenantId, String id) {

        UUID tenantUuid = UUID.fromString(tenantId);

        List<ThingEntity> deleteList = repository.deleteByTenantIdAndId(tenantUuid, UUID.fromString(id));

        return convert(deleteList);
    }

    @Override
    public List<ThingResponse> deleteByTypeAndUrn(String tenantId, String type, String urn) {

        UUID tenantUuid = UUID.fromString(tenantId);

        List<ThingEntity> deleteList = repository.deleteByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        return convert(deleteList);
    }

    @Override
    public Optional<ThingResponse> findByTypeAndUrn(String tenantId, String type, String urn) {

        UUID tenantUuid = UUID.fromString(tenantId);
        Optional<ThingEntity> entity = repository.findByTenantIdAndTypeAndUrn(tenantUuid, type, urn);

        return convert(entity);
    }

    @Override
    public List<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, Long page, Long size) {
        return null;
    }

    @Override
    public Optional<ThingResponse> findById(String tenantId, String id) {

        UUID tenantUuid = UUID.fromString(tenantId);
        UUID thingId = UUID.fromString(id);
        Optional<ThingEntity> entity = repository.findByTenantIdAndId(tenantUuid, thingId);

        return convert(entity);
    }

    @Override
    public List<Optional<ThingResponse>> findByIds(String tenantId, Collection<String> ids, Long page, Long size) {

        List<Optional<ThingResponse>> responseList = new ArrayList<>();

        for (String id: ids)
        {
            Optional<ThingResponse> response = findById(tenantId, id);
            responseList.add(response);
        }

        return responseList;
    }

    @Override
    public List<ThingResponse> findAll(String tenantId, Long page, Long size) {

        UUID tenantUuid = UUID.fromString(tenantId);
        List<ThingEntity> entityList = repository.findByTenantId(tenantUuid);

        return convert(entityList);
    }

    /**
     * Finds objects matching a specified object URN start.
     *
     * @param accountUrn the account URN
     * @param objectUrnStartsWith the first characters of the object URN
     * @return all objects whose {@code urn} starts with {@code objectUrnStartsWith}
     */
    @Deprecated
    public List<ThingResponse> findByObjectUrnStartsWith(String accountUrn, String objectUrnStartsWith) {

        List<ThingEntity> entityList = repository.findByTenantIdAndUrnStartsWith(UuidUtil.getUuidFromAccountUrn(accountUrn),
            objectUrnStartsWith);

        return convert(entityList);
    }

    @Deprecated
    public List<Optional<ThingResponse>> findByUrns(String accountUrn, Collection<String> urns)
    {

        List<Optional<ThingResponse>> entities = new ArrayList<>();

        for (String urn: urns)
        {
            Optional<ThingEntity> entity = Optional.empty();
            try
            {
                UUID uuid = UuidUtil.getUuidFromUrn(urn);
                entity = repository.findByTenantIdAndId(UuidUtil.getUuidFromAccountUrn(accountUrn), uuid);

                if (entity.isPresent())
                {
                    final ThingResponse response = conversionService.convert(entity.get(), ThingResponse.class);
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
    public List<ThingResponse> getObjects() {
        // You could theoretically create a conversion function to handle this, since
        // it'll happen fairly often and in numerous places, but for example sake it's
        // done inline here.
        return convert(repository.findAll());
    }

    private Optional<ThingEntity> findEntity(UUID tenantId, UUID id, String type, String urn) throws IllegalArgumentException {

        Optional<ThingEntity> entity = Optional.empty();

        if (id != null) {
            entity = repository.findByTenantIdAndId(tenantId, id);

            if (entity.isPresent() && StringUtils.isNotBlank(urn) && !urn.equals(entity.get().getUrn())) {
                throw new IllegalArgumentException("ID and URN do not match");
            }
        }

        if (StringUtils.isNotBlank(urn)) {
            entity = repository.findByTenantIdAndTypeAndUrn(tenantId, type, urn);

            if (entity.isPresent()) {
                ThingEntity objectEntity = entity.get();
                UUID entityId = objectEntity.getId();

                if (id != null && id != entityId) {
                    throw new IllegalArgumentException("ID and URN do not match");
                }
            }
        }

        return entity;
    }

    /**
     * Saves an object entity in an {@link ThingRepository}.
     *
     * @param objectEntity the object entity to persist
     * @return the persisted object entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException if the transaction fails because of something else
     */
    private ThingEntity persist(ThingEntity objectEntity) throws ConstraintViolationException, TransactionException {
        try {
            return repository.save(objectEntity);
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

    private Optional<ThingResponse> convert(Optional<ThingEntity> entity) {

        if (entity.isPresent()) {
            final ThingResponse response = conversionService.convert(entity.get(), ThingResponse.class);
            return Optional.ofNullable(response);
        }
        else {
            return Optional.empty();
        }
    }

    private List<ThingResponse> convert(List<ThingEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, ThingResponse.class))
            .collect(Collectors.toList());
    }
}
