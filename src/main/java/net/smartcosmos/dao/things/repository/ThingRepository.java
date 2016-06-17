package net.smartcosmos.dao.things.repository;

import net.smartcosmos.dao.things.domain.ThingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ThingRepository extends JpaRepository<ThingEntity, UUID>, QueryByExampleExecutor<ThingEntity>, JpaSpecificationExecutor<ThingEntity>
{
    @Transactional
    List<ThingEntity> deleteByTenantIdAndId(UUID tenantId, UUID id);

    Optional<ThingEntity> findByTenantIdAndId(UUID tenantId, UUID id);

    List<ThingEntity> findByTenantId(UUID tenantId);
}
