package net.smartcosmos.dao.things.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionException;

import net.smartcosmos.dao.things.SortOrder;
import net.smartcosmos.dao.things.ThingDao;
import net.smartcosmos.dao.things.domain.ThingEntity;
import net.smartcosmos.dao.things.repository.ThingRepository;
import net.smartcosmos.dao.things.util.ThingPersistenceUtil;
import net.smartcosmos.dao.things.util.UuidUtil;
import net.smartcosmos.dto.things.Page;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;

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
            Optional<ThingEntity> thing = repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

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

        return findByTypeList(tenantUrn, type, null);
    }

    @Override
    public List<ThingResponse> findByType(String tenantUrn, String type, SortOrder sortOrder, String sortBy) {

        sortBy = ThingPersistenceUtil.getSortByFieldName(sortBy);
        Sort.Direction direction = ThingPersistenceUtil.getSortDirection(sortOrder);
        Sort sort = new Sort(direction, sortBy);

        return findByTypeList(tenantUrn, type, sort);
    }

    private List<ThingResponse> findByTypeList(String tenantUrn, String type, Sort sort) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            List<ThingEntity> entityList;
            if (sort != null) {
                entityList = repository.findByTenantIdAndTypeIgnoreCase(tenantId, type, sort);
            } else {
                entityList = repository.findByTenantIdAndTypeIgnoreCase(tenantId, type);
            }

            return convert(entityList);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return new ArrayList<>();
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, Integer page, Integer size) {

        return findByTypePage(tenantUrn, type, getPageable(page, size, null, null));
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        Sort.Direction direction = ThingPersistenceUtil.getSortDirection(sortOrder);
        sortBy = ThingPersistenceUtil.getSortByFieldName(sortBy);

        return findByTypePage(tenantUrn, type, getPageable(page, size, sortBy, direction));
    }

    private Page<ThingResponse> findByTypePage(String tenantUrn, String type, Pageable pageable) {

        Page<ThingResponse> result = ThingPersistenceUtil.emptyPage();
        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            org.springframework.data.domain.Page<ThingEntity> pageResponse = repository.findByTenantIdAndTypeIgnoreCase(tenantId, type, pageable);

            return conversionService.convert(pageResponse, result.getClass());
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return result;
    }

    // endregion

    // region Delete

    @Override
    public List<ThingResponse> delete(String tenantUrn, String type, String urn) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID id = UuidUtil.getUuidFromUrn(urn);
            List<ThingEntity> deleteList = repository.deleteByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

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

            Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

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
    public List<ThingResponse> findByTypeAndUrns(String tenantUrn, String type, Collection<String> urns) {

        return findByTypeAndUrns(tenantUrn, type, urns, null);
    }

    @Override
    public List<ThingResponse> findByTypeAndUrns(String tenantUrn, String type, Collection<String> urns, SortOrder sortOrder, String sortBy) {

        sortBy = ThingPersistenceUtil.getSortByFieldName(sortBy);
        Sort.Direction direction = ThingPersistenceUtil.getSortDirection(sortOrder);
        Sort sort = new Sort(direction, sortBy);

        return findByTypeAndUrns(tenantUrn, type, urns, sort);
    }

    private List<ThingResponse> findByTypeAndUrns(String tenantUrn, String type, Collection<String> urns, Sort sort) {

        UUID tenantId;
        try {
            tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
            return new ArrayList<>();
        }

        List<UUID> ids = getUuidListFromUrnCollection(tenantUrn, urns);

        List<ThingEntity> entityList;
        if (sort != null) {
            entityList = repository.findByTenantIdAndTypeIgnoreCaseAndIdIn(tenantId, type, ids, sort);
        } else {
            entityList = repository.findByTenantIdAndTypeIgnoreCaseAndIdIn(tenantId, type, ids);
        }

        return convert(entityList);
    }

    private List<UUID> getUuidListFromUrnCollection(String tenantUrn, Collection<String> urns) {
        return urns.stream()
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
        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            sortBy = ThingPersistenceUtil.getSortByFieldName(sortBy);
            Sort sort = new Sort(ThingPersistenceUtil.getSortDirection(sortOrder), sortBy);
            List<ThingEntity> entityList = repository.findByTenantId(tenantId, sort);

            return convert(entityList);
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return new ArrayList<>();
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, Integer page, Integer size) {

        return findAll(tenantUrn, getPageable(page, size, null, null));
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        Sort.Direction direction = ThingPersistenceUtil.getSortDirection(sortOrder);
        sortBy = ThingPersistenceUtil.getSortByFieldName(sortBy);

        return findAll(tenantUrn, getPageable(page, size, sortBy, direction));
    }

    private Page<ThingResponse> findAll(String tenantUrn, Pageable pageable) {

        Page<ThingResponse> result = ThingPersistenceUtil.emptyPage();
        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            org.springframework.data.domain.Page<ThingEntity> pageResponse = repository.findByTenantId(tenantId, pageable);

            return conversionService.convert(pageResponse, result.getClass());
        }
        catch (IllegalArgumentException e) {
            log.warn("Error processing URN: Tenant URN '{}'", tenantUrn);
        }

        return result;
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

        return StringUtils.isNotBlank(createThing.getUrn()) && findByUrnAndType(tenantUrn, createThing.getUrn(), createThing.getType()).isPresent();
    }

    private Optional<ThingResponse> findByUrnAndType(String tenantUrn, String urn, String type) {

        try {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            UUID id = UuidUtil.getUuidFromUrn(urn);

            Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

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

    /**
     * Builds the pageable for repository calls, including translation of 1-based page numbering on the API level to
     * 0-based page numbering on the repository level.
     *
     * @param page the page number
     * @param size the page size
     * @param sortBy the name of the field to sort by
     * @param direction the sort order direction
     * @return the pageable object
     */
    protected Pageable getPageable(Integer page, Integer size, String sortBy, Sort.Direction direction) {

        if (page < 1) {
            throw new IllegalArgumentException("Page index must not be less than one!");
        }
        page = page - 1;

        if (StringUtils.isBlank(sortBy) || direction == null) {
            return new PageRequest(page, size);
        }

        return new PageRequest(page, size, direction, sortBy);
    }

    // endregion
}
