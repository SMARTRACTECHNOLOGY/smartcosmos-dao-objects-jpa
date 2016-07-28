package net.smartcosmos.dao.things.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.transaction.annotation.Transactional;

import net.smartcosmos.dao.things.domain.ThingEntity;

public interface ThingRepository extends JpaRepository<ThingEntity, UUID>, PagingAndSortingRepository<ThingEntity, UUID>, QueryByExampleExecutor<ThingEntity>, JpaSpecificationExecutor<ThingEntity>
{
    @Transactional
    List<ThingEntity> deleteByIdAndTenantIdAndTypeIgnoreCase(UUID id, UUID tenantId, String type);

    Optional<ThingEntity> findByIdAndTenantIdAndTypeIgnoreCase(UUID id, UUID tenantId, String type);

    Optional<ThingEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    Page<ThingEntity> findByTenantIdAndTypeIgnoreCase(UUID tenantId, String type, Pageable pageable);

    Page<ThingEntity> findByTenantId(UUID tenantId, Pageable pageable);

    Page<ThingEntity> findByTenantIdAndTypeIgnoreCaseAndIdIn(UUID tenantId, String type, Collection<UUID> ids, Pageable pageable);

    Page<ThingEntity> findAll(Pageable pageable);

}
