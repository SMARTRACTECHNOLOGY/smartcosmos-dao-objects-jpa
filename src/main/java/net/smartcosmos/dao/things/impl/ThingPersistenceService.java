package net.smartcosmos.dao.things.impl;

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
import org.springframework.core.convert.TypeDescriptor;
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
import net.smartcosmos.dto.things.PageInformation;
import net.smartcosmos.dto.things.ThingCreate;
import net.smartcosmos.dto.things.ThingResponse;
import net.smartcosmos.dto.things.ThingUpdate;

import static net.smartcosmos.dao.things.util.ThingPersistenceUtil.emptyPage;

@Slf4j
@Service
public class ThingPersistenceService implements ThingDao {

    private final ThingRepository repository;
    private final ConversionService conversionService;

    @Autowired
    public ThingPersistenceService(
        ThingRepository repository,
        ConversionService conversionService) {

        this.repository = repository;
        this.conversionService = conversionService;
    }

    // region Create

    @Override
    public Optional<ThingResponse> create(String tenantUrn, ThingCreate createThing) {

        if (!alreadyExists(tenantUrn, createThing)) {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);

            ThingEntity entity = conversionService.convert(createThing, ThingEntity.class);
            entity.setTenantId(tenantId);

            entity = persist(entity);
            ThingResponse response = conversionService.convert(entity, ThingResponse.class);

            return Optional.ofNullable(response);

        }

