package net.smartcosmos.dao.things.repository;

import java.util.Collection;
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

public interface ThingRepository
    extends JpaRepository<ThingEntity, UUID>, PagingAndSortingRepository<ThingEntity, UUID>, QueryByExampleExecutor<ThingEntity>,
            JpaSpecificationExecutor<ThingEntity> {

    @Transactional
    List<ThingEntity> deleteByIdAndTenantIdAndType(UUID id, UUID tenantId, String type);

    Optional<ThingEntity> findByIdAndTenantIdAndType(UUID id, UUID tenantId, String type);

    Page<ThingEntity> findByTenantIdAndType(UUID tenantId, String type, Pageable pageable);

    Page<ThingEntity> findByTenantId(UUID tenantId, Pageable pageable);

    List<ThingEntity> findByTenantIdAndTypeAndIdIn(UUID tenantId, String type, Collection<UUID> ids);

    List<ThingEntity> findByTenantIdAndTypeAndIdIn(UUID tenantId, String type, Collection<UUID> ids, Sort sort);

    Page<ThingEntity> findAll(Pageable pageable);

}
