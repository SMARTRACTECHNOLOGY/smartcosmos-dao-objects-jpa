package net.smartcosmos.dao.things.impl;

import lombok.extern.slf4j.Slf4j;
import net.smartcosmos.dao.things.SortOrder;
import net.smartcosmos.dao.things.ThingDao;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dao.things.util.ThingPersistenceUtil;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.PageInformation;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
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

    // region Create

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

    // endregion

    // region Update

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

    // endregion

    // region Find By Type

    @Override
    public List<ThingResponse> findByType(String tenantUrn, String type) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            List<ThingEntity> deleteList = repository.findByTenantIdAndType(tenantId, type);

            return convert(deleteList);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return new ArrayList<>();
    }

    @Override
    public List<ThingResponse> findByType(String tenantUrn, String type, SortOrder sortOrder, String sortBy) {
        // TODO: Implement Sorting
        return findByType(tenantUrn, type);
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, Integer page, Integer size) {
        // TODO: Implement Paging
        return null;
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, Integer page, Integer size, SortOrder sortOrder, String sortBy) {
        // TODO: Implement Paging and Sorting
        return null;
    }

    // endregion

    // region Delete

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

    // endregion

    // region Find By Type and URN

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

    // endregion

    // region Find by Type and URN startsWith

    @Override
    public List<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith) {
        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    @Override
    public List<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, SortOrder sortOrder, String sortBy) {
        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    @Override
    public Page<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, Integer page, Integer size) {
        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    @Override
    public Page<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, Integer page, Integer size, SortOrder sortOrder, String sortBy) {
        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    // endregion

    // region Find by URNs

    @Override
    public List<ThingResponse> findByUrns(String tenantUrn, Collection<String> urns) {

        UUID tenantId;
        try {
            tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
            return new ArrayList<>();
        }

        List<UUID> ids = urns.stream()
            .map(urn -> {
                try {
                    return UuidUtil.getUuidFromUrn(urn);
                } catch (IllegalArgumentException e) {
                    log.warn("Error processing URNs: Tenant URN '{}' - Thing URN '{}'", tenantUrn, urn);
                }
                return null;
            })
            .filter(uuid -> uuid != null)
            .collect(Collectors.toList());

        List<ThingEntity> entityList = repository.findByIdInAndTenantId(ids, tenantId);

        return convert(entityList);
    }

    @Override
    public List<ThingResponse> findByUrns(String tenantUrn, Collection<String> urns, SortOrder sortOrder, String sortBy) {
        // TODO: Implement Sorting
        return findByUrns(tenantUrn, urns);
    }

    // endregion

    // region Find All

    @Override
    public List<ThingResponse> findAll(String tenantUrn) {

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

    @Override
    public List<ThingResponse> findAll(String tenantUrn, SortOrder sortOrder, String sortBy) {
        // TODO: Implement Sorting
        return null;
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, Integer page, Integer size) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            org.springframework.data.domain.Page<ThingEntity> entityList = repository.findByTenantId(tenantId, new PageRequest(page, size));

            return convert(entityList);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return ThingPersistenceUtil.emptyPage();
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, Integer page, Integer size, SortOrder sortOrder, String sortBy) {
        // TODO: Implement Sorting
        return findAll(tenantUrn, page, size);
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

    // endregion

    // region Helper Methods

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

    private boolean alreadyExists(String tenantUrn, ThingCreate createThing) {

        return StringUtils.isNotBlank(createThing.getUrn()) && findByUrn(tenantUrn, createThing.getUrn()).isPresent();
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

    private Page<ThingResponse> convert(org.springframework.data.domain.Page<ThingEntity> page) {
        List<ThingResponse> data = convert(page.getContent());

        PageInformation pageInformation = PageInformation.builder()
            .number(page.getNumber())
            .totalElements(page.getTotalElements())
            .size(page.getNumberOfElements())
            .totalPages(page.getTotalPages())
            .build();

        return new Page<>(data, pageInformation);
    }

    // endregion
}
