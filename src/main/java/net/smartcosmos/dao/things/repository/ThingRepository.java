package net.smartcosmos.dao.things.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    List<ThingEntity> findByTenantIdAndTypeIgnoreCase(UUID tenantId, String type);

    List<ThingEntity> findByTenantIdAndTypeIgnoreCase(UUID tenantId, String type, Sort sort);

    Page<ThingEntity> findByTenantIdAndTypeIgnoreCase(UUID tenantId, String type, Pageable pageable);

    List<ThingEntity> findByTenantId(UUID tenantId);

    List<ThingEntity> findByTenantId(UUID tenantId, Sort sort);

    Page<ThingEntity> findByTenantId(UUID tenantId, Pageable pageable);

    List<ThingEntity> findByTenantIdAndTypeIgnoreCaseAndIdIn(UUID tenantId, String type, List<UUID> ids);

    List<ThingEntity> findByTenantIdAndTypeIgnoreCaseAndIdIn(UUID tenantId, String type, List<UUID> ids, Sort sort);

}
