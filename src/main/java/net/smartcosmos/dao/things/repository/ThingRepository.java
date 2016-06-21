package net.smartcosmos.dao.things.repository;

import net.smartcosmos.dao.things.domain.ThingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThingRepository extends JpaRepository<ThingEntity, UUID>, PagingAndSortingRepository<ThingEntity, UUID>, QueryByExampleExecutor<ThingEntity>, JpaSpecificationExecutor<ThingEntity>
{
    @Transactional
    List<ThingEntity> deleteByIdAndTenantIdAndType(UUID id, UUID tenantId, String type);

    Optional<ThingEntity> findByIdAndTenantIdAndType(UUID id, UUID tenantId, String type);

    Optional<ThingEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    List<ThingEntity> findByTenantIdAndType(UUID tenantId, String type);

    List<ThingEntity> findByTenantIdAndType(UUID tenantId, String type, Sort sort);

    Page<ThingEntity> findByTenantIdAndType(UUID tenantId, String type, Pageable pageable);

    List<ThingEntity> findByTenantId(UUID tenantId);

    List<ThingEntity> findByTenantId(UUID tenantId, Sort sort);

    List<ThingEntity> findByIdInAndTenantId(List<UUID> ids, UUID tenantId);

    List<ThingEntity> findByIdInAndTenantId(List<UUID> ids, UUID tenantId, Sort sort);

    Page<ThingEntity> findByTenantId(UUID tenantId, Pageable pageable);
}
