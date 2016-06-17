package net.smartcosmos.dao.things.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.things.ThingDao;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dao.things.util.ThingPersistenceUtil;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
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
    public Optional<ThingResponse> create(String tenantUrn, ThingCreate createThing) {

        if (!alreadyExists(tenantUrn, createThing)) {
            try {
                UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);

                ThingEntity entity = conversionService.convert(createThing, ThingEntity.class);
                entity.setTenantId(tenantId);

                entity = persist(entity);
                ThingResponse response = conversionService.convert(entity, ThingResponse.class);

                return Optional.ofNullable(response);
            }
            catch (IllegalArgumentException e) {
                if (StringUtils.isNotBlank(createThing.getUrn())) {
                    log.warn("Error processing URNs: Tenant URN '{}' - Thing URN '{}'", tenantUrn, createThing.getUrn());
                }
                else {
                    log.warn("Error processing ID: Tenant ID '{}'", tenantUrn);
                }
                throw e;
            }
        }

        return Optional.empty();
    }

    private boolean alreadyExists(String tenantUrn, ThingCreate createThing) {

        return StringUtils.isNotBlank(createThing.getUrn()) && findByUrn(tenantUrn, createThing.getUrn()).isPresent();
    }

    @Override
    public Optional<ThingResponse> update(String tenantUrn, String type, String urn, ThingUpdate updateThing) throws ConstraintViolationException {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID id = UuidUtil.getUuidFromUrn(urn);
            Optional<ThingEntity> thing = repository.findByIdAndTenantIdAndType(id, tenantId, type);

            if (thing.isPresent()) {
                ThingEntity updateEntity = ThingPersistenceUtil.merge(thing.get(), updateThing);
                updateEntity = persist(updateEntity);
                final ThingResponse response = conversionService.convert(updateEntity, ThingResponse.class);

                return Optional.ofNullable(response);
            }
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URNs: Tenant URN '{}' - Thing URN '{}'", tenantUrn, urn);
        }

        return Optional.empty();
    }

    @Override
    public List<ThingResponse> delete(String tenantUrn, String type, String urn) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID id = UuidUtil.getUuidFromUrn(urn);
            List<ThingEntity> deleteList = repository.deleteByIdAndTenantIdAndType(id, tenantId, type);

            return convert(deleteList);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URNs: Tenant URN '{}' - Thing URN '{}'", tenantUrn, urn);
        }

        return new ArrayList<>();
    }

    @Override
    public Optional<ThingResponse> findByTypeAndUrn(String tenantUrn, String type, String urn) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID id = UuidUtil.getUuidFromUrn(urn);

            Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndType(id, tenantId, type);

            return convert(entity);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URNs: Tenant URN '{}' - Thing URN '{}'", tenantUrn, urn);
        }

        return Optional.empty();
    }

    @Override
    public List<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, Long page, Integer size) {

        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    private Optional<ThingResponse> findByUrn(String tenantUrn, String urn) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID id = UuidUtil.getUuidFromUrn(urn);

            Optional<ThingEntity> entity = repository.findByIdAndTenantId(id, tenantId);

            return convert(entity);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URNs: Tenant URN '{}' - Thing URN '{}'", tenantUrn, urn);
        }

        return Optional.empty();
    }

    @Override
    public List<Optional<ThingResponse>> findByUrns(String tenantUrn, Collection<String> urns) {

        return urns.stream()
            .map(urn -> findByUrn(tenantUrn, urn))
            .collect(Collectors.toList());
    }

    @Override
    public List<ThingResponse> findAll(String tenantUrn, Long page, Integer size) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            List<ThingEntity> entityList = repository.findByTenantId(tenantId);

            return convert(entityList);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return new ArrayList<>();
    }

    /**
     * This is a temporary function for development purposes -- eventually we don't want
     * to support a "get everything" call, since theoretically that'd be billions of
     * objects.
     *
     * @return All the objects.
     */
    public List<ThingResponse> getThings() {
        // You could theoretically create a conversion function to handle this, since
        // it'll happen fairly often and in numerous places, but for example sake it's
        // done inline here.
        return convert(repository.findAll());
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

    /**
     * Converts a single thing entity optional to the corresponding response object.
     *
     * @param entity the thing entity
     * @return an {@link Optional} that contains a {@link ThingResponse} instance or is empty
     */
    private Optional<ThingResponse> convert(Optional<ThingEntity> entity) {

        if (entity.isPresent()) {
            final ThingResponse response = conversionService.convert(entity.get(), ThingResponse.class);
            return Optional.ofNullable(response);
        }
        else {
            return Optional.empty();
        }
    }

    /**
     * Converts a list of thing entities to a list of corresponding response objects.
     *
     * @param entityList the entities
     * @return the list of {@link ThingResponse} instances
     */
    private List<ThingResponse> convert(List<ThingEntity> entityList) {
        return entityList.stream()
            .map(o -> conversionService.convert(o, ThingResponse.class))
            .collect(Collectors.toList());
    }
}
