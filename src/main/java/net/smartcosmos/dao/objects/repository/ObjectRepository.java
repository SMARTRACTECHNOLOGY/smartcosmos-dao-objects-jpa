package net.smartcosmos.dao.objects.repository;

import net.smartcosmos.dao.objects.domain.ObjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author voor
 */
public interface ObjectRepository extends JpaRepository<ObjectEntity, UUID>, QueryByExampleExecutor<ObjectEntity>
{

    Optional<ObjectEntity> findByAccountIdAndObjectUrn(UUID accountId, String objectUrn);

    Optional<ObjectEntity> findByAccountIdAndId(UUID accountId, UUID id);

    List<ObjectEntity> findByAccountIdAndObjectUrnStartsWith(UUID accountId, String objectUrn);
}