        return Optional.empty();
    }

    // endregion

    // region Update

    @Override
    public Optional<ThingResponse> update(String tenantUrn, String type, String urn, ThingUpdate updateThing) throws ConstraintViolationException {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID id = UuidUtil.getUuidFromUrn(urn);
        Optional<ThingEntity> thing = repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

        if (thing.isPresent()) {
            ThingEntity updateEntity = ThingPersistenceUtil.merge(thing.get(), updateThing);
            updateEntity = persist(updateEntity);
            final ThingResponse response = conversionService.convert(updateEntity, ThingResponse.class);

            return Optional.ofNullable(response);
        }

        return Optional.empty();
    }

    // endregion

    // region Delete

    @Override
    public Optional<ThingResponse> delete(String tenantUrn, String type, String urn) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID id = UuidUtil.getUuidFromUrn(urn);
        List<ThingEntity> deleteList = repository.deleteByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);

        if (!deleteList.isEmpty()) {
            return Optional.ofNullable(conversionService.convert(deleteList.get(0), ThingResponse.class));
        }

        return Optional.empty();
    }

    // endregion

    // region Find By Type

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type) {

        return findByType(tenantUrn, type, getPageable(null, null, null, null));
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, SortOrder sortOrder, String sortBy) {

        return findByType(tenantUrn, type, getPageable(null, null, ThingPersistenceUtil.getSortByFieldName(sortBy),
                                                       ThingPersistenceUtil.getSortDirection(sortOrder)));
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, Integer page, Integer size) {

        return findByType(tenantUrn, type, getPageable(page, size, null, null));
    }

    @Override
    public Page<ThingResponse> findByType(String tenantUrn, String type, Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        return findByType(tenantUrn, type, getPageable(page, size, ThingPersistenceUtil.getSortByFieldName(sortBy),
                                                       ThingPersistenceUtil.getSortDirection(sortOrder)));
    }

    private Page<ThingResponse> findByType(String tenantUrn, String type, Pageable pageable) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        org.springframework.data.domain.Page<ThingEntity> pageResponse = repository.findByTenantIdAndTypeIgnoreCase(tenantId, type, pageable);

        return conversionService.convert(pageResponse, emptyPage().getClass());
    }

    // endregion

    // region Find By Type and URN

    @Override
    public Optional<ThingResponse> findByTypeAndUrn(String tenantUrn, String type, String urn) {

        UUID id = UuidUtil.getUuidFromUrn(urn);

        Optional<ThingEntity> entity;
        if (StringUtils.isNotBlank(tenantUrn)) {
            UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
            entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);
        } else {
            entity = repository.findByIdAndTypeIgnoreCase(id, type);
        }

        if (entity.isPresent()) {
            return Optional.ofNullable(conversionService.convert(entity.get(), ThingResponse.class));
        }

        return Optional.empty();
    }

    // endregion

    // region Find by Type and URN startsWith

    @Override
    public Page<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith) {

        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    @Override
    public Page<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, Integer page, Integer number) {

        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    @Override
    public Page<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, SortOrder sortOrder, String sortBy) {

        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    @Override
    public Page<ThingResponse> findByTypeAndUrnStartsWith(
        String tenantUrn, String type, String urnStartsWith, Integer page, Integer size,
        SortOrder sortOrder, String sortBy) {

        throw new UnsupportedOperationException("The database implementation does not support 'startsWith' search for URNs");
    }

    private Page<ThingResponse> findByTypeAndUrnStartsWith(String tenantUrn, String type, String urnStartsWith, Pageable pageable) {

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

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        List<UUID> ids = getUuidListFromUrnCollection(tenantUrn, urns);

        List<ThingEntity> entityList;
        if (sort != null) {
            entityList = repository.findByTenantIdAndTypeIgnoreCaseAndIdIn(tenantId, type, ids, sort);
        } else {
            entityList = repository.findByTenantIdAndTypeIgnoreCaseAndIdIn(tenantId, type, ids);
        }

        return convertList(entityList, ThingEntity.class, ThingResponse.class);
    }

    // endregion

    // region Find All

    @Override
    public Page<ThingResponse> findAll(String tenantUrn) {

        return findAll(tenantUrn, getPageable(null, null, null, null));
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, SortOrder sortOrder, String sortBy) {

        return findAll(tenantUrn, getPageable(null, null, sortBy, ThingPersistenceUtil.getSortDirection(sortOrder)));
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, Integer page, Integer size) {

        return findAll(tenantUrn, getPageable(page, size, null, null));
    }

    @Override
    public Page<ThingResponse> findAll(String tenantUrn, Integer page, Integer size, SortOrder sortOrder, String sortBy) {

        return findAll(tenantUrn, getPageable(page, size, sortBy, ThingPersistenceUtil.getSortDirection(sortOrder)));
    }

    private Page<ThingResponse> findAll(String tenantUrn, Pageable pageable) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        org.springframework.data.domain.Page<ThingEntity> pageResponse = repository.findByTenantId(tenantId, pageable);

        return conversionService.convert(pageResponse, emptyPage().getClass());
    }

    /**
     * This is a temporary function for development purposes -- eventually we don't want
     * to support a "get everything" call, since theoretically that'd be billions of
     * objects.
     *
     * @return All the objects.
     */
    public Page<ThingResponse> getThings() {

        return convertPage(repository.findAll(getPageable(null, null, null, null)), ThingEntity.class, ThingResponse.class);
    }

    // endregion

    // region Helper Methods

    /**
     * Saves an object entity in an {@link ThingRepository}.
     *
     * @param objectEntity the object entity to persist
     * @return the persisted object entity
     * @throws ConstraintViolationException if the transaction fails due to violated constraints
     * @throws TransactionException         if the transaction fails because of something else
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

    private boolean alreadyExists(String tenantUrn, ThingCreate createThing) {

        return StringUtils.isNotBlank(createThing.getUrn()) && findByUrnAndType(tenantUrn, createThing.getUrn(), createThing.getType()).isPresent();
    }

    private Optional<ThingResponse> findByUrnAndType(String tenantUrn, String urn, String type) {

        UUID tenantId = UuidUtil.getUuidFromUrn(tenantUrn);
        UUID id = UuidUtil.getUuidFromUrn(urn);

        Optional<ThingEntity> entity = repository.findByIdAndTenantIdAndTypeIgnoreCase(id, tenantId, type);
        if (entity.isPresent()) {
            return Optional.ofNullable(conversionService.convert(entity.get(), ThingResponse.class));
        }
            
        return Optional.empty();
    }

    /**
     * Uses the conversion service to convert a typed list into another typed list.
     *
     * @param list the list
     * @param sourceClass the class of the source type
     * @param targetClass the class of the target type
     * @param <S> the generic source type
     * @param <T> the generic target type
     * @return the converted typed list
     */
    @SuppressWarnings("unchecked")
    private <S, T> List<T> convertList(List<S> list, Class sourceClass, Class targetClass) {

        TypeDescriptor sourceDescriptor = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(sourceClass));
        TypeDescriptor targetDescriptor = TypeDescriptor.collection(List.class, TypeDescriptor.valueOf(targetClass));

        return (List<T>) conversionService.convert(list, sourceDescriptor, targetDescriptor);
    }

    /**
     * Uses the conversion service to covert a typed {@link org.springframework.data.domain.Page} into a typed {@link Page}, i.e. converts the page
     * information and the content list.
     *
     * @param page the page
     * @param sourceClass the class of the source type
     * @param targetClass the class of the target type
     * @param <S> the generic source type
     * @param <T> the generic target type
     * @return the converted typed page
     */
    private <S, T> Page<T> convertPage(org.springframework.data.domain.Page<S> page, Class sourceClass, Class targetClass) {

        return Page.<T>builder()
            .page(conversionService.convert(page, PageInformation.class))
            .data(convertList(page.getContent(), sourceClass, targetClass))
            .build();
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

        if (page == null) {
            page = DEFAULT_PAGE;
        }
        if (size == null) {
            size = DEFAULT_SIZE;
        }
        if (sortBy == null) {
            sortBy = DEFAULT_SORT_BY;
        }
        if (direction == null) {
            direction = Sort.Direction.fromString(DEFAULT_SORT_ORDER.toString());
        }

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
