package net.smartcosmos.dao.things.repository;

import net.smartcosmos.dao.things.domain.ThingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author voor
 */
public interface ThingRepository extends JpaRepository<ThingEntity, UUID>, QueryByExampleExecutor<ThingEntity>, JpaSpecificationExecutor<ThingEntity>
{

    Optional<ThingEntity> findByAccountIdAndObjectUrn(UUID accountId, String objectUrn);

    Optional<ThingEntity> findByAccountIdAndId(UUID accountId, UUID id);

    List<ThingEntity> findByAccountIdAndObjectUrnStartsWith(UUID accountId, String objectUrn);

}
